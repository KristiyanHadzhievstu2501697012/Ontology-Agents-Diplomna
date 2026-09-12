package ontology;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OntologyManagerTest {

    private static OntologyManager ontologyManager;

    @BeforeAll
    static void setUp() {
        ontologyManager = new OntologyManager();
    }

    @Test
    void race1ShouldReadAirTemperatureFromOntology() {
        assertEquals(
                30.0,
                ontologyManager.getAirTemperature("Race1")
        );
    }

    @Test
    void race1ShouldReadLapsFromOntology() {
        assertEquals(
                53,
                ontologyManager.getLaps("Race1")
        );
    }

    @Test
    void race1ShouldUseMonacoCircuit() {
        assertEquals(
                "Monaco",
                ontologyManager.getCircuit("Race1")
        );
    }

    @Test
    void race1ShouldUseVerstappenAsFocusDriver() {
        assertEquals(
                "Verstappen",
                ontologyManager.getDriver("Race1")
        );
    }

    @Test
    void verstappenShouldHaveAggressiveDrivingStyle() {
        assertEquals(
                "Aggressive",
                ontologyManager.getDrivingStyle("Verstappen")
        );
    }

    @Test
    void race1ShouldBeInferredAsDryRace() {
        assertEquals(
                "DryRace",
                ontologyManager.getInferredRaceType("Race1")
        );
    }

    @Test
    void race2ShouldBeInferredAsWetRace() {
        assertEquals(
                "WetRace",
                ontologyManager.getInferredRaceType("Race2")
        );
    }

    @Test
    void race1ShouldContainTwentyParticipants() {
        assertEquals(
                20,
                ontologyManager.getParticipants("Race1").size()
        );
    }
}