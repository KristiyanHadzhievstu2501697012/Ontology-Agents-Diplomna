package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.OntologyManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.TrackInfoMessage;
import messages.TrackRequest;

public class TrackAgent extends Agent {

    private OntologyManager ontologyManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " started!");
        ontologyManager = new OntologyManager();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    TrackRequest request;

                    try {

                        request =
                                objectMapper.readValue(
                                        msg.getContent(),
                                        TrackRequest.class
                                );

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Invalid track request JSON: "
                                        + e.getMessage()
                        );

                        return;
                    }

                    if (!"TRACK_REQUEST".equals(request.type)) {
                        return;
                    }

                    String raceName = request.raceName;

                    String circuit = ontologyManager.getCircuit(raceName);
                    String driver = ontologyManager.getDriver(raceName);
                    String trackCondition = ontologyManager.getTrackCondition(raceName);
                    String weather = ontologyManager.getWeather(raceName);
                    double temperature = ontologyManager.getAirTemperature(raceName);
                    int laps = ontologyManager.getLaps(raceName);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    TrackInfoMessage trackInfo =
                            new TrackInfoMessage();

                    trackInfo.circuit = circuit;
                    trackInfo.driver = driver;
                    trackInfo.trackCondition = trackCondition;
                    trackInfo.weather = weather;
                    trackInfo.temperature = temperature;
                    trackInfo.laps = laps;

                    try {

                        reply.setContent(
                                objectMapper.writeValueAsString(trackInfo)
                        );

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Cannot serialize track information: "
                                        + e.getMessage()
                        );

                        return;
                    }

                    send(reply);
                    System.out.println(getLocalName() + " sent reply: " + reply.getContent());
                } else {
                    block();
                }
            }
        });
    }
}