package agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import messages.TelemetryMessage;
import messages.TelemetryRequest;
import messages.RaceControlMessage;

import simulation.SimulationRandom;

public class TelemetryAgent extends Agent {

    private double speed = 280.0;
    private int rpm = 10500;
    private double engineTemperature = 95.0;
    private double ersLevel = 100.0;
    private boolean drsEnabled = false;
    private double brakeTemperature = 450.0;

    private TickerBehaviour telemetryTicker;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    protected void setup() {

        System.out.println(getLocalName() + " started!");

        telemetryTicker = new TickerBehaviour(this, 2000) {

            @Override
            protected void onTick() {

                updateTelemetry();

                ACLMessage telemetryMessage =
                        new ACLMessage(ACLMessage.INFORM);

                telemetryMessage.addReceiver(
                        new AID(
                                "RaceSimulationAgent",
                                AID.ISLOCALNAME
                        )
                );

                try {

                    telemetryMessage.setContent(
                            buildTelemetryJson()
                    );

                } catch (Exception e) {

                    System.err.println(
                            getLocalName()
                                    + ": Cannot serialize telemetry: "
                                    + e.getMessage()
                    );

                    return;
                }

                send(telemetryMessage);

                System.out.println(
                        getLocalName()
                                + " sent telemetry: "
                                + telemetryMessage.getContent()
                );
            }
        };

        addBehaviour(telemetryTicker);

        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {

                ACLMessage msg = receive();

                if (msg == null) {
                    block();
                    return;
                }

                RaceControlMessage controlMessage = null;

                try {

                    controlMessage =
                            objectMapper.readValue(
                                    msg.getContent(),
                                    RaceControlMessage.class
                            );

                } catch (Exception ignored) {
                }

                if (controlMessage != null
                        && "RACE_FINISHED".equals(controlMessage.type)) {

                    System.out.println(
                            getLocalName()
                                    + " stopping telemetry updates for "
                                    + controlMessage.raceName
                    );

                    if (telemetryTicker != null) {
                        telemetryTicker.stop();
                    }

                    doDelete();
                    return;
                }

                if (msg.getPerformative() == ACLMessage.REQUEST) {

                    try {

                        TelemetryRequest request =
                                objectMapper.readValue(
                                        msg.getContent(),
                                        TelemetryRequest.class
                                );

                        System.out.println(
                                getLocalName()
                                        + " received telemetry request for "
                                        + request.raceName
                        );

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Invalid telemetry request JSON: "
                                        + e.getMessage()
                        );

                        return;
                    }

                    updateTelemetry();

                    ACLMessage reply =
                            msg.createReply();

                    reply.setPerformative(
                            ACLMessage.INFORM
                    );

                    try {

                        reply.setContent(
                                buildTelemetryJson()
                        );

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Cannot serialize telemetry reply: "
                                        + e.getMessage()
                        );

                        return;
                    }

                    send(reply);

                    System.out.println(
                            getLocalName()
                                    + " replied to "
                                    + msg.getSender().getLocalName()
                                    + ": "
                                    + reply.getContent()
                    );
                }
            }
        });
    }

    private void updateTelemetry() {

        speed += SimulationRandom.nextDouble("TELEMETRY",-20.0, 20.0);

        rpm += SimulationRandom.nextInt("TELEMETRY",500, 501);

        engineTemperature += SimulationRandom.nextDouble("TELEMETRY",-1.0, 1.5);


        ersLevel -= SimulationRandom.nextDouble("TELEMETRY",1.0, 4.0);


        brakeTemperature += SimulationRandom.nextDouble("TELEMETRY",-30.0, 40.0);


        drsEnabled =
                speed > 290.0 &&
                        SimulationRandom.nextBoolean("TELEMETRY");

        speed = clamp(speed, 80.0, 350.0);

        rpm = Math.max(
                4000,
                Math.min(15000, rpm)
        );

        engineTemperature =
                clamp(
                        engineTemperature,
                        75.0,
                        125.0
                );

        ersLevel =
                clamp(
                        ersLevel,
                        0.0,
                        100.0
                );

        brakeTemperature =
                clamp(
                        brakeTemperature,
                        200.0,
                        1000.0
                );

        if (ersLevel <= 5.0) {

            ersLevel =
                    SimulationRandom.nextDouble("WEATHER",40, 70);
        }
    }

    private String buildTelemetryJson()
            throws Exception {

        TelemetryMessage telemetry =
                new TelemetryMessage();

        telemetry.type = "TELEMETRY";
        telemetry.speed = speed;
        telemetry.rpm = rpm;
        telemetry.engineTemperature =
                engineTemperature;

        telemetry.ers = ersLevel;
        telemetry.drs = drsEnabled;
        telemetry.brakeTemperature =
                brakeTemperature;

        return objectMapper.writeValueAsString(
                telemetry
        );
    }

    private double clamp(
            double value,
            double minimum,
            double maximum
    ) {

        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    @Override
    protected void takeDown() {

        System.out.println(
                getLocalName() + " terminated."
        );
    }
}