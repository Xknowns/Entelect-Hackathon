package za.co.entelect;

public class Tyre {
    public final int id;
    public final String compound;
    public final double baseFriction;
    public final double lifeSpan;
    
    // Weather multipliers
    public final double dryMult, coldMult, lightRainMult, heavyRainMult;
    // Degradation rates
    public final double dryDeg, coldDeg, lightRainDeg, heavyRainDeg;
    
    // Current state (changes during race)
    public double currentDegradation;
    public boolean blownOut;

    public Tyre(int id, String compound, double baseFriction, double lifeSpan,
                double dryMult, double coldMult, double lightRainMult, double heavyRainMult,
                double dryDeg, double coldDeg, double lightRainDeg, double heavyRainDeg) {
        this.id = id;
        this.compound = compound;
        this.baseFriction = baseFriction;
        this.lifeSpan = lifeSpan;
        this.dryMult = dryMult;
        this.coldMult = coldMult;
        this.lightRainMult = lightRainMult;
        this.heavyRainMult = heavyRainMult;
        this.dryDeg = dryDeg;
        this.coldDeg = coldDeg;
        this.lightRainDeg = lightRainDeg;
        this.heavyRainDeg = heavyRainDeg;
        this.currentDegradation = 0;
        this.blownOut = false;
    }

    public double getFriction(String weather) {
        double mult = switch (weather) {
            case "dry" -> dryMult;
            case "cold" -> coldMult;
            case "light_rain" -> lightRainMult;
            case "heavy_rain" -> heavyRainMult;
            default -> 1.0;
        };
        return (baseFriction - currentDegradation) * mult;
    }

    public double getDegradationRate(String weather) {
        return switch (weather) {
            case "dry" -> dryDeg;
            case "cold" -> coldDeg;
            case "light_rain" -> lightRainDeg;
            case "heavy_rain" -> heavyRainDeg;
            default -> 0.1;
        };
    }

    public Tyre copy() {
        Tyre copy = new Tyre(id, compound, baseFriction, lifeSpan,
            dryMult, coldMult, lightRainMult, heavyRainMult,
            dryDeg, coldDeg, lightRainDeg, heavyRainDeg);
        copy.currentDegradation = this.currentDegradation;
        copy.blownOut = this.blownOut;
        return copy;
    }
}