package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import simulation.SimulationRandom;

public class Launcher {

    public static void launchAgents(String raceName) {

        SimulationRandom.setSeed(1L);

        System.out.println(
                "Simulation random seed: " + SimulationRandom.getSeed()
        );

        try {
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();

            AgentContainer container = runtime.createMainContainer(profile);

            AgentController trackAgent = container.createNewAgent(
                    "TrackAgent",
                    "agents.TrackAgent",
                    null
            );

            AgentController tyreAgent = container.createNewAgent(
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
                                    false,
                                    SimulationRandom.getSeed()
                            }
                    );

            AgentController weatherAgent =
                    container.createNewAgent(
                            "WeatherAgent",
                            "agents.WeatherAgent",
                            new Object[]{raceName}
                    );

            AgentController strategyAgent = container.createNewAgent(
                    "StrategyAgent",
                    "agents.StrategyAgent",
                    new Object[]{raceName}
            );

            trackAgent.start();
            tyreAgent.start();
            strategyAgent.start();
            telemetryAgent.start();
            raceSimulationAgent.start();
            weatherAgent.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launchAgents("Race1");
    }
}