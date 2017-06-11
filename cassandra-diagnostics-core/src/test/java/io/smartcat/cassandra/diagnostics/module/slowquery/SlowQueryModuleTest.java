package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.query.Query;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class SlowQueryModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";
    private static final String SLOW_QUERY_MESUREMENT_NAME = "slow_query";
    private static final String SLOW_QUERY_COUNT_SUFIX = "_count";

    @Test
    public void should_transform() throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryReportEnabled", true);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            Query query = Query
                    .create(1474741407205L, 234L, "/127.0.0.1:40042", Query.StatementType.SELECT, "keyspace", "table",
                            "select count(*) from keyspace.table", Query.ConsistencyLevel.ONE);

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().process(query);

            within(dilated(duration("1 second")), () -> {
                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });

            testModule.underlyingActor().stop();

            final Measurement measurement = testReporter.underlyingActor().getReported().get(0);

            assertThat(measurement.fields.keySet()).isEqualTo(Sets.newSet("statement", "client", "consistencyLevel"));
            assertThat(measurement.fields.get("statement")).isEqualTo("select count(*) from keyspace.table");
            assertThat(measurement.fields.get("client")).isEqualTo("/127.0.0.1:40042");
            assertThat(measurement.fields.get("consistencyLevel")).isEqualTo("ONE");
            assertThat(measurement.isSimple()).isTrue();
            assertThat(measurement.value).isEqualTo(234);

            assertThat(measurement.tags.keySet()).isEqualTo(Sets.newSet("host", "statementType", "systemName"));
            assertThat(measurement.tags.get("statementType")).isEqualTo("SELECT");
        }};
    }

    @Test
    public void should_report_number_of_slow_queries() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1);

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(110);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 100;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                long reportedMeasurementCount = 0;
                while (reportedMeasurementCount < numberOfSlowQueries) {
                    reportedMeasurementCount = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        if (measurement.name.equals(SLOW_QUERY_MESUREMENT_NAME)) {
                            reportedMeasurementCount++;
                        }
                    }
                    try {
                        latch.await(1100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        assert false;
                    }
                }

                testModule.underlyingActor().stop();

                return null;
            });

            long reportedMeasurementCount = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                if (measurement.name.equals(SLOW_QUERY_MESUREMENT_NAME)) {
                    reportedMeasurementCount++;
                }
            }

            long slowQueryCounts = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                if (measurement.name.equals(SLOW_QUERY_MESUREMENT_NAME + SLOW_QUERY_COUNT_SUFIX)) {
                    slowQueryCounts += measurement.value;
                }
            }

            assertThat(numberOfSlowQueries).isEqualTo(reportedMeasurementCount);
            assertThat(numberOfSlowQueries).isEqualTo(slowQueryCounts);
        }};
    }

    @Test
    public void should_not_report_any_slow_queries() throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.measurement = SLOW_QUERY_MESUREMENT_NAME;
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryThresholdInMilliseconds", 0);
        conf.options.put("slowQueryReportEnabled", false);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 100;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("100 milliseconds")), () -> {
                expectNoMsg();

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported()).isEmpty();
        }};
    }

    @Test
    public void should_not_log_all_types_if_slow_query_logging_is_disabled()
            throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.measurement = SLOW_QUERY_MESUREMENT_NAME;
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryThresholdInMilliseconds", 0);
        conf.options.put("slowQueryReportEnabled", false);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.options.put("queryTypesToLog", Arrays.asList("ALL"));
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(1);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 100;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("100 milliseconds")), () -> {
                expectNoMsg();

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported()).isEmpty();
        }};
    }

    @Test
    public void should_log_only_mutation_statements_when_only_mutation_type_logging_is_enabled()
            throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.measurement = SLOW_QUERY_MESUREMENT_NAME;
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryThresholdInMilliseconds", 0);
        conf.options.put("slowQueryReportEnabled", true);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.options.put("queryTypesToLog", Arrays.asList("UPDATE"));
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final int expected = 500;
            final CountDownLatch latch = new CountDownLatch(expected);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 1000;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("1100 milliseconds")), () -> {
                for (int i = 0; i < expected; i++) {
                    probe.expectMsgAnyClassOf(Measurement.class);
                }

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(expected);
            for (Measurement measurement : testReporter.underlyingActor().getReported()) {
                assertThat(measurement.tags.get("statementType")).isEqualTo("UPDATE");
            }
        }};
    }

    @Test
    public void should_log_all_statements_when_all_types_logging_is_enabled()
            throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.measurement = SLOW_QUERY_MESUREMENT_NAME;
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryThresholdInMilliseconds", 0);
        conf.options.put("slowQueryReportEnabled", true);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.options.put("queryTypesToLog", Arrays.asList("ALL"));
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(100);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 100;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("1100 milliseconds")), () -> {
                try {
                    boolean waited = testReporter.underlyingActor().latch.await(1000, TimeUnit.MILLISECONDS);
                    assertThat(waited).isTrue();
                } catch (InterruptedException e) {
                    assert false;
                }

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(100);
            int numberOfMutationsReported = 0;
            int numberOfSelectsReported = 0;
            for (Measurement measurement : testReporter.underlyingActor().getReported()) {
                if (measurement.tags.get("statementType").equals("UPDATE")) {
                    numberOfMutationsReported++;
                } else if (measurement.tags.get("statementType").equals("SELECT")) {
                    numberOfSelectsReported++;
                }
            }

            assertThat(numberOfMutationsReported).isEqualTo(50);
            assertThat(numberOfSelectsReported).isEqualTo(50);
        }};
    }

    @Test
    public void should_log_mutation_and_select_statements_when_mutation_and_select_types_logging_is_enabled()
            throws NoSuchMethodException, ClassNotFoundException {
        final ModuleConfiguration conf = new ModuleConfiguration();
        conf.measurement = SLOW_QUERY_MESUREMENT_NAME;
        conf.module = MODULE_NAME;
        conf.options.put("slowQueryThresholdInMilliseconds", 0);
        conf.options.put("slowQueryReportEnabled", true);
        conf.options.put("slowQueryCountReportEnabled", false);
        conf.options.put("queryTypesToLog", Arrays.asList("UPDATE", "SELECT"));
        conf.reporters.add(TEST_REPORTER_NAME);

        final Configuration configuration = testConfiguration(conf);

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(100);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<SlowQueryModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfSlowQueries = 100;
            for (int i = 0; i < numberOfSlowQueries / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("1100 milliseconds")), () -> {
                try {
                    boolean waited = testReporter.underlyingActor().latch.await(1000, TimeUnit.MILLISECONDS);
                    assertThat(waited).isTrue();
                } catch (InterruptedException e) {
                    assert false;
                }

                testModule.underlyingActor().stop();

                return null;
            });

            assertThat(testReporter.underlyingActor().getReported().size()).isEqualTo(100);
            int numberOfMutationsReported = 0;
            int numberOfSelectsReported = 0;
            for (Measurement measurement : testReporter.underlyingActor().getReported()) {
                if (measurement.tags.get("statementType").equals("UPDATE")) {
                    numberOfMutationsReported++;
                } else if (measurement.tags.get("statementType").equals("SELECT")) {
                    numberOfSelectsReported++;
                }
            }

            assertThat(numberOfMutationsReported).isEqualTo(50);
            assertThat(numberOfSelectsReported).isEqualTo(50);
        }};
    }

    private Configuration testConfiguration(final int period) {
        return testConfiguration(testModuleConfiguration(period));
    }

    private Configuration testConfiguration(final ModuleConfiguration moduleConfiguration) {
        final Configuration config = new Configuration();
        config.modules.add(moduleConfiguration);
        config.reporters.add(testReporterConfiguration());
        return config;
    }

    private ModuleConfiguration testModuleConfiguration(final int period) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = MODULE_NAME;
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", true);
        configuration.options.put("slowQueryCountReportEnabled", true);
        configuration.options.put("slowQueryCountReportPeriod", period);
        configuration.options.put("slowQueryCountReportTimeunit", "SECONDS");
        configuration.reporters.add(TEST_REPORTER_NAME);
        return configuration;
    }

    private ReporterConfiguration testReporterConfiguration() {
        final ReporterConfiguration reporterConfiguration = new ReporterConfiguration();
        reporterConfiguration.reporter = TEST_REPORTER_NAME;
        return reporterConfiguration;
    }
}
