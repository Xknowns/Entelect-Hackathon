package za.co.entelect;

import java.util.List;
import java.util.Map;

public class RaceConfig {
    public final String name;
    public final int laps;
    public final double basePitTime;
    public final double tyreSwapTime;
    public final double refuelRate;
    public final double crashPenalty;
    public final double pitExitSpeed;
    public final double fuelSoftCap;
    public final double timeReference;
    public final int startingWeatherId;
    public final List<WeatherCondition> weatherConditions;

    public RaceConfig(String name, int laps, double basePitTime, 
                      double tyreSwapTime, double refuelRate,
                      double crashPenalty, double pitExitSpeed,
                      double fuelSoftCap, double timeReference,
                      int startingWeatherId, List<WeatherCondition> weatherConditions) {
        this.name = name;
        this.laps = laps;
        this.basePitTime = basePitTime;
        this.tyreSwapTime = tyreSwapTime;
        this.refuelRate = refuelRate;
        this.crashPenalty = crashPenalty;
        this.pitExitSpeed = pitExitSpeed;
        this.fuelSoftCap = fuelSoftCap;
        this.timeReference = timeReference;
        this.startingWeatherId = startingWeatherId;
        this.weatherConditions = weatherConditions;
    }

    public String getWeatherAtTime(double raceTime) {
        // Cycle through weather conditions based on duration
        if (weatherConditions.isEmpty()) return "dry";
        
        double totalDuration = weatherConditions.stream()
            .mapToDouble(w -> w.duration)
            .sum();
        
        double adjustedTime = raceTime % totalDuration;
        double currentTime = 0;
        
        for (WeatherCondition wc : weatherConditions) {
            currentTime += wc.duration;
            if (adjustedTime <= currentTime) {
                return wc.condition;
            }
        }
        
        return weatherConditions.get(weatherConditions.size() - 1).condition;
    }

    public static class WeatherCondition {
        public final int id;
        public final String condition;
        public final double duration;
        public final double accelMult;
        public final double brakeMult;

        public WeatherCondition(int id, String condition, double duration,
                               double accelMult, double brakeMult) {
            this.id = id;
            this.condition = condition;
            this.duration = duration;
            this.accelMult = accelMult;
            this.brakeMult = brakeMult;
        }
    }
}