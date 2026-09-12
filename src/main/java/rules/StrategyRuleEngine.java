package rules;

public class StrategyRuleEngine {

    public StrategyDecision decide(
            String trackCondition,
            String raceType,
            boolean highSkillDriver,
            boolean longRace,
            double temperature,
            int laps
    ) {

        int softScore = 0;
        int mediumScore = 0;
        int hardScore = 0;
        int wetScore = 0;

        // Inferred race class from HermiT.
        if ("WetRace".equals(raceType)) {
            wetScore += 5;
            softScore -= 2;
            mediumScore -= 1;
        }

        if ("DryRace".equals(raceType)) {
            hardScore += 2;
            mediumScore += 1;
        }

        // Inferred driver class from HermiT.
        // A highly skilled driver can support a more aggressive tyre choice.
        if (highSkillDriver) {
            softScore += 2;
            mediumScore += 1;
        }

        // Inferred race class from HermiT.
        // Long races favour durability.
        if (longRace) {
            hardScore += 3;
            mediumScore += 1;
            softScore -= 1;
        }

        if ("WetTrack".equals(trackCondition)) {
            wetScore += 5;
            hardScore -= 2;
            softScore -= 2;
        }

        if ("MixedTrack".equals(trackCondition)) {
            mediumScore += 3;
            wetScore += 2;
        }

        if ("DryTrack".equals(trackCondition)) {
            softScore += 2;
            mediumScore += 2;
            hardScore += 1;
        }

        if (temperature >= 30) {
            hardScore += 4;
            mediumScore += 1;
            softScore -= 2;
        } else if (temperature >= 22) {
            mediumScore += 3;
            hardScore += 1;
        } else {
            softScore += 3;
            mediumScore += 1;
        }

        if (laps >= 60) {
            hardScore += 4;
            mediumScore += 1;
            softScore -= 1;
        } else if (laps >= 50) {
            mediumScore += 2;
            hardScore += 2;
        } else {
            softScore += 2;
            mediumScore += 1;
        }

        System.out.println(
                "Decision using HermiT inferences -> "
                        + "RaceType=" + raceType
                        + ", HighSkillDriver=" + highSkillDriver
                        + ", LongRace=" + longRace
                        + " | Soft=" + softScore
                        + ", Medium=" + mediumScore
                        + ", Hard=" + hardScore
                        + ", Wet=" + wetScore
        );

        if (wetScore >= softScore
                && wetScore >= mediumScore
                && wetScore >= hardScore) {

            return new StrategyDecision(
                    "WetTyre",
                    2,
                    "Wet race inference gives wet tyres the highest strategy score."
            );
        }

        if (hardScore >= mediumScore
                && hardScore >= softScore) {

            return new StrategyDecision(
                    "HardTyre",
                    2,
                    "Durability factors give hard tyres the highest strategy score."
            );
        }

        if (softScore >= mediumScore) {

            return new StrategyDecision(
                    "SoftTyre",
                    2,
                    "Pace-related factors give soft tyres the highest strategy score."
            );
        }

        return new StrategyDecision(
                "MediumTyre",
                1,
                "Medium tyres provide the highest balanced strategy score."
        );
    }
}