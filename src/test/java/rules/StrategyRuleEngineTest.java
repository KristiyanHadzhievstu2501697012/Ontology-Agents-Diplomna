package rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategyRuleEngineTest {

    private final StrategyRuleEngine ruleEngine =
            new StrategyRuleEngine();

    @Test
    void wetRaceAndWetTrackShouldChooseWetTyre() {

        StrategyDecision decision =
                ruleEngine.decide(
                        "WetTrack",
                        "WetRace",
                        false,
                        false,
                        20.0,
                        40
                );

        assertEquals(
                "WetTyre",
                decision.getTyre()
        );

        assertEquals(
                2,
                decision.getPitStops()
        );
    }

    @Test
    void dryHotRaceShouldChooseHardTyre() {

        StrategyDecision decision =
                ruleEngine.decide(
                        "DryTrack",
                        "DryRace",
                        true,
                        false,
                        30.0,
                        53
                );

        assertEquals(
                "HardTyre",
                decision.getTyre()
        );
    }

    @Test
    void shortCoolDryRaceShouldChooseSoftTyre() {

        StrategyDecision decision =
                ruleEngine.decide(
                        "DryTrack",
                        "UnknownRace",
                        false,
                        false,
                        18.0,
                        40
                );

        assertEquals(
                "SoftTyre",
                decision.getTyre()
        );
    }

    @Test
    void mixedTrackShouldChooseMediumTyre() {

        StrategyDecision decision =
                ruleEngine.decide(
                        "MixedTrack",
                        "UnknownRace",
                        false,
                        false,
                        25.0,
                        55
                );

        assertEquals(
                "MediumTyre",
                decision.getTyre()
        );

        assertEquals(
                1,
                decision.getPitStops()
        );
    }
}