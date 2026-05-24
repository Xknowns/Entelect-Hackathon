package za.co.entelect;

import org.json.*;

public class JsonOutput {
    
    public static String generateSubmission(Strategy strategy, RaceConfig config) {
        JSONObject submission = new JSONObject();
        submission.put("initial_tyre_id", strategy.initialTyre.id);
        
        JSONArray laps = new JSONArray();
        for (LapStrategy lap : strategy.laps) {
            JSONObject lapObj = new JSONObject();
            lapObj.put("lap", lap.lapNumber);
            
            JSONArray segments = new JSONArray();
            for (SegmentAction action : lap.segments) {
                JSONObject segObj = new JSONObject();
                segObj.put("id", action.segmentId);
                
                boolean isStraight = action.targetSpeed > 0;
                if (isStraight) {
                    segObj.put("type", "straight");
                    segObj.put("target_m/s", action.targetSpeed);
                    segObj.put("brake_start_m_before_next", action.brakeDistance);
                } else {
                    segObj.put("type", "corner");
                }
                segments.put(segObj);
            }
            lapObj.put("segments", segments);
            
            if (lap.pitStop != null && lap.pitStop.enter) {
                JSONObject pit = new JSONObject();
                pit.put("enter", true);
                pit.put("tyre_change_set_id", lap.pitStop.tyreChangeId);
                pit.put("fuel_refuel_amount_l", lap.pitStop.refuelAmount);
                lapObj.put("pit", pit);
            } else {
                lapObj.put("pit", new JSONObject().put("enter", false));
            }
            
            laps.put(lapObj);
        }
        submission.put("laps", laps);
        return submission.toString(2);
    }
}