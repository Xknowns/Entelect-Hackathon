package za.co.entelect;

import java.util.*;

public class Optimizer {
    private final Car car;
    private final Track track;
    private final RaceConfig config;
    private final List<Tyre> tyres;
    private final Simulator simulator;

    public Optimizer(JsonLoader.RaceData data) {
        this.car = data.car;
        this.track = data.track;
        this.config = data.config;
        this.tyres = data.tyres;
        this.simulator = new Simulator(car, track, config);
    }

    public Strategy optimize() {
        // Start with a greedy baseline strategy
        Strategy baseline = createGreedyStrategy();
        RaceResult baselineResult = simulator.simulateRace(baseline);
        
        System.out.println("Baseline time: " + baselineResult.totalTime);
        System.out.println("Baseline score: " + baselineResult.finalScore);
        
        // Try to improve with local search
        Strategy best = baseline;
        double bestScore = baselineResult.finalScore;
        
        // Try different tyre combinations
        for (Tyre startingTyre : getCandidateTyres()) {
            Strategy candidate = createSmartStrategy(startingTyre);
            RaceResult result = simulator.simulateRace(candidate);
            
            if (result.finalScore > bestScore) {
                best = candidate;
                bestScore = result.finalScore;
                System.out.println("Improved score: " + bestScore);
            }
        }
        
        return best;
    }

    private Strategy createGreedyStrategy() {
        // Start with Soft tyres (fastest, but wear quickly)
        Tyre startTyre = tyres.stream()
            .filter(t -> t.compound.equals("Soft"))
            .findFirst()
            .orElse(tyres.get(0));
        
        Strategy strategy = new Strategy(startTyre, tyres);
        
        for (int lap = 1; lap <= config.laps; lap++) {
            LapStrategy lapStrat = new LapStrategy(lap);
            
            // Build segment actions
            for (Segment seg : track.segments) {
                if (seg.isStraight()) {
                    // Greedy: max speed, brake at 80% of straight
                    double target = car.maxSpeed;
                    double brakeDist = seg.length * 0.2;  // Brake last 20%
                    lapStrat.segments.add(new SegmentAction(seg.id, target, brakeDist));
                } else {
                    lapStrat.segments.add(new SegmentAction(seg.id, 0, 0));
                }
            }
            
            // Pit on middle lap if multiple laps
            if (config.laps > 1 && lap == config.laps / 2) {
            	String weather = config.getWeatherAtTime(lap * 1000); // or however you calculate time
            	lapStrat.pitStop = new PitStop(true, getBestTyreForConditions(weather).id, 50);
            } else {
                lapStrat.pitStop = new PitStop(false, 0, 0);
            }
            
            strategy.laps.add(lapStrat);
        }
        
        return strategy;
    }

    private Strategy createSmartStrategy(Tyre startTyre) {
        Strategy strategy = new Strategy(startTyre, tyres);
        
        for (int lap = 1; lap <= config.laps; lap++) {
            LapStrategy lapStrat = new LapStrategy(lap);
            String expectedWeather = config.getWeatherAtTime(lap * config.timeReference / config.laps);
            
            for (Segment seg : track.segments) {
                if (seg.isStraight()) {
                    double target = calculateOptimalSpeed(seg, expectedWeather, lap);
                    double brakeDist = calculateOptimalBraking(seg, target);
                    lapStrat.segments.add(new SegmentAction(seg.id, target, brakeDist));
                } else {
                    lapStrat.segments.add(new SegmentAction(seg.id, 0, 0));
                }
            }
            
            // Smart pit strategy
            lapStrat.pitStop = calculatePitStop(lap, expectedWeather);
            strategy.laps.add(lapStrat);
        }
        
        return strategy;
    }

    private double calculateOptimalSpeed(Segment seg, String weather, int lap) {
        // Conservative: slightly below max to save tyres/fuel
        return car.maxSpeed * 0.95;
    }

    private double calculateOptimalBraking(Segment seg, double targetSpeed) {
        // Brake enough to hit next corner at safe speed
        // This needs the next segment info - simplified here
        return seg.length * 0.25;
    }

    private PitStop calculatePitStop(int lap, String weather) {
        // Don't pit on last lap
        if (lap >= config.laps) return new PitStop(false, 0, 0);
        
        // Pit if weather changes or mid-race
        if (lap == config.laps / 2) {
            Tyre best = getBestTyreForConditions(weather);
            return new PitStop(true, best.id, 40);
        }
        
        return new PitStop(false, 0, 0);
    }

    private Tyre getBestTyreForConditions(String weather) {
        return switch (weather) {
            case "heavy_rain", "light_rain" -> 
                tyres.stream().filter(t -> t.compound.equals("Wet")).findFirst().orElse(tyres.get(0));
            case "cold" -> 
                tyres.stream().filter(t -> t.compound.equals("Soft")).findFirst().orElse(tyres.get(0));
            default -> // dry
                tyres.stream().filter(t -> t.compound.equals("Soft")).findFirst().orElse(tyres.get(0));
        };
    }

    private List<Tyre> getCandidateTyres() {
        // Try starting with different compounds
        List<Tyre> candidates = new ArrayList<>();
        for (String compound : List.of("Soft", "Medium", "Hard")) {
            tyres.stream()
                .filter(t -> t.compound.equals(compound))
                .findFirst()
                .ifPresent(candidates::add);
        }
        return candidates;
    }
}