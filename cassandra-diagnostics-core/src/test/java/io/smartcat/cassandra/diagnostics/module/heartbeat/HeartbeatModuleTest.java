package io.smartcat.cassandra.diagnostics.module.heartbeat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.LogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class HeartbeatModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";
    private static final String LOG_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.reporter.LogReporter";

    @Test
    public void should_load_default_configuration_and_initialize()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            final ActorRef module = system.actorOf(ActorFactory.moduleProps(MODULE_NAME, testConfiguration()));

            module.tell(new Command.Start(), getRef());

            module.tell(new Command.Stop(), getRef());
        }};
    }

    @Test
    public void should_report_heartbeat_when_started() throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            final Configuration configuration = testConfiguration();

            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<HeartbeatModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("100 milliseconds")), () -> {
                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(1);
            final Measurement measurement = testReporter.underlyingActor().getReported().get(0);
            assertThat(measurement.value).isEqualTo(1.0);
        }};
    }

    @Test
    public void should_report_using_log_reporter() throws NoSuchMethodException, ClassNotFoundException {
        final ActorSystem actorSystem = ActorSystem
                .create("testsystem", ConfigFactory.parseString("akka.loggers = [\"akka.testkit.TestEventListener\"]"));

        new TestKit(actorSystem) {{
            final Configuration configuration = testConfiguration();
            configuration.reporters.add(logReporterConfiguration());
            configuration.modules.get(0).reporters.add(LOG_REPORTER_NAME);

            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<LogReporter> logReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(LOG_REPORTER_NAME, configuration));

            final TestActorRef<HeartbeatModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("200 milliseconds")), () -> {
                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(1);
            final Measurement measurement = testReporter.underlyingActor().getReported().get(0);
            assertThat(measurement.value).isEqualTo(1.0);

        }};

        TestKit.shutdownActorSystem(actorSystem);
    }

    private Configuration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_heartbeat";
        configuration.module = MODULE_NAME;
        configuration.options.put("period", 1);
        configuration.options.put("timeunit", "SECONDS");
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

    private ReporterConfiguration logReporterConfiguration() {
        final ReporterConfiguration reporterConfiguration = new ReporterConfiguration();
        reporterConfiguration.reporter = LOG_REPORTER_NAME;
        return reporterConfiguration;
    }

}
