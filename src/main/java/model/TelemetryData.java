package model;

public class TelemetryData {

    private double tyreTemperature;
    private double airTemperature;
    private double trackTemperature;

    private double fuelLevel;
    private double gripLevel;

    private double tyreWear;

    public TelemetryData() {
    }

    public double getTyreTemperature() {
        return tyreTemperature;
    }

    public void setTyreTemperature(double tyreTemperature) {
        this.tyreTemperature = tyreTemperature;
    }

    public double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public double getTrackTemperature() {
        return trackTemperature;
    }

    public void setTrackTemperature(double trackTemperature) {
        this.trackTemperature = trackTemperature;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public double getGripLevel() {
        return gripLevel;
    }

    public void setGripLevel(double gripLevel) {
        this.gripLevel = gripLevel;
    }

    public double getTyreWear() {
        return tyreWear;
    }

    public void setTyreWear(double tyreWear) {
        this.tyreWear = tyreWear;
    }
}