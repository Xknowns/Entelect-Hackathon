package za.co.entelect;

public class RaceResult {
    public double totalTime = 0;
    public double totalFuelUsed = 0;
    public int crashes = 0;
    public int blowouts = 0;
    public boolean finished = true;
    public double finalTyreDegradation = 0;
    public double[] lapTimes;
    
    // Scoring
    public double baseScore = 0;
    public double fuelBonus = 0;
    public double tyreBonus = 0;
    public double finalScore = 0;

    public RaceResult(int numLaps) {
        this.lapTimes = new double[numLaps];
    }
}