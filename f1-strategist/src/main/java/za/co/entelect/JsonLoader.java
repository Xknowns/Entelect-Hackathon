package za.co.entelect;

import org.json.*;
import java.util.*;

public class JsonLoader {
    
    public static RaceData loadLevel(String jsonString) {
        JSONObject root = new JSONObject(jsonString);
        
        Car car = parseCar(root.getJSONObject("car"));
        RaceConfig config = parseRace(root.getJSONObject("race"));
        Track track = parseTrack(root.getJSONObject("track"));
        List<Tyre> tyres = parseTyres(root.getJSONObject("tyres"));
        
        return new RaceData(car, config, track, tyres);
    }

    private static Car parseCar(JSONObject carJson) {
        return new Car(
            carJson.getDouble("max_speed_m/s"),
            carJson.getDouble("accel_m/se2"),
            carJson.getDouble("brake_m/se2"),
            carJson.getDouble("limp_constant_m/s"),
            carJson.getDouble("crawl_constant_m/s"),
            carJson.getDouble("fuel_tank_capacity_l"),
            carJson.getDouble("initial_fuel_l")
        );
    }

    private static RaceConfig parseRace(JSONObject raceJson) {
        List<RaceConfig.WeatherCondition> weatherList = new ArrayList<>();
        
        if (raceJson.has("weather")) {
            JSONObject weatherObj = raceJson.getJSONObject("weather");
            JSONArray conditions = weatherObj.getJSONArray("conditions");
            
            for (int i = 0; i < conditions.length(); i++) {
                JSONObject wc = conditions.getJSONObject(i);
                weatherList.add(new RaceConfig.WeatherCondition(
                    wc.getInt("id"),
                    wc.getString("condition"),
                    wc.getDouble("duration_s"),
                    wc.getDouble("acceleration_multiplier"),
                    wc.getDouble("deceleration_multiplier")
                ));
            }
        }
        
        return new RaceConfig(
            raceJson.getString("name"),
            raceJson.getInt("laps"),
            raceJson.getDouble("base_pit_stop_time_s"),
            raceJson.getDouble("pit_tyre_swap_time_s"),
            raceJson.getDouble("pit_refuel_rate_l/s"),
            raceJson.getDouble("corner_crash_penalty_s"),
            raceJson.getDouble("pit_exit_speed_m/s"),
            raceJson.optDouble("fuel_soft_cap_limit_l", 999999),
            raceJson.optDouble("time_reference_s", 7300),
            raceJson.optInt("starting_weather_condition_id", 1),
            weatherList
        );
    }

    private static Track parseTrack(JSONObject trackJson) {
        String name = trackJson.getString("name");
        JSONArray segs = trackJson.getJSONArray("segments");
        List<Segment> segments = new ArrayList<>();
        
        for (int i = 0; i < segs.length(); i++) {
            JSONObject s = segs.getJSONObject(i);
            double radius = s.has("radius_m") ? s.getDouble("radius_m") : 0;
            segments.add(new Segment(
                s.getInt("id"),
                s.getString("type"),
                s.getDouble("length_m"),
                radius
            ));
        }
        
        return new Track(name, segments);
    }

    private static List<Tyre> parseTyres(JSONObject tyresJson) {
        List<Tyre> tyres = new ArrayList<>();
        JSONObject props = tyresJson.getJSONObject("properties");
        JSONArray available = tyresJson.getJSONArray("available_sets");
        
        // Base friction values from PDF
        Map<String, Double> baseFriction = Map.of(
            "Soft", 1.8, "Medium", 1.7, "Hard", 1.6,
            "Intermediate", 1.2, "Wet", 1.1
        );
        
        for (int i = 0; i < available.length(); i++) {
            JSONObject set = available.getJSONObject(i);
            String compound = set.getString("compound");
            JSONArray ids = set.getJSONArray("ids");
            JSONObject p = props.getJSONObject(compound);
            
            for (int j = 0; j < ids.length(); j++) {
                tyres.add(new Tyre(
                    ids.getInt(j),
                    compound,
                    baseFriction.getOrDefault(compound, 1.0),
                    p.getDouble("life_span"),
                    p.getDouble("dry_friction_multiplier"),
                    p.getDouble("cold_friction_multiplier"),
                    p.getDouble("light_rain_friction_multiplier"),
                    p.getDouble("heavy_rain_friction_multiplier"),
                    p.getDouble("dry_degradation"),
                    p.getDouble("cold_degradation"),
                    p.getDouble("light_rain_degradation"),
                    p.getDouble("heavy_rain_degradation")
                ));
            }
        }
        
        return tyres;
    }

    public static class RaceData {
        public final Car car;
        public final RaceConfig config;
        public final Track track;
        public final List<Tyre> tyres;

        public RaceData(Car car, RaceConfig config, Track track, List<Tyre> tyres) {
            this.car = car;
            this.config = config;
            this.track = track;
            this.tyres = tyres;
        }
    }
}