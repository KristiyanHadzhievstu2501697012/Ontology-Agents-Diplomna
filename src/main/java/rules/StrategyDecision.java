package rules;

public class StrategyDecision {

    private final String tyre;
    private final int pitStops;
    private final String reason;

    public StrategyDecision(String tyre, int pitStops, String reason) {
        this.tyre = tyre;
        this.pitStops = pitStops;
        this.reason = reason;
    }

    public String getTyre() {
        return tyre;
    }

    public int getPitStops() {
        return pitStops;
    }

    public String getReason() {
        return reason;
    }
}