package za.co.entelect;

import java.util.List;

public class Simulator {
    private final Car car;
    private final Track track;
    private final RaceConfig config;

    public Simulator(Car car, Track track, RaceConfig config) {
        this.car = car;
        this.track = track;
        this.config = config;
    }

    public RaceResult simulateRace(Strategy strategy) {
        RaceResult result = new RaceResult(config.laps);
        double currentFuel = car.initialFuel;
        Tyre currentTyre = strategy.initialTyre.copy();
        double entrySpeed = 0;  // Start from rest
        
        for (int lap = 0; lap < config.laps; lap++) {
            LapStrategy lapStrat = strategy.laps.get(lap);
            double lapTime = 0;
            double lapFuel = 0;
            
            // Check for pit stop at start of lap (end of previous lap)
            if (lapStrat.pitStop != null && lapStrat.pitStop.enter) {
                double pitTime = calculatePitTime(lapStrat.pitStop, currentFuel);
                lapTime += pitTime;
                
                // Change tyre if specified
                if (lapStrat.pitStop.tyreChangeId > 0) {
                    currentTyre = strategy.getTyreById(lapStrat.pitStop.tyreChangeId).copy();
                }
                
                // Refuel
                if (lapStrat.pitStop.refuelAmount > 0) {
                    currentFuel = Math.min(currentFuel + lapStrat.pitStop.refuelAmount, 
                                          car.fuelTankCapacity);
                }
                
                entrySpeed = config.pitExitSpeed;
            }
            
            // Simulate each segment
            for (SegmentAction action : lapStrat.segments) {
                Segment seg = track.getSegment(action.segmentId);
                String weather = config.getWeatherAtTime(result.totalTime + lapTime);
                
                if (currentTyre.blownOut || currentFuel <= 0) {
                    // Limp mode
                    double limpTime = seg.length / car.limpSpeed;
                    lapTime += limpTime;
                    entrySpeed = car.limpSpeed;
                    continue;
                }
                
                if (seg.isStraight()) {
                    StraightResult sr = simulateStraight(seg, currentTyre, weather, 
                        entrySpeed, action.targetSpeed, action.brakeDistance, currentFuel);
                    lapTime += sr.time;
                    lapFuel += sr.fuelUsed;
                    currentTyre.currentDegradation += sr.tyreWear;
                    entrySpeed = sr.exitSpeed;
                    currentFuel -= sr.fuelUsed;
                    
                } else {  // Corner
                    CornerResult cr = simulateCorner(seg, currentTyre, weather, entrySpeed);
                    lapTime += cr.time;
                    currentTyre.currentDegradation += cr.tyreWear;
                    
                    if (cr.crashed) {
                        lapTime += config.crashPenalty;
                        result.crashes++;
                        entrySpeed = car.crawlSpeed;  // Crawl mode
                    } else {
                        entrySpeed = cr.exitSpeed;
                    }
                    
                    if (cr.blownOut) {
                        currentTyre.blownOut = true;
                        result.blowouts++;
                    }
                }
                
                // Check fuel
                if (currentFuel <= 0) {
                    currentFuel = 0;
                }
            }
            
            result.lapTimes[lap] = lapTime;
            result.totalTime += lapTime;
            result.totalFuelUsed += lapFuel;
        }
        
        result.finalTyreDegradation = currentTyre.currentDegradation;
        calculateScore(result);
        return result;
    }

    private StraightResult simulateStraight(Segment seg, Tyre tyre, String weather,
                                           double entrySpeed, double targetSpeed,
                                           double brakeDist, double fuelAvailable) {
        double length = seg.length;
        double accel = car.accel;  // Could apply weather multiplier
        double brake = car.brake;
        
        targetSpeed = Math.min(targetSpeed, car.maxSpeed);
        
        // Phase 1: Accelerate
        double accelDist = PhysicsEngine.distanceToAccelerate(entrySpeed, targetSpeed, accel);
        double accelTime = PhysicsEngine.timeToAccelerate(entrySpeed, targetSpeed, accel);
        
        // Adjust if we can't reach target before braking point
        double availableCruise = length - accelDist - brakeDist;
        if (availableCruise < 0) {
            // Never reach target - accelerate then brake immediately
            accelDist = length - brakeDist;
            double vMaxSq = entrySpeed * entrySpeed + 2 * accel * accelDist;
            double vMax = Math.sqrt(Math.max(0, vMaxSq));
            accelTime = PhysicsEngine.timeToAccelerate(entrySpeed, vMax, accel);
            targetSpeed = vMax;
            availableCruise = 0;
        }
        
        // Phase 2: Cruise
        double cruiseTime = PhysicsEngine.timeAtConstantSpeed(availableCruise, targetSpeed);
        
        // Phase 3: Brake
        double vExitSq = targetSpeed * targetSpeed - 2 * brake * brakeDist;
        double vExit = Math.sqrt(Math.max(0, vExitSq));
        double brakeTime = PhysicsEngine.timeToAccelerate(vExit, targetSpeed, brake);
        
        // Totals
        double totalTime = accelTime + cruiseTime + brakeTime;
        double fuelUsed = PhysicsEngine.calculateFuel(entrySpeed, vExit, length);
        
        // Tyre wear
        double rate = tyre.getDegradationRate(weather);
        double straightWear = PhysicsEngine.straightDegradation(rate, length);
        double brakeWear = PhysicsEngine.brakingDegradation(rate, targetSpeed, vExit);
        
        return new StraightResult(totalTime, vExit, fuelUsed, straightWear + brakeWear);
    }

    private CornerResult simulateCorner(Segment seg, Tyre tyre, String weather,
                                       double entrySpeed) {
        double friction = tyre.getFriction(weather);
        double maxSafe = PhysicsEngine.maxCornerSpeed(friction, seg.radius, car.crawlSpeed);
        
        boolean crashed = entrySpeed > maxSafe;
        double speed = crashed ? car.crawlSpeed : entrySpeed;
        
        double time = seg.length / speed;
        double rate = tyre.getDegradationRate(weather);
        double wear = PhysicsEngine.cornerDegradation(rate, speed, seg.radius);
        
        if (crashed) {
            wear += 0.1;  // Crash penalty
        }
        
        boolean blownOut = (tyre.currentDegradation + wear) >= tyre.lifeSpan;
        
        return new CornerResult(time, speed, wear, crashed, blownOut);
    }

    private double calculatePitTime(PitStop pit, double currentFuel) {
        double refuelTime = pit.refuelAmount / config.refuelRate;
        double tyreTime = pit.tyreChangeId > 0 ? config.tyreSwapTime : 0;
        return refuelTime + tyreTime + config.basePitTime;
    }

    private void calculateScore(RaceResult result) {
        // Level 1: Base score
        result.baseScore = 500000 * Math.pow(config.timeReference / result.totalTime, 3);
        
        // Level 2/3: Fuel bonus
        if (config.fuelSoftCap > 0) {
            double fuelRatio = Math.min(1.0, result.totalFuelUsed / config.fuelSoftCap);
            result.fuelBonus = 500000 * Math.pow(1 - fuelRatio, 2);
        }
        
        // Level 4: Tyre bonus (simplified)
        result.tyreBonus = 100000 * result.finalTyreDegradation - 50000 * result.blowouts;
        
        result.finalScore = result.baseScore + result.fuelBonus + result.tyreBonus;
    }

    // Inner result classes
    private static class StraightResult {
        final double time, exitSpeed, fuelUsed, tyreWear;
        StraightResult(double t, double e, double f, double w) {
            time = t; exitSpeed = e; fuelUsed = f; tyreWear = w;
        }
    }

    private static class CornerResult {
        final double time, exitSpeed, tyreWear;
        final boolean crashed, blownOut;
        CornerResult(double t, double e, double w, boolean c, boolean b) {
            time = t; exitSpeed = e; tyreWear = w; crashed = c; blownOut = b;
        }
    }
}