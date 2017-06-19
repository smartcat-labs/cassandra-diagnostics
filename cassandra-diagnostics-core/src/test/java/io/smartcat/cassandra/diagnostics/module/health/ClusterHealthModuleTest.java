package io.smartcat.cassandra.diagnostics.module.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestInfoProvider;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * Cluster health module test.
 */
public class ClusterHealthModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.health.ClusterHealthModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";

    @Test
    public void should_load_default_configuration_and_initialize()
            throws ConfigurationException, NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            int periodInMinutes = 1;
            boolean numberOfUnreachableNodesEnabled = true;
            final Configuration configuration = testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled);

            final ActorRef module = system.actorOf(ActorFactory.moduleProps(MODULE_NAME, configuration));
        }};
    }

    @Test
    public void should_report_number_of_unreachable_nodes_when_started()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            List<String> unreachableNodes = new ArrayList<>();
            unreachableNodes.add("127.0.0.1");
            unreachableNodes.add("127.0.0.2");

            final int periodInMinutes = 1;
            final boolean numberOfUnreachableNodesEnabled = true;
            final Configuration configuration = testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled);

            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, null, 0, null, unreachableNodes, null, configuration));

            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<ClusterHealthModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("1100 milliseconds")), () -> {

                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(1);
            final Measurement measurement = testReporter.underlyingActor().getReported().get(0);
            assertThat(measurement.value).isEqualTo(unreachableNodes.size());
        }};
    }

    @Test
    public void should_report_zero_unreachable_nodes_when_started()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            List<String> unreachableNodes = new ArrayList<>();

            final int periodInMinutes = 1;
            final boolean numberOfUnreachableNodesEnabled = true;
            final Configuration configuration = testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled);

            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, null, 0, null, unreachableNodes, null, configuration));

            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<ClusterHealthModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("1100 milliseconds")), () -> {

                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(1);
            final Measurement measurement = testReporter.underlyingActor().getReported().get(0);
            assertThat(measurement.value).isEqualTo(unreachableNodes.size());
        }};
    }

    @Test
    public void should_not_report_number_of_unreachable_nodes_if_not_enabled()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            List<String> unreachableNodes = new ArrayList<>();

            final int periodInMinutes = 1;
            final boolean numberOfUnreachableNodesEnabled = false;
            final Configuration configuration = testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled);

            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, null, 0, null, unreachableNodes, null, configuration));

            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<ClusterHealthModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("1100 milliseconds")), () -> {

                expectNoMsg();

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(0);
        }};
    }

    private Configuration testConfiguration(final int period, final boolean numberOfUnreachableNodesEnabled) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = MODULE_NAME;
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "MINUTES");
        configuration.options.put("numberOfUnreachableNodesEnabled", numberOfUnreachableNodesEnabled);
        configuration.reporters.add(TEST_REPORTER_NAME);
        final Configuration config = new Configuration();
        config.modules.add(configuration);
        config.reporters.add(testReporterConfiguration());
        return config;
    }

    private ReporterConfiguration testReporterConfiguration() {
        final ReporterConfiguration reporterConfiguration = new ReporterConfiguration();
        reporterConfiguration.reporter = TEST_REPORTER_NAME;
        return reporterConfiguration;
    }

}
