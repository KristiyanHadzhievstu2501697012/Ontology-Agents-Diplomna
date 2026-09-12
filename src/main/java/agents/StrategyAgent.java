package agents;

import database.DatabaseManager;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.OntologyManager;
import main.LiveDashboardFrame;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.InitialStrategyRequest;
import messages.InitialStrategyProposal;
import messages.TrackInfoMessage;
import messages.RaceInfoMessage;
import messages.TelemetryRequest;
import messages.TelemetryMessage;
import messages.TrackRequest;

public class StrategyAgent extends Agent {

    public static String lastResult = "No result yet.";

    private String raceName = "Race1";
    private String trackCondition;
    private String weather;
    private int laps;
    private String circuit;
    private String driver;
    private String team;

    private String telemetryReason = "";

    private double tyreTemperature;
    private double airTemperature;
    private double trackTemperature;
    private double fuelLevel;
    private double gripLevel;
    private double tyreWear;
    private double rainProbability;

    private double speed;
    private int rpm;
    private double engineTemperature;
    private double ersLevel;
    private boolean drsEnabled;
    private double brakeTemperature;

    private String recommendedTyre;
    private int pitStops;


    private boolean tyreDecisionReceived;
    private boolean telemetryReceived;
    private boolean resultSaved;

    private DatabaseManager databaseManager;
    private OntologyManager ontologyManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " started!");

        databaseManager = new DatabaseManager();
        ontologyManager = new OntologyManager();

        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            raceName = args[0].toString();
        }

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage request =
                        new ACLMessage(ACLMessage.REQUEST);

                request.addReceiver(
                        new AID("TrackAgent", AID.ISLOCALNAME)
                );

                TrackRequest trackRequest =
                        new TrackRequest(raceName);

                try {

                    request.setContent(
                            objectMapper.writeValueAsString(
                                    trackRequest
                            )
                    );

                } catch (Exception e) {

                    System.err.println(
                            getLocalName()
                                    + ": Cannot serialize track request: "
                                    + e.getMessage()
                    );

                    return;
                }

                send(request);

                System.out.println(
                        getLocalName() +
                                " sent to TrackAgent: " +
                                request.getContent()
                );
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg == null) {
                    block();
                    return;
                }

                String senderName =
                        msg.getSender().getLocalName();

                System.out.println(
                        getLocalName() +
                                " received from " +
                                senderName +
                                ": " +
                                msg.getContent()
                );

                if ("TrackAgent".equals(senderName)) {
                    handleTrackAgentResponse(msg);

                } else if ("TyreAgent".equals(senderName)) {
                    handleTyreAgentResponse(msg);

                } else if ("TelemetryAgent".equals(senderName)) {
                    handleTelemetryAgentResponse(msg);

                }
            }
        });
    }

    private void handleTrackAgentResponse(ACLMessage msg) {
        parseTrackInfo(msg.getContent());

        sendTyreRequest(msg.getContent());
        sendTelemetryRequest();
    }

    private void sendTyreRequest(String trackInformation) {

        ACLMessage tyreRequest =
                new ACLMessage(ACLMessage.REQUEST);

        tyreRequest.addReceiver(
                new AID("TyreAgent", AID.ISLOCALNAME)
        );

        String raceType =
                ontologyManager.getInferredRaceType(raceName);

        System.out.println(
                "HermiT race classification used by strategy: "
                        + raceName + " -> " + raceType
        );

        raceType =
                ontologyManager.getInferredRaceType(raceName);

        boolean highSkillDriver =
                ontologyManager.isInferredInstanceOf(
                        driver,
                        "HighSkillDriver"
                );

        boolean longRace =
                ontologyManager.isInferredInstanceOf(
                        raceName,
                        "LongRace"
                );

        System.out.println(
                "HermiT strategy inferences: "
                        + raceName
                        + " -> " + raceType
                        + "; Driver=" + driver
                        + "; HighSkillDriver=" + highSkillDriver
                        + "; LongRace=" + longRace
        );

        InitialStrategyRequest request =
                new InitialStrategyRequest();

        request.circuit = circuit;
        request.driver = driver;
        request.trackCondition = trackCondition;
        request.weather = weather;
        request.raceType = raceType;

        request.highSkillDriver = highSkillDriver;
        request.longRace = longRace;

        request.temperature = airTemperature;
        request.laps = laps;

        try {

            tyreRequest.setContent(
                    objectMapper.writeValueAsString(request)
            );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Cannot serialize initial strategy request: "
                            + e.getMessage()
            );

            return;
        }

        send(tyreRequest);

        System.out.println(
                getLocalName()
                        + " sent to TyreAgent: "
                        + tyreRequest.getContent()
        );
    }

    private void sendTelemetryRequest() {

        ACLMessage request =
                new ACLMessage(
                        ACLMessage.REQUEST
                );

        request.addReceiver(
                new AID(
                        "TelemetryAgent",
                        AID.ISLOCALNAME
                )
        );

        TelemetryRequest telemetryRequest =
                new TelemetryRequest(raceName);

        try {

            request.setContent(
                    objectMapper.writeValueAsString(
                            telemetryRequest
                    )
            );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Cannot serialize telemetry request: "
                            + e.getMessage()
            );

            return;
        }

        send(request);

        System.out.println(
                getLocalName()
                        + " sent to TelemetryAgent: "
                        + request.getContent()
        );
    }


    private void handleTyreAgentResponse(ACLMessage msg) {

        InitialStrategyProposal proposal;

        try {

            proposal =
                    objectMapper.readValue(
                            msg.getContent(),
                            InitialStrategyProposal.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid initial strategy JSON: "
                            + e.getMessage()
            );

            return;
        }

        recommendedTyre =
                proposal.recommendedTyre;

        pitStops =
                proposal.pitStops;

        System.out.println(
                "Tyre decision received: "
                        + msg.getContent()
        );

        tyreDecisionReceived = true;

        finalizeDecisionIfReady();
    }

    private void handleTelemetryAgentResponse(
            ACLMessage msg
    ) {

        TelemetryMessage telemetry;

        try {

            telemetry =
                    objectMapper.readValue(
                            msg.getContent(),
                            TelemetryMessage.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid telemetry JSON: "
                            + e.getMessage()
            );

            return;
        }

        if (!"TELEMETRY".equals(telemetry.type)) {
            return;
        }

        System.out.println(
                "Telemetry response received from TelemetryAgent: "
                        + msg.getContent()
        );

        System.out.println(
                "Telemetry data -> "
                        + "Speed=" + telemetry.speed
                        + "; RPM=" + telemetry.rpm
                        + "; EngineTemperature="
                        + telemetry.engineTemperature
                        + "; ERS=" + telemetry.ers
                        + "; DRS=" + telemetry.drs
                        + "; BrakeTemperature="
                        + telemetry.brakeTemperature
        );
    }



    private void finalizeDecisionIfReady() {
        if (!tyreDecisionReceived ||
                !telemetryReceived ||
                resultSaved) {
            return;
        }

        resultSaved = true;

        telemetryReason =
                "TelemetryAgent response received and included in the final decision.";

        System.out.println(
                "Final strategy decision: " +
                        "RecommendedTyre=" + recommendedTyre +
                        "; PitStops=" + pitStops
        );

        databaseManager.saveStrategyResult(
                raceName,
                circuit,
                driver,
                trackCondition,
                weather,
                //temperature,
                (int) Math.round(airTemperature),
                laps,
                recommendedTyre,
                pitStops
        );

        ontologyManager.saveStrategyToOntology(
                raceName,
                recommendedTyre,
                pitStops
        );

        lastResult =
                "Race: " + raceName + "\n" +
                        "Circuit: " + circuit + "\n" +
                        "Driver: " + driver + "\n" +
                        "Team: " + team + "\n" +
                        "Track Condition: " + trackCondition + "\n" +
                        "Weather: " + weather + "\n" +
                        //"Temperature: " + temperature + "\n" +
                        "Laps: " + laps + "\n\n" +

                        "Telemetry:\n" +
                        "Speed: " + speed + " km/h\n" +
                        "RPM: " + rpm + "\n" +
                        "Engine Temperature: " + engineTemperature + " °C\n" +
                        "ERS: " + ersLevel + " %\n" +
                        "DRS: " + drsEnabled + "\n" +
                        "Brake Temperature: " + brakeTemperature + " °C\n\n" +

                        "Recommended Tyre: " + recommendedTyre + "\n" +
                        "Pit Stops: " + pitStops + "\n\n" +

                        "Decision explanation:\n" +
                        telemetryReason;
    }

    private void parseTrackInfo(String content) {

        TrackInfoMessage trackInfo;

        try {

            trackInfo =
                    objectMapper.readValue(
                            content,
                            TrackInfoMessage.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid track information JSON: "
                            + e.getMessage()
            );

            return;
        }

        trackCondition = trackInfo.trackCondition;
        weather = trackInfo.weather;
        airTemperature = trackInfo.temperature;
        laps = trackInfo.laps;
        circuit = trackInfo.circuit;
        driver = trackInfo.driver;

        if (driver != null && !driver.isBlank()) {
            team = ontologyManager.getTeam(driver);
        }

        System.out.println(
                "Ontology race data: Circuit=" + circuit
                        + "; Driver=" + driver
                        + "; Team=" + team
        );

        ACLMessage raceInfoMessage =
                new ACLMessage(ACLMessage.INFORM);

        raceInfoMessage.addReceiver(
                new AID(
                        "RaceSimulationAgent",
                        AID.ISLOCALNAME
                )
        );

        RaceInfoMessage raceInfo =
                new RaceInfoMessage();

        raceInfo.type = "RACE_INFO";
        raceInfo.circuit = circuit;
        raceInfo.driver = driver;
        raceInfo.team = team;
        raceInfo.trackCondition = trackCondition;
        raceInfo.weather = weather;

        try {

            raceInfoMessage.setContent(
                    objectMapper.writeValueAsString(raceInfo)
            );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Cannot serialize race information: "
                            + e.getMessage()
            );

            return;
        }

        send(raceInfoMessage);

        System.out.println(
                "Race information sent to RaceSimulationAgent: "
                        + raceInfoMessage.getContent()
        );
    }



}