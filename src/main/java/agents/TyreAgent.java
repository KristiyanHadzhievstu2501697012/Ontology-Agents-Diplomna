package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.OntologyManager;
import rules.StrategyDecision;
import rules.StrategyRuleEngine;
import rules.TyreRules;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.TyreDecisionRequest;
import messages.TyreDecisionProposal;
import messages.InitialStrategyRequest;
import messages.InitialStrategyProposal;

public class TyreAgent extends Agent {

    private StrategyRuleEngine ruleEngine;
    private OntologyManager ontologyManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void setup() {

        System.out.println(getLocalName() + " started!");

        ruleEngine = new StrategyRuleEngine();
        ontologyManager = new OntologyManager();

        DFAgentDescription dfd =
                new DFAgentDescription();

        dfd.setName(getAID());

        ServiceDescription service =
                new ServiceDescription();

        service.setType("tyre-strategy");
        service.setName("TyreStrategyService");

        dfd.addServices(service);

        try {
            DFService.register(this, dfd);

            System.out.println(
                    getLocalName()
                            + " registered in DF as tyre-strategy"
            );

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        MessageTemplate contractNetProtocol =
                MessageTemplate.MatchProtocol(
                        FIPANames.InteractionProtocol.FIPA_CONTRACT_NET
                );

        MessageTemplate contractNetTemplate =
                MessageTemplate.and(
                        contractNetProtocol,
                        MessageTemplate.MatchPerformative(
                                ACLMessage.CFP
                        )
                );

        addBehaviour(
                new ContractNetResponder(
                        this,
                        contractNetTemplate
                ) {

                    @Override
                    protected ACLMessage handleCfp(
                            ACLMessage cfp
                    ) {

                        String content =
                                cfp.getContent();

                        System.out.println(
                                getLocalName()
                                        + " received FIPA CFP from "
                                        + cfp.getSender().getLocalName()
                        );

                        return handleLiveTyreRequest(
                                cfp,
                                content
                        );
                    }

                    @Override
                    protected ACLMessage handleAcceptProposal(
                            ACLMessage cfp,
                            ACLMessage propose,
                            ACLMessage accept
                    ) {

                        System.out.println(
                                getLocalName()
                                        + " received ACCEPT_PROPOSAL from "
                                        + accept.getSender().getLocalName()
                                        + ": "
                                        + accept.getContent()
                        );

                        return null;
                    }

                    @Override
                    protected void handleRejectProposal(
                            ACLMessage cfp,
                            ACLMessage propose,
                            ACLMessage reject
                    ) {

                        System.out.println(
                                getLocalName()
                                        + " received REJECT_PROPOSAL from "
                                        + reject.getSender().getLocalName()
                                        + ": "
                                        + reject.getContent()
                        );
                    }
                }
        );


        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {

                ACLMessage msg =
                        receive(
                                MessageTemplate.not(
                                        contractNetProtocol
                                )
                        );

                if (msg == null) {
                    block();
                    return;
                }

                String senderName =
                        msg.getSender().getLocalName();

                String content =
                        msg.getContent();

                if (msg.getPerformative()
                        == ACLMessage.ACCEPT_PROPOSAL) {

                    System.out.println(
                            getLocalName()
                                    + " received ACCEPT_PROPOSAL from "
                                    + senderName
                                    + ": "
                                    + content
                    );

                } else if (msg.getPerformative()
                        == ACLMessage.REJECT_PROPOSAL) {

                    System.out.println(
                            getLocalName()
                                    + " received REJECT_PROPOSAL from "
                                    + senderName
                                    + ": "
                                    + content
                    );

                } else if (content != null
                        && content.contains("TyreWear=")
                        && content.contains("CurrentTyre=")) {

                    handleLiveTyreRequest(
                            msg,
                            content
                    );

                } else {

                    handleInitialStrategyRequest(
                            msg,
                            content
                    );
                }
            }
        });
    }

    private void handleInitialStrategyRequest(
            ACLMessage msg,
            String content
    ) {

        InitialStrategyRequest request;

        try {

            request =
                    objectMapper.readValue(
                            content,
                            InitialStrategyRequest.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid initial strategy JSON: "
                            + e.getMessage()
            );

            return;
        }
        StrategyDecision decision =
                ruleEngine.decide(
                        request.trackCondition,
                        request.raceType,
                        request.highSkillDriver,
                        request.longRace,
                        request.temperature,
                        request.laps
                );

        InitialStrategyProposal proposal =
                new InitialStrategyProposal();

        proposal.recommendedTyre =
                decision.getTyre();

        proposal.pitStops =
                decision.getPitStops();

        proposal.reason =
                decision.getReason();

        ACLMessage reply =
                msg.createReply();

        reply.setPerformative(
                ACLMessage.PROPOSE
        );

        try {

            reply.setContent(
                    objectMapper.writeValueAsString(proposal)
            );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Cannot serialize initial strategy proposal: "
                            + e.getMessage()
            );

            return;
        }

        send(reply);

        System.out.println(
                getLocalName()
                        + " sent initial strategy reply: "
                        + reply.getContent()
        );
    }

    private ACLMessage handleLiveTyreRequest(
            ACLMessage msg,
            String content
    ) {

        TyreDecisionRequest tyreRequest;

        try {
            tyreRequest =
                    objectMapper.readValue(
                            content,
                            TyreDecisionRequest.class
                    );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Invalid JSON tyre decision request: "
                            + e.getMessage()
            );

            return null;
        }

        double tyreWear =
                tyreRequest.tyreWear;

        double gripLevel =
                tyreRequest.gripLevel;

        double rainProbability =
                tyreRequest.rainProbability;

        String currentTyre =
                tyreRequest.currentTyre;

        String drivingStyle =
                tyreRequest.drivingStyle;

        if (drivingStyle == null || drivingStyle.isBlank()) {
            throw new IllegalStateException(
                    "Missing driving style in tyre decision request"
            );
        }

        TyreRules tyreRules =
                new TyreRules(
                        ontologyManager,
                        drivingStyle
                );

        double intermediateThreshold =
                tyreRules.getIntermediateThreshold();

        double wetThreshold =
                tyreRules.getWetThreshold();

        double wearThreshold =
                tyreRules.getWearThreshold();

        double criticalGripThreshold =
                tyreRules.getCriticalGripThreshold();

        String recommendedTyre =
                currentTyre;

        boolean pitRequired = false;

        String reason =
                "Current tyres remain suitable.";

        if (rainProbability >= wetThreshold) {

            recommendedTyre = "WetTyre";

            pitRequired =
                    !recommendedTyre.equals(
                            currentTyre
                    );

            reason =
                    drivingStyle
                            + " strategy: rain level requires wet tyres.";

        } else if (rainProbability
                >= intermediateThreshold) {

            recommendedTyre =
                    "IntermediateTyre";

            pitRequired =
                    !recommendedTyre.equals(
                            currentTyre
                    );

            reason =
                    drivingStyle
                            + " strategy: rain level requires intermediate tyres.";

        } else {

            recommendedTyre =
                    "MediumTyre";

            if (!recommendedTyre.equals(
                    currentTyre)) {

                pitRequired = true;

                reason =
                        drivingStyle
                                + " strategy: track is dry enough for medium tyres.";

            } else if (tyreWear
                    >= wearThreshold) {

                pitRequired = true;

                reason =
                        drivingStyle
                                + " strategy: tyre wear threshold reached.";

            } else if (gripLevel
                    <= criticalGripThreshold) {

                pitRequired = true;

                reason =
                        drivingStyle
                                + " strategy: grip level is critical.";
            }
        }

        TyreDecisionProposal tyreProposal =
                new TyreDecisionProposal();

        tyreProposal.driver = tyreRequest.driver;
        tyreProposal.recommendedTyre = recommendedTyre;
        tyreProposal.pitRequired = pitRequired;
        tyreProposal.reason = reason;
        tyreProposal.drivingStyle = drivingStyle;

        ACLMessage reply =
                msg.createReply();

        reply.setPerformative(
                ACLMessage.PROPOSE
        );
        try {
            reply.setContent(
                    objectMapper.writeValueAsString(tyreProposal)
            );

        } catch (Exception e) {

            System.err.println(
                    getLocalName()
                            + ": Cannot serialize tyre proposal to JSON: "
                            + e.getMessage()
            );

            return null;
        }


        System.out.println(
                getLocalName()
                        + " sent live tyre reply: "
                        + reply.getContent()
        );

        System.out.println(
                "TYRE AI"
                        + " | Strategy="
                        + drivingStyle
                        + " | Rain="
                        + String.format(
                        "%.1f",
                        rainProbability
                )
                        + "%"
                        + " | IntermediateThreshold="
                        + intermediateThreshold
                        + "%"
                        + " | WetThreshold="
                        + wetThreshold
                        + "%"
                        + " | WearThreshold="
                        + wearThreshold
                        + "%"
        );
        return reply;
    }


}