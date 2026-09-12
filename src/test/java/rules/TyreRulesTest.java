package rules;

import ontology.OntologyManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TyreRulesTest {

    private static OntologyManager ontologyManager;

    @BeforeAll
    static void setUp() {
        ontologyManager =
                new OntologyManager();
    }

    @Test
    void aggressiveStyleShouldLoadOntologyThresholds() {

        TyreRules rules =
                new TyreRules(
                        ontologyManager,
                        "Aggressive"
                );

        assertEquals(
                45.0,
                rules.getIntermediateThreshold()
        );

        assertEquals(
                75.0,
                rules.getWetThreshold()
        );

        assertEquals(
                78.0,
                rules.getWearThreshold()
        );

        assertEquals(
                22.0,
                rules.getCriticalGripThreshold()
        );
    }

    @Test
    void balancedStyleShouldLoadOntologyThresholds() {

        TyreRules rules =
                new TyreRules(
                        ontologyManager,
                        "Balanced"
                );

        assertEquals(
                40.0,
                rules.getIntermediateThreshold()
        );

        assertEquals(
                70.0,
                rules.getWetThreshold()
        );

        assertEquals(
                70.0,
                rules.getWearThreshold()
        );

        assertEquals(
                30.0,
                rules.getCriticalGripThreshold()
        );
    }

    @Test
    void conservativeStyleShouldLoadOntologyThresholds() {

        TyreRules rules =
                new TyreRules(
                        ontologyManager,
                        "Conservative"
                );

        assertEquals(
                35.0,
                rules.getIntermediateThreshold()
        );

        assertEquals(
                65.0,
                rules.getWetThreshold()
        );

        assertEquals(
                62.0,
                rules.getWearThreshold()
        );

        assertEquals(
                38.0,
                rules.getCriticalGripThreshold()
        );
    }
}