package za.co.entelect;

public class Segment {
    public final int id;
    public final String type;  // "straight" or "corner"
    public final double length;
    public final double radius;  // only for corners

    public Segment(int id, String type, double length, double radius) {
        this.id = id;
        this.type = type;
        this.length = length;
        this.radius = radius;
    }

    public boolean isStraight() {
        return "straight".equals(type);
    }

    public boolean isCorner() {
        return "corner".equals(type);
    }
}