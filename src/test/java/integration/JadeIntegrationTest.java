package integration;

import agents.TyreAgent;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JadeIntegrationTest {

    private static final CountDownLatch serviceFound =
            new CountDownLatch(1);

    @Test
    void tyreAgentShouldRegisterInJadeDf() throws Exception {

        int port = findFreePort();

        Runtime runtime = Runtime.instance();

        ProfileImpl profile =
                new ProfileImpl();

        profile.setParameter(
                Profile.MAIN_HOST,
                "localhost"
        );

        profile.setParameter(
                Profile.MAIN_PORT,
                String.valueOf(port)
        );

        AgentContainer container =
                runtime.createMainContainer(profile);

        try {

            AgentController tyreAgent =
                    container.acceptNewAgent(
                            "TyreAgentTest",
                            new TyreAgent()
                    );

            AgentController probeAgent =
                    container.acceptNewAgent(
                            "DfProbeAgent",
                            new DfProbeAgent()
                    );

            tyreAgent.start();
            probeAgent.start();

            boolean registered =
                    serviceFound.await(
                            10,
                            TimeUnit.SECONDS
                    );

            assertTrue(
                    registered,
                    "TyreAgent was not discovered in JADE DF"
            );

        } finally {

            try {
                container.kill();
            } catch (Exception ignored) {
            }

            runtime.shutDown();
        }
    }

    private static int findFreePort() throws Exception {

        try (ServerSocket socket =
                     new ServerSocket(0)) {

            return socket.getLocalPort();
        }
    }

    public static class DfProbeAgent
            extends Agent {

        @Override
        protected void setup() {

            addBehaviour(
                    new TickerBehaviour(
                            this,
                            100
                    ) {

                        @Override
                        protected void onTick() {

                            try {

                                DFAgentDescription template =
                                        new DFAgentDescription();

                                ServiceDescription service =
                                        new ServiceDescription();

                                service.setType(
                                        "tyre-strategy"
                                );

                                template.addServices(
                                        service
                                );

                                DFAgentDescription[] result =
                                        DFService.search(
                                                myAgent,
                                                template
                                        );

                                if (result.length > 0) {

                                    serviceFound.countDown();

                                    stop();

                                    myAgent.doDelete();
                                }

                            } catch (Exception e) {

                                stop();

                                myAgent.doDelete();
                            }
                        }
                    }
            );
        }
    }
}