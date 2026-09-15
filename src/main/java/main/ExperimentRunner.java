package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import model.ExperimentResult;
import simulation.ExperimentMode;
import simulation.ExperimentResultCollector;
import simulation.SimulationRandom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExperimentRunner {

    private static final String RESULTS_FILE = "results.csv";

    public static void main(String[] args) {

        if (args.length > 0) {

            if ("BATCH".equalsIgnoreCase(args[0])) {
                runBaselineBatch();
                return;
            }

            if ("STYLE_TEST".equalsIgnoreCase(args[0])) {
                runStyleTest();
                return;
            }

            if ("STYLE_BATCH".equalsIgnoreCase(args[0])) {
                runStyleBatch();
                return;
            }

            if ("ALL_BATCH".equalsIgnoreCase(args[0])) {
                resetResultsFile();
                runBaselineBatch(false);
                runStyleBatch(false);
                return;
            }
        }

        String raceName = "Race1";
        long seed = 1L;
        ExperimentMode mode = ExperimentMode.AGENTS;
        String style = "ONTOLOGY";

        if (args.length > 0) {
            raceName = args[0];
        }

        if (args.length > 1) {
            seed = Long.parseLong(args[1]);
        }

        if (args.length > 2) {
            mode = ExperimentMode.valueOf(
                    args[2].toUpperCase()
            );
        }

        if (args.length > 3) {
            style = args[3];
        }

        ExperimentResult result =
                runExperiment(
                        raceName,
                        seed,
                        mode,
                        style
                );

        if (result != null) {

            try {

                writeCsv(
                        result,
                        "SINGLE",
                        mode,
                        style
                );

                System.out.println(
                        "Result written to "
                                + RESULTS_FILE
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void runBaselineBatch() {
        runBaselineBatch(true);
    }

    private static void runBaselineBatch(
            boolean resetFile
    ) {

        String[] races = {
                "Race1",
                "Race2",
                "Race3"
        };

        ExperimentMode[] modes = {
                ExperimentMode.NO_CHANGE,
                ExperimentMode.ONE_STOP,
                ExperimentMode.RANDOM,
                ExperimentMode.AGENTS
        };

        if (resetFile) {
            resetResultsFile();
        }

        int totalExperiments =
                races.length
                        * 10
                        * modes.length;

        int completedExperiments = 0;

        System.out.println(
                "========================================"
        );

        System.out.println(
                "BASELINE EXPERIMENT BATCH START"
        );

        System.out.println(
                "Expected runs: "
                        + totalExperiments
        );

        System.out.println(
                "========================================"
        );

        for (String raceName : races) {

            for (long seed = 1;
                 seed <= 10;
                 seed++) {

                for (ExperimentMode mode : modes) {

                    completedExperiments++;

                    System.out.println();
                    System.out.println(
                            "========================================"
                    );

                    System.out.println(
                            "RUN "
                                    + completedExperiments
                                    + "/"
                                    + totalExperiments
                    );

                    System.out.println(
                            "Race="
                                    + raceName
                                    + " | Seed="
                                    + seed
                                    + " | Mode="
                                    + mode
                    );

                    System.out.println(
                            "========================================"
                    );

                    ExperimentResult result =
                            runExperiment(
                                    raceName,
                                    seed,
                                    mode,
                                    "ONTOLOGY"
                            );

                    if (result == null) {

                        System.err.println(
                                "Experiment failed: "
                                        + raceName
                                        + ", seed="
                                        + seed
                                        + ", mode="
                                        + mode
                        );

                        return;
                    }

                    try {

                        writeCsv(
                                result,
                                "BASELINE",
                                mode,
                                "ONTOLOGY"
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "BASELINE BATCH COMPLETED"
        );

        System.out.println(
                "Successful runs: "
                        + completedExperiments
        );

        System.out.println(
                "Results file: "
                        + RESULTS_FILE
        );

        System.out.println(
                "========================================"
        );
    }

    private static void runStyleTest() {

        resetResultsFile();

        String raceName = "Race1";
        long seed = 1L;

        String[] styles = {
                "Aggressive",
                "Balanced",
                "Conservative"
        };

        System.out.println(
                "========================================"
        );

        System.out.println(
                "STYLE TEST START"
        );

        System.out.println(
                "========================================"
        );

        for (String style : styles) {

            System.out.println();

            System.out.println(
                    "Race="
                            + raceName
                            + " | Seed="
                            + seed
                            + " | Style="
                            + style
            );

            ExperimentResult result =
                    runExperiment(
                            raceName,
                            seed,
                            ExperimentMode.AGENTS,
                            style
                    );

            if (result == null) {

                System.err.println(
                        "Style test failed: "
                                + style
                );

                return;
            }

            try {

                writeCsv(
                        result,
                        "STYLE_TEST",
                        ExperimentMode.AGENTS,
                        style
                );

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "STYLE TEST COMPLETED"
        );

        System.out.println(
                "Successful runs: 3"
        );

        System.out.println(
                "========================================"
        );
    }

    private static void runStyleBatch() {
        runStyleBatch(true);
    }

    private static void runStyleBatch(
            boolean resetFile
    ) {

        String[] races = {
                "Race1",
                "Race2",
                "Race3"
        };

        String[] styles = {
                "Aggressive",
                "Balanced",
                "Conservative"
        };

        if (resetFile) {
            resetResultsFile();
        }

        int totalExperiments =
                races.length
                        * 20
                        * styles.length;

        int completedExperiments = 0;

        System.out.println(
                "========================================"
        );

        System.out.println(
                "STYLE EXPERIMENT BATCH START"
        );

        System.out.println(
                "Expected runs: "
                        + totalExperiments
        );

        System.out.println(
                "========================================"
        );

        for (String raceName : races) {

            for (long seed = 1;
                 seed <= 20;
                 seed++) {

                for (String style : styles) {

                    completedExperiments++;

                    System.out.println();
                    System.out.println(
                            "========================================"
                    );

                    System.out.println(
                            "STYLE RUN "
                                    + completedExperiments
                                    + "/"
                                    + totalExperiments
                    );

                    System.out.println(
                            "Race="
                                    + raceName
                                    + " | Seed="
                                    + seed
                                    + " | Style="
                                    + style
                    );

                    System.out.println(
                            "========================================"
                    );

                    ExperimentResult result =
                            runExperiment(
                                    raceName,
                                    seed,
                                    ExperimentMode.AGENTS,
                                    style
                            );

                    if (result == null) {

                        System.err.println(
                                "Style experiment failed: "
                                        + raceName
                                        + ", seed="
                                        + seed
                                        + ", style="
                                        + style
                        );

                        return;
                    }

                    try {

                        writeCsv(
                                result,
                                "STYLE",
                                ExperimentMode.AGENTS,
                                style
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "STYLE BATCH COMPLETED"
        );

        System.out.println(
                "Successful runs: "
                        + completedExperiments
        );

        System.out.println(
                "========================================"
        );
    }

    private static ExperimentResult runExperiment(
            String raceName,
            long seed,
            ExperimentMode mode,
            String style
    ) {

        AgentContainer container = null;

        try {

            SimulationRandom.setSeed(seed);
            ExperimentResultCollector.reset();

            System.out.println(
                    "EXPERIMENT START -> Race="
                            + raceName
                            + ", Seed="
                            + seed
                            + ", Mode="
                            + mode
                            + ", Style="
                            + style
            );

            Runtime runtime =
                    Runtime.instance();

            Profile profile =
                    new ProfileImpl();

            profile.setParameter(
                    Profile.GUI,
                    "false"
            );

            container =
                    runtime.createMainContainer(
                            profile
                    );

            AgentController trackAgent =
                    container.createNewAgent(
                            "TrackAgent",
                            "agents.TrackAgent",
                            null
                    );

            AgentController tyreAgent =
                    container.createNewAgent(
                            "TyreAgent",
                            "agents.TyreAgent",
                            null
                    );

            AgentController telemetryAgent =
                    container.createNewAgent(
                            "TelemetryAgent",
                            "agents.TelemetryAgent",
                            null
                    );

            AgentController raceSimulationAgent =
                    container.createNewAgent(
                            "RaceSimulationAgent",
                            "agents.RaceSimulationAgent",
                            new Object[]{
                                    raceName,
                                    true,
                                    seed,
                                    mode.name(),
                                    style
                            }
                    );

            AgentController weatherAgent =
                    container.createNewAgent(
                            "WeatherAgent",
                            "agents.WeatherAgent",
                            new Object[]{
                                    raceName
                            }
                    );

            AgentController strategyAgent =
                    container.createNewAgent(
                            "StrategyAgent",
                            "agents.StrategyAgent",
                            new Object[]{
                                    raceName
                            }
                    );

            trackAgent.start();
            tyreAgent.start();
            strategyAgent.start();
            telemetryAgent.start();
            raceSimulationAgent.start();
            weatherAgent.start();

            ExperimentResult result =
                    ExperimentResultCollector.awaitResult(
                            180,
                            TimeUnit.SECONDS
                    );

            if (result == null) {

                throw new IllegalStateException(
                        "Experiment timed out: "
                                + raceName
                                + ", seed="
                                + seed
                                + ", mode="
                                + mode
                                + ", style="
                                + style
                );
            }

            System.out.println(
                    "Experiment completed successfully."
            );

            return result;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (container != null) {

                try {
                    container.kill();
                } catch (Exception ignored) {
                }
            }

            try {
                Runtime.instance().shutDown();
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void resetResultsFile() {

        File file =
                new File(RESULTS_FILE);

        if (file.exists()) {

            boolean deleted =
                    file.delete();

            if (!deleted) {

                throw new IllegalStateException(
                        "Could not clear old "
                                + RESULTS_FILE
                );
            }
        }
    }

    private static void writeCsv(
            ExperimentResult result,
            String series,
            ExperimentMode mode,
            String style
    ) throws Exception {

        File file =
                new File(RESULTS_FILE);

        boolean writeHeader =
                !file.exists()
                        || file.length() == 0;

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     file,
                                     true
                             )
                     )) {

            if (writeHeader) {

                writer.write(
                        "series,race,seed,mode,style,driver,"
                                + "totalRaceTime,"
                                + "fastestLap,"
                                + "pitStops,"
                                + "finalTyre"
                );

                writer.newLine();
            }

            writer.write(
                    String.format(
                            Locale.US,
                            "%s,%s,%d,%s,%s,%s,%.3f,%.3f,%d,%s",
                            series,
                            result.getRace(),
                            result.getSeed(),
                            mode.name(),
                            style,
                            result.getDriver(),
                            result.getTotalRaceTime(),
                            result.getFastestLap(),
                            result.getPitStops(),
                            result.getFinalTyre()
                    )
            );

            writer.newLine();
        }
    }
}