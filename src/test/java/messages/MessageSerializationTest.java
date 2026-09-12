package messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageSerializationTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void tyreDecisionRequestShouldSerializeAndDeserialize() throws Exception {

        TyreDecisionRequest request =
                new TyreDecisionRequest();

        request.driver = "Verstappen";
        request.tyreWear = 65.0;
        request.gripLevel = 72.0;
        request.rainProbability = 15.0;
        request.currentTyre = "MediumTyre";
        request.fuelLevel = 55.0;
        request.currentLap = 20;
        request.totalLaps = 53;
        request.drivingStyle = "Aggressive";

        String json =
                objectMapper.writeValueAsString(request);

        TyreDecisionRequest restored =
                objectMapper.readValue(
                        json,
                        TyreDecisionRequest.class
                );

        assertEquals("Verstappen", restored.driver);
        assertEquals(65.0, restored.tyreWear);
        assertEquals("Aggressive", restored.drivingStyle);
        assertEquals(20, restored.currentLap);
    }

    @Test
    void tyreDecisionProposalShouldSerializeAndDeserialize() throws Exception {

        TyreDecisionProposal proposal =
                new TyreDecisionProposal();

        proposal.driver = "Leclerc";
        proposal.recommendedTyre = "WetTyre";
        proposal.pitRequired = true;
        proposal.reason = "Rain probability is high.";
        proposal.drivingStyle = "Balanced";

        String json =
                objectMapper.writeValueAsString(proposal);

        TyreDecisionProposal restored =
                objectMapper.readValue(
                        json,
                        TyreDecisionProposal.class
                );

        assertEquals("Leclerc", restored.driver);
        assertEquals("WetTyre", restored.recommendedTyre);
        assertTrue(restored.pitRequired);
        assertEquals("Balanced", restored.drivingStyle);
    }

    @Test
    void tyreDecisionResponseShouldSerializeAndDeserialize() throws Exception {

        TyreDecisionResponse response =
                new TyreDecisionResponse();

        response.driver = "Norris";
        response.accepted = true;
        response.newTyre = "HardTyre";
        response.reason = "Proposal accepted.";
        response.drivingStyle = "Balanced";

        String json =
                objectMapper.writeValueAsString(response);

        TyreDecisionResponse restored =
                objectMapper.readValue(
                        json,
                        TyreDecisionResponse.class
                );

        assertEquals("Norris", restored.driver);
        assertTrue(restored.accepted);
        assertEquals("HardTyre", restored.newTyre);
        assertEquals("Balanced", restored.drivingStyle);
    }
}