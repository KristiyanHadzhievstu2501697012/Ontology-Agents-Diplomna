package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import simulation.SimulationRandom;

import jade.lang.acl.ACLMessage;
import jade.core.AID;

import jade.core.behaviours.CyclicBehaviour;

import ontology.OntologyManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import messages.WeatherMessage;
import messages.RaceControlMessage;

public class WeatherAgent extends Agent {

    private double airTemperature = 25;
    private double trackTemperature = 35;
    private double rainProbability = 0;

    private OntologyManager ontologyManager;
    private String raceName = "Race1";
    private String initialWeather;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    protected void setup() {

        System.out.println(getLocalName() + " started.");

        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            raceName = args[0].toString();
        }

        ontologyManager = new OntologyManager();

        initialWeather =
                ontologyManager.getWeather(raceName);

        initializeRainFromWeather(initialWeather);

        System.out.println(
                "WeatherAgent initialized from ontology: " +
                        "Race=" + raceName +
                        "; Weather=" + initialWeather +
                        "; InitialRain=" + rainProbability
        );

        addBehaviour(new TickerBehaviour(this, 2000) {

            @Override
            protected void onTick() {

                updateWeather();

                System.out.println(
                        "Weather -> Air="
                                + airTemperature
                                + " Track="
                                + trackTemperature
                                + " Rain="
                                + rainProbability
                );
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

                msg.addReceiver(new AID("RaceSimulationAgent", AID.ISLOCALNAME));

                WeatherMessage weatherMessage =
                        new WeatherMessage();

                weatherMessage.type = "WEATHER_UPDATE";
                weatherMessage.airTemperature =
                        airTemperature;

                weatherMessage.trackTemperature =
                        trackTemperature;

                weatherMessage.rainProbability =
                        rainProbability;

                try {

                    msg.setContent(
                            objectMapper.writeValueAsString(
                                    weatherMessage
                            )
                    );

                } catch (Exception e) {

                    System.err.println(
                            getLocalName()
                                    + ": Cannot serialize weather update: "
                                    + e.getMessage()
                    );

                    return;
                }

                send(msg);

            }
        });

        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {

                ACLMessage msg = receive();

                if (msg != null) {

                    try {

                        RaceControlMessage controlMessage =
                                objectMapper.readValue(
                                        msg.getContent(),
                                        RaceControlMessage.class
                                );

                        if ("RACE_FINISHED".equals(controlMessage.type)) {

                            System.out.println(
                                    getLocalName()
                                            + " stopping weather updates for "
                                            + controlMessage.raceName
                            );

                            doDelete();
                            return;
                        }

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Invalid race control JSON: "
                                        + e.getMessage()
                        );
                    }

                } else {
                    block();
                }
            }
        });

    }

    private void initializeRainFromWeather(String weather) {

        if (ontologyManager.isInferredInstanceOf(weather, "Rainy")) {
            rainProbability = 80.0;

        } else if (ontologyManager.isInferredInstanceOf(weather, "Cloudy")) {
            rainProbability = 40.0;

        } else if (ontologyManager.isInferredInstanceOf(weather, "Sunny")) {
            rainProbability = 10.0;

        } else {
            throw new IllegalStateException(
                    "Unsupported weather class in ontology: " + weather
            );
        }
    }

    private void updateWeather() {

        airTemperature +=
               SimulationRandom.nextDouble("WEATHER",-0.5, 0.5);

        trackTemperature +=
                SimulationRandom.nextDouble("WEATHER",-1.0, 1.0);

        rainProbability +=
                SimulationRandom.nextDouble("WEATHER",-6.0, 6.0);

        airTemperature =
                Math.max(10, Math.min(40, airTemperature));

        trackTemperature =
                Math.max(15, Math.min(60, trackTemperature));

        rainProbability =
                Math.max(0, Math.min(100, rainProbability));
    }

    @Override
    protected void takeDown() {

        System.out.println(
                getLocalName() + " terminated."
        );
    }

}