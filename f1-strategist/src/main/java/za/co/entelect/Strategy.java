package za.co.entelect;

import java.util.ArrayList;
import java.util.List;

public class Strategy {
    public Tyre initialTyre;
    public List<LapStrategy> laps;
    public List<Tyre> availableTyres;

    public Strategy(Tyre initialTyre, List<Tyre> availableTyres) {
        this.initialTyre = initialTyre;
        this.availableTyres = availableTyres;
        this.laps = new ArrayList<>();
    }

    public Tyre getTyreById(int id) {
        return availableTyres.stream()
            .filter(t -> t.id == id)
            .findFirst()
            .orElse(null);
    }
}

class LapStrategy {
    public int lapNumber;
    public List<SegmentAction> segments;
    public PitStop pitStop;

    public LapStrategy(int lapNumber) {
        this.lapNumber = lapNumber;
        this.segments = new ArrayList<>();
    }
}

class SegmentAction {
    public int segmentId;
    public double targetSpeed;      // For straights
    public double brakeDistance;    // For straights
    // Corners don't need these - speed is determined by entry

    public SegmentAction(int segmentId, double targetSpeed, double brakeDistance) {
        this.segmentId = segmentId;
        this.targetSpeed = targetSpeed;
        this.brakeDistance = brakeDistance;
    }
}

class PitStop {
    public boolean enter;
    public int tyreChangeId;
    public double refuelAmount;

    public PitStop(boolean enter, int tyreChangeId, double refuelAmount) {
        this.enter = enter;
        this.tyreChangeId = tyreChangeId;
        this.refuelAmount = refuelAmount;
    }
}