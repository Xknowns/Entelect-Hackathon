package za.co.entelect;

public class Car {
    public final double maxSpeed;
    public final double accel;
    public final double brake;
    public final double limpSpeed;
    public final double crawlSpeed;
    public final double fuelTankCapacity;
    public final double initialFuel;
    
    // Fuel constants
    public static final double K_BASE = 0.0005;
    public static final double K_DRAG = 0.0000000015;

    public Car(double maxSpeed, double accel, double brake, 
               double limpSpeed, double crawlSpeed, 
               double fuelTankCapacity, double initialFuel) {
        this.maxSpeed = maxSpeed;
        this.accel = accel;
        this.brake = brake;
        this.limpSpeed = limpSpeed;
        this.crawlSpeed = crawlSpeed;
        this.fuelTankCapacity = fuelTankCapacity;
        this.initialFuel = initialFuel;
    }
}