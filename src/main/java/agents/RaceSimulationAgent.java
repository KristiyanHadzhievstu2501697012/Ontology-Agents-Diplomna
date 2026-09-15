package agents;

import jade.core.Agent;

import jade.core.behaviours.TickerBehaviour;
import simulation.SimulationRandom;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import database.DatabaseManager;
import main.LiveDashboardFrame;
import main.RaceResultFrame;

import javax.swing.SwingUtilities;

import model.DriverResult;
import ontology.OntologyManager;
import messages.TyreDecisionRequest;
import messages.TyreDecisionProposal;
import messages.TyreDecisionResponse;
import messages.RaceInfoMessage;
import messages.TelemetryMessage;
import messages.WeatherMessage;
import messages.RaceControlMessage;
import model.ExperimentResult;
import simulation.ExperimentResultCollector;
import simulation.ExperimentMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;


public class RaceSimulationAgent extends Agent {


    private DatabaseManager databaseManager;
    private boolean historySaved;
    private ExperimentMode experimentMode =
            ExperimentMode.AGENTS;


    private double totalSpeed;
    private double maximumSpeed;
    private int speedMeasurements;
    private boolean resultFrameShown;
    private boolean headless = false;
    private long experimentSeed = 1L;
    private String experimentStyleOverride = null;

    private OntologyManager ontologyManager;
    private List<DriverResult> driverResults;

    private String raceName;
    private int totalLaps;

    private String circuit = "Unknown";
    private String focusDriver = "Unknown";
    private String focusTeam = "Unknown";
    private String trackCondition = "Unknown";
    private String weather = "Unknown";

    private double airTemperature;
    private double trackTemperature = 35.0;
    private double rainProbability;

    private double speed;
    private int rpm;
    private double engineTemperature = 90.0;
    private double ersLevel = 100.0;
    private boolean drsEnabled;
    private double brakeTemperature = 300.0;

    private boolean waitingForTyreDecisions = false;
    private final java.util.Set<String> pendingTyreDecisions =
            new java.util.HashSet<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void setup() {


        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {

                ACLMessage msg =
                        receive(
                                MessageTemplate.not(
                                        MessageTemplate.MatchProtocol(
                                                FIPANames.InteractionProtocol.FIPA_CONTRACT_NET
                                        )
                                )
                        );

                if (msg != null) {
                    String senderName = msg.getSender().getLocalName();
                    String content = msg.getContent();

                    if ("WeatherAgent".equals(senderName)) {
                        handleWeatherUpdate(content);

                    } else if ("TelemetryAgent".equals(senderName)) {
                        handleTelemetryUpdate(content);

                    } else if ("StrategyAgent".equals(senderName)) {
                        handleRaceInfo(content);
                    }

                } else {
                    block();
                }

            }

            private void handleWeatherUpdate(
                    String content
            ) {

                WeatherMessage weatherMessage;

                try {

                    weatherMessage =
                            objectMapper.readValue(
                                    content,
                                    WeatherMessage.class
                            );

                } catch (Exception e) {

                    System.err.println(
                            getLocalName()
                                    + ": Invalid weather JSON: "
                                    + e.getMessage()
                    );

                    return;
                }

                if (!"WEATHER_UPDATE".equals(
                        weatherMessage.type
                )) {
                    return;
                }

                RaceSimulationAgent.this.airTemperature =
                        weatherMessage.airTemperature;

                RaceSimulationAgent.this.trackTemperature =
                        weatherMessage.trackTemperature;

                RaceSimulationAgent.this.rainProbability =
                        weatherMessage.rainProbability;

                System.out.println(
                        getLocalName()
                                + " updated weather: "
                                + "Air="
                                + weatherMessage.airTemperature
                                + "; Track="
                                + weatherMessage.trackTemperature
                                + "; Rain="
                                + weatherMessage.rainProbability
                );
            }

            private void handleTelemetryUpdate(
                    String content
            ) {

                TelemetryMessage telemetry;

                try {

                    telemetry =
                            objectMapper.readValue(
                                    content,
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

                RaceSimulationAgent.this.speed =
                        telemetry.speed;

                RaceSimulationAgent.this.rpm =
                        telemetry.rpm;

                RaceSimulationAgent.this.engineTemperature =
                        telemetry.engineTemperature;

                RaceSimulationAgent.this.ersLevel =
                        telemetry.ers;

                RaceSimulationAgent.this.drsEnabled =
                        telemetry.drs;

                RaceSimulationAgent.this.brakeTemperature =
                        telemetry.brakeTemperature;

                System.out.println(
                        getLocalName()
                                + " updated telemetry: "
                                + "Speed=" + telemetry.speed
                                + "; RPM=" + telemetry.rpm
                                + "; EngineTemp="
                                + telemetry.engineTemperature
                                + "; ERS=" + telemetry.ers
                                + "; DRS=" + telemetry.drs
                                + "; BrakeTemp="
                                + telemetry.brakeTemperature
                );
            }

            private void handleRaceInfo(String content) {

                RaceInfoMessage raceInfo;

                try {

                    raceInfo =
                            objectMapper.readValue(
                                    content,
                                    RaceInfoMessage.class
                            );

                } catch (Exception e) {

                    System.err.println(
                            getLocalName()
                                    + ": Invalid race information JSON: "
                                    + e.getMessage()
                    );

                    return;
                }

                if (!"RACE_INFO".equals(raceInfo.type)) {
                    return;
                }

                RaceSimulationAgent.this.circuit =
                        raceInfo.circuit;

                RaceSimulationAgent.this.focusDriver =
                        raceInfo.driver;

                RaceSimulationAgent.this.focusTeam =
                        raceInfo.team;

                RaceSimulationAgent.this.trackCondition =
                        raceInfo.trackCondition;

                RaceSimulationAgent.this.weather =
                        raceInfo.weather;

                System.out.println(
                        "Race information updated: "
                                + focusDriver
                                + " - "
                                + focusTeam
                );
            }
        });

        addBehaviour(new TickerBehaviour(this, headless ? 50 : 2000) {

            @Override
            protected void onTick() {

                if (waitingForTyreDecisions) {
                    return;
                }

                boolean raceFinished =
                        driverResults.stream()
                                .allMatch(DriverResult::isFinished);

                if (raceFinished) {

                    DriverResult focusResult = getFocusDriverResult();

                    if (!historySaved) {
                        databaseManager.saveRaceHistory(
                                raceName,
                                circuit,
                                focusDriver,
                                focusTeam,
                                trackCondition,
                                weather,
                                totalLaps,
                                focusResult != null ? focusResult.getCompletedLaps() : totalLaps,
                                focusResult != null ? focusResult.getPitStops() : 0,
                                focusResult != null ? focusResult.getFuelLevel() : 0.0,
                                focusResult != null ? focusResult.getCurrentTyre() : "Unknown",
                                focusResult != null ? focusResult.getTyreWear() : 0.0,
                                focusResult != null ? focusResult.getGripLevel() : 0.0,
                                airTemperature,
                                trackTemperature,
                                rainProbability
                        );

                        historySaved = true;
                    }

                    RaceControlMessage raceFinishedMessage =
                            new RaceControlMessage(
                                    "RACE_FINISHED",
                                    raceName
                            );

                    String raceFinishedJson;

                    try {

                        raceFinishedJson =
                                objectMapper.writeValueAsString(
                                        raceFinishedMessage
                                );

                    } catch (Exception e) {

                        System.err.println(
                                getLocalName()
                                        + ": Cannot serialize race control message: "
                                        + e.getMessage()
                        );

                        return;
                    }

                    ACLMessage stopWeather =
                            new ACLMessage(ACLMessage.INFORM);

                    stopWeather.addReceiver(
                            new AID(
                                    "WeatherAgent",
                                    AID.ISLOCALNAME
                            )
                    );

                    stopWeather.setContent(raceFinishedJson);

                    send(stopWeather);

                    System.out.println(
                            getLocalName() +
                                    " sent RACE_FINISHED to WeatherAgent."
                    );

                    ACLMessage stopTelemetry =
                            new ACLMessage(ACLMessage.INFORM);

                    stopTelemetry.addReceiver(
                            new AID(
                                    "TelemetryAgent",
                                    AID.ISLOCALNAME
                            )
                    );

                    stopTelemetry.setContent(raceFinishedJson);

                    send(stopTelemetry);

                    System.out.println(
                            getLocalName() +
                                    " sent RACE_FINISHED to TelemetryAgent."
                    );

                    if (!headless && !resultFrameShown) {

                        resultFrameShown = true;

                        double averageSpeed =
                                speedMeasurements > 0
                                        ? totalSpeed / speedMeasurements
                                        : 0.0;

                        double simulatedRaceTime =
                                focusResult != null
                                        ? focusResult.getTotalRaceTime()
                                        : 0.0;

                        String formattedRaceTime =
                                formatRaceTime(simulatedRaceTime);

                        SwingUtilities.invokeLater(() ->
                                new RaceResultFrame(
                                        raceName,
                                        focusDriver,
                                        totalLaps,
                                        formattedRaceTime,
                                        focusResult != null ? focusResult.getPitStops() : 0,
                                        focusResult != null ? focusResult.getCurrentTyre() : "Unknown",
                                        averageSpeed,
                                        maximumSpeed
                                )
                        );
                    }

                    if (headless && focusResult != null) {

                        ExperimentResultCollector.complete(
                                new ExperimentResult(
                                        raceName,
                                        experimentSeed,
                                        focusDriver,
                                        focusResult.getTotalRaceTime(),
                                        focusResult.getFastestLap(),
                                        focusResult.getPitStops(),
                                        focusResult.getCurrentTyre()
                                )
                        );

                        System.out.println(
                                "HEADLESS RESULT READY -> "
                                        + raceName
                                        + ", seed="
                                        + experimentSeed
                        );
                    }

                    printFinalClassification();

                    stop();
                    return;
                }

                simulateLap();
                requestTyreDecisionsForGrid();

                DriverResult currentFocusResult = getFocusDriverResult();

                System.out.println(
                        getLocalName() +
                                " completed lap " +
                                (currentFocusResult != null
                                        ? currentFocusResult.getCompletedLaps()
                                        : 0) +
                                "/" +
                                totalLaps
                );

                System.out.println(
                        "Race=" + raceName +
                                "; Circuit=" + circuit +
                                "; FocusDriver=" + focusDriver +
                                "; Lap=" +
                                (currentFocusResult != null
                                        ? currentFocusResult.getCompletedLaps()
                                        : 0) +
                                "/" + totalLaps +
                                "; Weather=" + weather +
                                "; RainProbability=" + rainProbability
                );


            }
        });

        System.out.println(getLocalName() + " started!");

        databaseManager = new DatabaseManager();
        historySaved = false;

        totalSpeed = 0.0;
        maximumSpeed = 0.0;
        speedMeasurements = 0;
        resultFrameShown = false;


        Object[] args = getArguments();

        raceName = "Race1";

        if (args != null && args.length > 0) {
            raceName = args[0].toString();
        }

        if (args != null && args.length > 3) {
            experimentMode =
                    ExperimentMode.valueOf(
                            args[3].toString()
                    );
        }

        System.out.println(
                "Experiment strategy mode: "
                        + experimentMode
        );

        if (args != null && args.length > 1) {
            headless = Boolean.parseBoolean(
                    args[1].toString()
            );
        }

        if (args != null && args.length > 2) {
            experimentSeed =
                    Long.parseLong(
                            args[2].toString()
                    );
        }

        if (args != null && args.length > 4) {

            String styleArgument = args[4].toString();

            if (!styleArgument.isBlank()
                    && !"ONTOLOGY".equalsIgnoreCase(styleArgument)) {

                experimentStyleOverride = styleArgument;
            }
        }

        System.out.println(
                "Experiment style: "
                        + (experimentStyleOverride != null
                        ? experimentStyleOverride
                        : "ONTOLOGY")
        );

        System.out.println(
                "RaceSimulationAgent mode: "
                        + (headless ? "HEADLESS" : "GUI")
        );

        ontologyManager = new OntologyManager();

         totalLaps = ontologyManager.getLaps(raceName);
         airTemperature = ontologyManager.getAirTemperature(raceName);



        driverResults = new ArrayList<>();

        List<String> participants =
                ontologyManager.getParticipants(raceName);

        participants.sort(String.CASE_INSENSITIVE_ORDER);

        for (String driver : participants) {

            String team = ontologyManager.getTeam(driver);
            String car = ontologyManager.getCar(driver);

            DriverResult result =
                    new DriverResult(driver, team, car);

            result.setCurrentTyre("MediumTyre");

            driverResults.add(result);
        }

        System.out.println(
                "Starting grid loaded from ontology: " +
                        driverResults.size() +
                        " drivers."
        );

    }

    private DriverResult getFocusDriverResult() {

        for (DriverResult result : driverResults) {

            if (result.getDriver().equals(focusDriver)) {
                return result;
            }
        }

        return null;
    }


    private void handleGridTyreProposal(
            ACLMessage proposal,
            java.util.Vector acceptances
    ) {

        String content = proposal.getContent();

        String conversationId =
                proposal.getConversationId();

        if (conversationId != null) {
            pendingTyreDecisions.remove(conversationId);
        }

        if (pendingTyreDecisions.isEmpty()) {
            waitingForTyreDecisions = false;
        }

        TyreDecisionProposal tyreProposal;

        try {

            tyreProposal =
                    objectMapper.readValue(
                            content,
                            TyreDecisionProposal.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid JSON tyre proposal: "
                            + e.getMessage()
            );

            return;
        }

        String driver =
                tyreProposal.driver;

        String recommendedTyre =
                tyreProposal.recommendedTyre;

        boolean pitRequired =
                tyreProposal.pitRequired;


        if (driver == null || driver.isBlank()) {

            int prefixLength =
                    "TYRE_DECISION_".length();

            int lapIndex =
                    conversationId.lastIndexOf("_LAP_");

            if (lapIndex > prefixLength) {

                driver =
                        conversationId.substring(
                                prefixLength,
                                lapIndex
                        );
            }
        }


        DriverResult driverResult = null;

        for (DriverResult result : driverResults) {

            if (result.getDriver().equals(driver)) {

                driverResult = result;
                break;
            }
        }


        if (driverResult == null) {

            ACLMessage reject =
                    proposal.createReply();

            reject.setPerformative(
                    ACLMessage.REJECT_PROPOSAL
            );

            TyreDecisionResponse response =
                    new TyreDecisionResponse();

            response.driver = driver;
            response.accepted = false;
            response.newTyre = null;
            response.reason = "Driver not found";

            try {
                reject.setContent(
                        objectMapper.writeValueAsString(response)
                );

            } catch (Exception e) {

                System.err.println(
                        getLocalName()
                                + ": Cannot serialize REJECT_PROPOSAL JSON: "
                                + e.getMessage()
                );

                return;
            }
            acceptances.add(reject);

            System.out.println(
                    getLocalName()
                            + " REJECT_PROPOSAL -> TyreAgent"
                            + " | Driver="
                            + driver
            );

            return;
        }


        boolean finalPitDecision =
                shouldPitForExperimentMode(
                        driverResult,
                        pitRequired
                );

        if (finalPitDecision
                && recommendedTyre != null
                && !recommendedTyre.isBlank()) {

            driverResult.addPitStop();

            driverResult.setCurrentTyre(
                    recommendedTyre
            );

            driverResult.setTyreWear(0.0);
            driverResult.setGripLevel(100.0);
            driverResult.setWeatherMismatchLaps(0);


            ACLMessage accept =
                    proposal.createReply();

            accept.setPerformative(
                    ACLMessage.ACCEPT_PROPOSAL
            );

            TyreDecisionResponse response =
                    new TyreDecisionResponse();

            response.driver = driver;
            response.accepted = true;
            response.newTyre = recommendedTyre;
            response.reason = "Pit stop accepted";

            try {
                accept.setContent(
                        objectMapper.writeValueAsString(response)
                );

            } catch (Exception e) {

                System.err.println(
                        getLocalName()
                                + ": Cannot serialize ACCEPT_PROPOSAL JSON: "
                                + e.getMessage()
                );

                return;
            }

            acceptances.add(accept);

            System.out.println(
                    getLocalName()
                            + " ACCEPT_PROPOSAL -> TyreAgent"
                            + " | Driver="
                            + driver
                            + " | NewTyre="
                            + recommendedTyre
                            + " | Stops="
                            + driverResult.getPitStops()
            );

        } else {

            ACLMessage reject =
                    proposal.createReply();

            reject.setPerformative(
                    ACLMessage.REJECT_PROPOSAL
            );

            TyreDecisionResponse response =
                    new TyreDecisionResponse();

            response.driver = driver;
            response.accepted = false;
            response.newTyre = null;
            response.reason = "Pit stop not required";

            try {
                reject.setContent(
                        objectMapper.writeValueAsString(response)
                );

            } catch (Exception e) {

                System.err.println(
                        getLocalName()
                                + ": Cannot serialize REJECT_PROPOSAL JSON: "
                                + e.getMessage()
                );

                return;
            }

            acceptances.add(reject);

            System.out.println(
                    getLocalName()
                            + " REJECT_PROPOSAL -> TyreAgent"
                            + " | Driver="
                            + driver
                            + " | PitRequired=false"
            );
        }
    }

    private AID findTyreAgent() {

        DFAgentDescription template =
                new DFAgentDescription();

        ServiceDescription service =
                new ServiceDescription();

        service.setType("tyre-strategy");

        template.addServices(service);

        try {

            DFAgentDescription[] results =
                    DFService.search(this, template);

            if (results.length > 0) {
                return results[0].getName();
            }

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void requestTyreDecisionsForGrid() {

        AID tyreAgent = findTyreAgent();

        if (tyreAgent == null) {

            System.err.println(
                    getLocalName()
                            + " could not find tyre-strategy service in DF."
            );

            return;
        }

        for (DriverResult result : driverResults) {

            if (result.isFinished()) {
                continue;
            }

            String drivingStyle =
                    experimentStyleOverride != null
                            ? experimentStyleOverride
                            : ontologyManager.getDrivingStyle(
                            result.getDriver()
                    );

            ACLMessage request =
                    new ACLMessage(
                            ACLMessage.CFP
                    );

            request.setProtocol(
                    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET
            );

            request.addReceiver(tyreAgent);

            String conversationId =
                    "TYRE_DECISION_"
                            + result.getDriver()
                            + "_LAP_"
                            + result.getCompletedLaps();

            request.setConversationId(
                    conversationId
            );

            pendingTyreDecisions.add(conversationId);


            TyreDecisionRequest tyreRequest =
                    new TyreDecisionRequest();

            tyreRequest.driver = result.getDriver();
            tyreRequest.tyreWear = result.getTyreWear();
            tyreRequest.gripLevel = result.getGripLevel();
            tyreRequest.rainProbability = rainProbability;
            tyreRequest.currentTyre = result.getCurrentTyre();
            tyreRequest.fuelLevel = result.getFuelLevel();
            tyreRequest.currentLap = result.getCompletedLaps();
            tyreRequest.totalLaps = totalLaps;
            tyreRequest.drivingStyle = drivingStyle;

            try {
                request.setContent(
                        objectMapper.writeValueAsString(tyreRequest)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            addBehaviour(
                    new ContractNetInitiator(
                            this,
                            request
                    ) {
                        @Override
                        protected void handleAllResponses(
                                java.util.Vector responses,
                                java.util.Vector acceptances
                        ) {

                            for (Object responseObject : responses) {

                                ACLMessage response =
                                        (ACLMessage) responseObject;

                                if (response.getPerformative()
                                        == ACLMessage.PROPOSE) {

                                    handleGridTyreProposal(
                                            response,
                                            acceptances
                                    );
                                }
                            }
                        }
                    }
            );

            System.out.println(
                    getLocalName()
                            + " CFP -> TyreAgent"
                            + " | Driver="
                            + result.getDriver()
                            + " | Lap="
                            + result.getCompletedLaps()
            );
        }
        waitingForTyreDecisions = !pendingTyreDecisions.isEmpty();
    }

    private void simulateLap() {


        for (DriverResult result : driverResults) {

            if (result.isFinished()) {
                continue;
            }

            int driverSkill =
                    ontologyManager.getDriverSkill(
                            result.getDriver()
                    );

            String strategy =
                    experimentStyleOverride != null
                            ? experimentStyleOverride
                            : ontologyManager.getDrivingStyle(
                            result.getDriver()
                    );

            String car =
                    ontologyManager.getCar(
                            result.getDriver()
                    );

            int carPerformance =
                    ontologyManager.getCarPerformance(car);

            double overallPerformance =
                    driverSkill * 0.6
                            + carPerformance * 0.4;

            double baseLapTime = 92.0;

            double performanceBonus =
                    (overallPerformance - 80.0) * 0.10;

            double tyreWearIncrease;

            if ("Aggressive".equalsIgnoreCase(strategy)) {

                tyreWearIncrease =
                        SimulationRandom.nextDouble(
                                "RACE",
                                4.0,
                                6.5
                        );

            } else if ("Conservative".equalsIgnoreCase(strategy)) {

                tyreWearIncrease =
                        SimulationRandom.nextDouble("RACE", 2.0, 4.0);

            } else {

                tyreWearIncrease =
                        SimulationRandom.nextDouble("RACE", 3.0, 5.0);
            }

            double newTyreWear =
                    Math.min(
                            result.getTyreWear()
                                    + tyreWearIncrease,
                            100.0
                    );

            result.setTyreWear(newTyreWear);

            result.setGripLevel(
                    Math.max(
                            0.0,
                            100.0 - newTyreWear
                    )
            );

            double fuelConsumption =
                    SimulationRandom.nextDouble("RACE", 1.5, 2.5);

            result.setFuelLevel(
                    result.getFuelLevel()
                            - fuelConsumption
            );

            double tyrePenalty =
                    result.getTyreWear() * 0.02;

            double randomVariation =
                    SimulationRandom.nextDouble("RACE", -0.6, 0.6);

            double strategyLapEffect;

            if ("Aggressive".equalsIgnoreCase(strategy)) {

                strategyLapEffect = -0.35;

            } else if ("Conservative".equalsIgnoreCase(strategy)) {

                strategyLapEffect = 0.30;

            } else {

                strategyLapEffect = 0.0;
            }

            double lapTime =
                    baseLapTime
                            - performanceBonus
                            + tyrePenalty
                            + strategyLapEffect
                            + randomVariation;

            result.addLapTime(lapTime);

            if (result.getCompletedLaps()
                    >= totalLaps) {

                result.setFinished(true);
            }
        }


        DriverResult focusResult = getFocusDriverResult();


        double currentSpeed = speed;

        totalSpeed += currentSpeed;
        speedMeasurements++;

        if (currentSpeed > maximumSpeed) {
            maximumSpeed = currentSpeed;
        }

        String liveWeather;

        if (rainProbability > 80.0) {
            liveWeather = "Heavy Rain";
        } else if (rainProbability > 50.0) {
            liveWeather = "Light Rain";
        } else if (rainProbability > 20.0) {
            liveWeather = "Cloudy";
        } else {
            liveWeather = "Dry";
        }

        LiveDashboardFrame dashboard =
                LiveDashboardFrame.getInstance();

        if (dashboard != null) {

            dashboard.updateDashboard(
                    raceName,
                    focusDriver,
                    focusTeam,
                    focusResult != null
                            ? focusResult.getCompletedLaps()
                            : 0,
                    totalLaps,
                    focusResult != null
                            ? focusResult.getFuelLevel()
                            : 0.0,
                    focusResult != null
                            ? focusResult.getCurrentTyre()
                            : "Unknown",
                    focusResult != null
                            ? focusResult.getTyreWear()
                            : 0.0,
                    focusResult != null
                            ? focusResult.getGripLevel()
                            : 0.0,
                    liveWeather,
                    rainProbability,
                    speed,
                    rpm,
                    engineTemperature,
                    ersLevel,
                    drsEnabled,
                    brakeTemperature,
                    focusResult != null
                            ? focusResult.getLastLap()
                            : 0.0,
                    focusResult != null
                            ? focusResult.getFastestLap()
                            : 0.0
            );

            long simulatedElapsedSeconds =
                    focusResult != null
                            ? (long) focusResult.getTotalRaceTime()
                            : 0L;

            dashboard.updateRaceTime(
                    simulatedElapsedSeconds
            );
        }
    }


    private void printFinalClassification() {

        driverResults.sort(
                Comparator.comparingDouble(
                        DriverResult::getTotalRaceTime
                )
        );

        System.out.println();
        System.out.println("========== FINAL CLASSIFICATION ==========");

        for (int index = 0;
             index < driverResults.size();
             index++) {

            DriverResult result = driverResults.get(index);

            System.out.printf(
                    "P%-2d %-12s %-14s Time=%s Fastest=%s%n",
                    index + 1,
                    result.getDriver(),
                    result.getTeam(),
                    formatRaceTime(result.getTotalRaceTime()),
                    formatLapTime(result.getFastestLap())
            );
        }

        System.out.println("==========================================");
    }

    private String formatRaceTime(double totalSeconds) {

        int hours = (int) (totalSeconds / 3600);
        int minutes =
                (int) ((totalSeconds % 3600) / 60);

        double seconds = totalSeconds % 60;

        return String.format(
                "%02d:%02d:%06.3f",
                hours,
                minutes,
                seconds
        );
    }

    private String formatLapTime(double totalSeconds) {

        if (totalSeconds == Double.MAX_VALUE ||
                totalSeconds <= 0.0) {

            return "--:--.---";
        }

        int minutes = (int) (totalSeconds / 60);
        double seconds = totalSeconds % 60;

        return String.format(
                "%d:%06.3f",
                minutes,
                seconds
        );
    }

    private boolean shouldPitForExperimentMode(
            DriverResult driverResult,
            boolean agentPitRequired
    ) {

        switch (experimentMode) {

            case NO_CHANGE:
                return false;

            case ONE_STOP:

                int halfwayLap =
                        Math.max(
                                1,
                                totalLaps / 2
                        );

                return driverResult.getPitStops() == 0
                        && driverResult.getCompletedLaps()
                        >= halfwayLap;

            case RANDOM:

                if (driverResult.getPitStops() >= 3) {
                    return false;
                }

                double randomDecision =
                        SimulationRandom.nextDouble(
                                "BASELINE_RANDOM",
                                0.0,
                                1.0
                        );

                return randomDecision < 0.06;

            case AGENTS:
            default:
                return agentPitRequired;
        }
    }

}