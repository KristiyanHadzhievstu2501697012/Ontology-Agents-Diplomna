package rules;

import ontology.OntologyManager;

public class TyreRules {

    private final double intermediateThreshold;
    private final double wetThreshold;
    private final double wearThreshold;
    private final double criticalGripThreshold;

    public TyreRules(
            OntologyManager ontologyManager,
            String drivingStyle
    ) {

        this.intermediateThreshold =
                ontologyManager.getDrivingStyleThreshold(
                        drivingStyle,
                        "intermediateThreshold"
                );

        this.wetThreshold =
                ontologyManager.getDrivingStyleThreshold(
                        drivingStyle,
                        "wetThreshold"
                );

        this.wearThreshold =
                ontologyManager.getDrivingStyleThreshold(
                        drivingStyle,
                        "wearThreshold"
                );

        this.criticalGripThreshold =
                ontologyManager.getDrivingStyleThreshold(
                        drivingStyle,
                        "criticalGripThreshold"
                );
    }

    public double getIntermediateThreshold() {
        return intermediateThreshold;
    }

    public double getWetThreshold() {
        return wetThreshold;
    }

    public double getWearThreshold() {
        return wearThreshold;
    }

    public double getCriticalGripThreshold() {
        return criticalGripThreshold;
    }
}