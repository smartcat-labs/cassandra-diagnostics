package io.smartcat.cassandra.diagnostics.module.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestInfoProvider;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * Status module test.
 */
public class StatusModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.status.StatusModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";

    @Test
    public void should_load_default_configuration_and_initialize()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            int periodInMinutes = 1;
            boolean compactionsEnabled = true;
            boolean tpStatsEnabled = true;
            boolean repairsEnabled = true;
            boolean nodeInfoEnabled = true;
            final Configuration configuration = testConfiguration(periodInMinutes, compactionsEnabled, tpStatsEnabled,
                    repairsEnabled, nodeInfoEnabled);

            final ActorRef module = system.actorOf(ActorFactory.moduleProps(MODULE_NAME, configuration));
        }};
    }

    @Test
    public void should_report_compaction_info_when_started() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, true, false, false, false);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system.actorOf(TestInfoProvider
                    .props(getCompactions(), null, 0, getCompactionSettingsInfo(), null, null, configuration));

            final int expected = 2;
            final CountDownLatch latch = new CountDownLatch(expected);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("1100 milliseconds")), () -> {
                for (int i = 0; i < expected; i++) {
                    probe.expectMsgAnyClassOf(Measurement.class);
                }

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(expected);
        }};
    }

    @Test
    public void should_report_thread_pool_info_when_started() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, false, true, false, false);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, getTPStats(), 0, null, null, null, configuration));

            final int expected = 3;
            final CountDownLatch latch = new CountDownLatch(expected);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("1100 milliseconds")), () -> {
                for (int i = 0; i < expected; i++) {
                    probe.expectMsgAnyClassOf(Measurement.class);
                }

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(expected);
        }};
    }

    @Test
    public void should_report_repair_info_when_started() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, false, false, true, false);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, null, getRepairSessions(), null, null, null, configuration));

            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
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
        }};
    }

    @Test
    public void should_not_report_compactions_when_disabled() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, false, false, false, false);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system.actorOf(TestInfoProvider
                    .props(getCompactions(), null, 0, getCompactionSettingsInfo(), null, null, configuration));

            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("100 milliseconds")), () -> {
                expectNoMsg();

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(0);
        }};
    }

    @Test
    public void should_not_report_node_info_when_disabled() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, false, false, false, false);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system
                    .actorOf(TestInfoProvider.props(null, null, 0, null, null, getNodeInfo(), configuration));

            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("100 milliseconds")), () -> {
                expectNoMsg();

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(0);
        }};
    }

    @Test
    public void should_report_node_info_when_enabled() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, false, false, false, true);

        new TestKit(system) {{
            final ActorRef testInfoProvider = system.actorOf(TestInfoProvider
                    .props(null, null, 0, null, null, getNodeInfo(), configuration));

            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<StatusModule> testModule = TestActorRef
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
        }};
    }

    private Configuration testConfiguration(final int period, final boolean compactionsEnabled,
            final boolean tpStatsEnabled, final boolean repairsEnabled, final boolean nodeInfoEnabled) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = MODULE_NAME;
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "MINUTES");
        configuration.options.put("compactionsEnabled", compactionsEnabled);
        configuration.options.put("tpStatsEnabled", tpStatsEnabled);
        configuration.options.put("repairsEnabled", repairsEnabled);
        configuration.options.put("nodeInfoEnabled", nodeInfoEnabled);
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

    private List<CompactionInfo> getCompactions() {
        return new ArrayList<CompactionInfo>() {
            {
                CompactionInfo compactionInfo = new CompactionInfo(1000000, 1000, "bytes", "Validation", "test_ks",
                        "test_cf", "5010e2f6-8feb-4e11-87e5-b14570339a67");
                add(compactionInfo);
            }
        };
    }

    private CompactionSettingsInfo getCompactionSettingsInfo() {
        return new CompactionSettingsInfo(16, 1, 1, 1, 1);
    }

    private List<TPStatsInfo> getTPStats() {
        return new ArrayList<TPStatsInfo>() {
            {
                add(new TPStatsInfo("test1", 1, 1, 1, 1, 1));
                add(new TPStatsInfo("test2", 2, 2, 2, 2, 2));
                add(new TPStatsInfo("test3", 3, 3, 3, 3, 3));
            }
        };
    }

    private long getRepairSessions() {
        return 39;
    }

    private NodeInfo getNodeInfo() {
        return new NodeInfo(true, false, true, 10);
    }

}
