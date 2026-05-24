package za.co.entelect;

import java.util.List;

public class Track {
    public final String name;
    public final List<Segment> segments;

    public Track(String name, List<Segment> segments) {
        this.name = name;
        this.segments = segments;
    }

    public Segment getSegment(int id) {
        return segments.stream()
            .filter(s -> s.id == id)
            .findFirst()
            .orElse(null);
    }
}