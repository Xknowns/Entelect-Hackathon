package za.co.entelect;

public class PhysicsEngine {
    // Constants
    public static final double GRAVITY = 9.8;
    public static final double K_STRAIGHT = 0.0000166;
    public static final double K_BRAKING = 0.0398;
    public static final double K_CORNER = 0.000265;

    // Time to accelerate from v0 to v1
    public static double timeToAccelerate(double v0, double v1, double accel) {
        if (v1 <= v0) return 0;
        return (v1 - v0) / accel;
    }

    // Distance covered while accelerating
    public static double distanceToAccelerate(double v0, double v1, double accel) {
        if (v1 <= v0) return 0;
        return (v1 * v1 - v0 * v0) / (2 * accel);
    }

    // Time at constant speed
    public static double timeAtConstantSpeed(double distance, double speed) {
        if (speed <= 0) return Double.POSITIVE_INFINITY;
        return distance / speed;
    }

    // Max safe corner speed
    public static double maxCornerSpeed(double tyreFriction, double radius, double crawlSpeed) {
        return Math.sqrt(tyreFriction * GRAVITY * radius) + crawlSpeed;
    }

    // Fuel calculation
    public static double calculateFuel(double v0, double v1, double distance) {
        double avgSpeed = (v0 + v1) / 2.0;
        return (Car.K_BASE + Car.K_DRAG * avgSpeed * avgSpeed) * distance;
    }

    // Tyre degradation on straight
    public static double straightDegradation(double rate, double length) {
        return rate * length * K_STRAIGHT;
    }

    // Tyre degradation from braking
    public static double brakingDegradation(double rate, double v0, double v1) {
        return (Math.pow(v0 / 100, 2) - Math.pow(v1 / 100, 2)) * K_BRAKING * rate;
    }

    // Tyre degradation in corner
    public static double cornerDegradation(double rate, double speed, double radius) {
        return K_CORNER * (speed * speed / radius) * rate;
    }
}