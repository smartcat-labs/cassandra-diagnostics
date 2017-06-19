package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.query.Query;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class RequestRateModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";

    @Test
    public void should_load_default_configuration_and_initialize()
            throws NoSuchMethodException, ClassNotFoundException {
        new TestKit(system) {{
            final Configuration configuration = testConfiguration(1, Arrays.asList("*:*"));
            final ActorRef module = system.actorOf(ActorFactory.moduleProps(MODULE_NAME, configuration));

            module.tell(new Command.Start(), getRef());

            module.tell(new Command.Stop(), getRef());
        }};
    }

    @Test
    public void should_report_request_rate_when_started() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, Arrays.asList("*:*"));

        new TestKit(system) {{
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<RequestRateModule> testModule = TestActorRef
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
        }};
    }

    @Test
    public void should_report_exact_request_rate_values() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, Arrays.asList("SELECT:ALL", "UPDATE:ALL"));

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(20);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<RequestRateModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfRequests = 1000;
            for (int i = 0; i < numberOfRequests / 2; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                long requestRate = 0;
                while (requestRate < numberOfRequests) {
                    requestRate = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        requestRate += measurement.value;
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

            long totalRequests = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                totalRequests += measurement.value;
            }

            assertThat(totalRequests).isEqualTo(numberOfRequests);
        }};
    }

    @Test
    public void should_report_request_rates_for_only_configured_statements_and_consistency_values()
            throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, Arrays.asList("SELECT:ALL", "UPDATE:ALL"));

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(20);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<RequestRateModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQueryWithLowerConsistency = mock(Query.class);
            when(updateQueryWithLowerConsistency.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQueryWithLowerConsistency.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ONE);
            final Query unknownQuery = mock(Query.class);
            when(unknownQuery.statementType()).thenReturn(Query.StatementType.UNKNOWN);
            when(unknownQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfRequests = 1000;
            // only half of requests will be reported, unknown requests and lower consistency requests will be ignored
            final long expectedNumberOfRequests = 500;
            for (int i = 0; i < numberOfRequests / 4; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
                testModule.underlyingActor().process(updateQueryWithLowerConsistency);
                testModule.underlyingActor().process(unknownQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                long requestRate = 0;
                while (requestRate < expectedNumberOfRequests) {
                    requestRate = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        requestRate += measurement.value;
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

            long totalRequests = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                totalRequests += measurement.value;
            }

            assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
        }};
    }

    @Test
    public void should_report_request_rates_for_only_selects_and_all_consistency_values()
            throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, Arrays.asList("SELECT:*"));

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(20);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<RequestRateModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQueryWithLowerConsistency = mock(Query.class);
            when(updateQueryWithLowerConsistency.statementType()).thenReturn(Query.StatementType.SELECT);
            when(updateQueryWithLowerConsistency.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ONE);
            final Query unknownQuery = mock(Query.class);
            when(unknownQuery.statementType()).thenReturn(Query.StatementType.UNKNOWN);
            when(unknownQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfRequests = 1000;
            // only half of requests will be reported, unknown and update requests will be ignored
            final long expectedNumberOfRequests = 500;
            for (int i = 0; i < numberOfRequests / 4; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
                testModule.underlyingActor().process(updateQueryWithLowerConsistency);
                testModule.underlyingActor().process(unknownQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                long requestRate = 0;
                while (requestRate < expectedNumberOfRequests) {
                    requestRate = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        requestRate += measurement.value;
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

            long totalRequests = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                totalRequests += measurement.value;
            }

            assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
        }};
    }

    @Test
    public void should_report_request_rates_for_all_statement_types_and_only_all_consistency_value()
            throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(1, Arrays.asList("*:ALL"));

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(20);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<RequestRateModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQuery = mock(Query.class);
            when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
            when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
            final Query updateQueryWithLowerConsistency = mock(Query.class);
            when(updateQueryWithLowerConsistency.statementType()).thenReturn(Query.StatementType.SELECT);
            when(updateQueryWithLowerConsistency.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ONE);
            final Query unknownQuery = mock(Query.class);
            when(unknownQuery.statementType()).thenReturn(Query.StatementType.UNKNOWN);
            when(unknownQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfRequests = 1000;
            // only 2/3 of requests will be reported, lower consistency requests will be ignored
            final long expectedNumberOfRequests = 750;
            for (int i = 0; i < numberOfRequests / 4; i++) {
                testModule.underlyingActor().process(selectQuery);
                testModule.underlyingActor().process(updateQuery);
                testModule.underlyingActor().process(updateQueryWithLowerConsistency);
                testModule.underlyingActor().process(unknownQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                long requestRate = 0;
                while (requestRate < expectedNumberOfRequests) {
                    requestRate = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        requestRate += measurement.value;
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

            long totalRequests = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                totalRequests += measurement.value;
            }

            assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
        }};
    }

    @Test
    public void should_report_average_request_rate_for_period() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration(2, Arrays.asList("*:*"));

        new TestKit(system) {{
            final CountDownLatch latch = new CountDownLatch(6);
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));
            testReporter.underlyingActor().latch = latch;

            final TestActorRef<RequestRateModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final Query selectQuery = mock(Query.class);
            when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
            when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

            testModule.underlyingActor().start();

            final long numberOfRequests = 1000;
            for (int i = 0; i < numberOfRequests; i++) {
                testModule.underlyingActor().process(selectQuery);
            }

            within(dilated(duration("10 seconds")), () -> {

                double requestRate = 0;
                while (requestRate < numberOfRequests / 2) {
                    requestRate = 0;
                    for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                        requestRate += measurement.value;
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

            double totalRequests = 0;
            for (final Measurement measurement : testReporter.underlyingActor().getReported()) {
                totalRequests += measurement.value;
            }

            assertThat(totalRequests).isEqualTo(numberOfRequests / 2);
        }};
    }

    private Configuration testConfiguration(final int period, final List<String> requestsToReport) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = MODULE_NAME;
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "SECONDS");
        configuration.options.put("requestsToReport", requestsToReport);
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
