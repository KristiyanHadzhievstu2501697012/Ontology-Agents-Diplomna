package model;

public class ExperimentResult {

    private final String race;
    private final long seed;
    private final String driver;
    private final double totalRaceTime;
    private final double fastestLap;
    private final int pitStops;
    private final String finalTyre;

    public ExperimentResult(
            String race,
            long seed,
            String driver,
            double totalRaceTime,
            double fastestLap,
            int pitStops,
            String finalTyre
    ) {
        this.race = race;
        this.seed = seed;
        this.driver = driver;
        this.totalRaceTime = totalRaceTime;
        this.fastestLap = fastestLap;
        this.pitStops = pitStops;
        this.finalTyre = finalTyre;
    }

    public String getRace() {
        return race;
    }

    public long getSeed() {
        return seed;
    }

    public String getDriver() {
        return driver;
    }

    public double getTotalRaceTime() {
        return totalRaceTime;
    }

    public double getFastestLap() {
        return fastestLap;
    }

    public int getPitStops() {
        return pitStops;
    }

    public String getFinalTyre() {
        return finalTyre;
    }
}