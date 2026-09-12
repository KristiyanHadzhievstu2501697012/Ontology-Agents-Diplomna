package model;

public class DriverResult {

    private String driver;
    private String team;
    private String car;

    private double totalRaceTime;
    private double fastestLap;
    private double lastLap;
    private double tyreWear = 0.0;
    private double gripLevel = 100.0;
    private double fuelLevel = 100.0;

    private int completedLaps;
    private int pitStops;
    private int weatherMismatchLaps = 0;

    private String currentTyre;

    private boolean finished;

    public DriverResult(String driver, String team, String car) {
        this.driver = driver;
        this.team = team;
        this.car = car;

        this.totalRaceTime = 0.0;
        this.fastestLap = Double.MAX_VALUE;
        this.lastLap = 0.0;
        this.completedLaps = 0;
        this.pitStops = 0;
        this.currentTyre = "MediumTyre";
        this.finished = false;
    }

    public String getDriver() {
        return driver;
    }

    public String getTeam() {
        return team;
    }

    public String getCar() {
        return car;
    }

    public double getTotalRaceTime() {
        return totalRaceTime;
    }

    public void addLapTime(double lapTime) {
        totalRaceTime += lapTime;
        lastLap = lapTime;
        completedLaps++;

        if (lapTime < fastestLap) {
            fastestLap = lapTime;
        }
    }

    public double getFastestLap() {
        return fastestLap;
    }

    public double getLastLap() {
        return lastLap;
    }

    public int getCompletedLaps() {
        return completedLaps;
    }

    public int getPitStops() {
        return pitStops;
    }

    public void addPitStop() {
        pitStops++;
    }

    public double getTyreWear() {
        return tyreWear;
    }

    public void setTyreWear(double tyreWear) {
        this.tyreWear = tyreWear;
    }

    public double getGripLevel() {
        return gripLevel;
    }

    public void setGripLevel(double gripLevel) {
        this.gripLevel = gripLevel;
    }


    public String getCurrentTyre() {
        return currentTyre;
    }

    public void setCurrentTyre(String currentTyre) {
        this.currentTyre = currentTyre;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getWeatherMismatchLaps() {
        return weatherMismatchLaps;
    }

    public void setWeatherMismatchLaps(int weatherMismatchLaps) {
        this.weatherMismatchLaps = weatherMismatchLaps;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = Math.max(0.0, fuelLevel);
    }

}