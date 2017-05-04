package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.LogReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class RequestRateModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException {
        final RequestRateModule module = new RequestRateModule(testConfiguration(1, Arrays.asList("*:*")),
                testReporters(), GlobalConfiguration.getDefault());
        module.stop();
    }

    @Test
    public void should_report_request_rate_when_started() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(), latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final RequestRateModule module = new RequestRateModule(testConfiguration(1, Arrays.asList("*:*")), reporters,
                GlobalConfiguration.getDefault());
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    @Test
    public void should_report_exact_request_rate_values() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final RequestRateModule module = new RequestRateModule(
                testConfiguration(1, Arrays.asList("SELECT:ALL", "UPDATE:ALL")), reporters,
                GlobalConfiguration.getDefault());

        final long numberOfRequests = 1000;
        for (int i = 0; i < numberOfRequests / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        long requestRate = 0;
        while (requestRate < numberOfRequests) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                requestRate += measurement.getValue();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            totalRequests += measurement.getValue();
        }

        assertThat(totalRequests).isEqualTo(numberOfRequests);
    }

    @Test
    public void should_report_request_rates_for_only_configured_statements_and_consistency_values()
            throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

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

        final RequestRateModule module = new RequestRateModule(
                testConfiguration(1, Arrays.asList("SELECT:ALL", "UPDATE:ALL")), reporters,
                GlobalConfiguration.getDefault());

        final long numberOfRequests = 1000;
        // only half of requests will be reported, unknown requests and lower consistency requests will be ignored
        final long expectedNumberOfRequests = 500;
        for (int i = 0; i < numberOfRequests / 4; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
            module.process(updateQueryWithLowerConsistency);
            module.process(unknownQuery);
        }

        long requestRate = 0;
        while (requestRate < expectedNumberOfRequests) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                requestRate += measurement.getValue();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            totalRequests += measurement.getValue();
        }

        assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
    }

    @Test
    public void should_report_request_rates_for_only_selects_and_all_consistency_values()
            throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

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

        final RequestRateModule module = new RequestRateModule(
                testConfiguration(1, Arrays.asList("SELECT:*")), reporters,
                GlobalConfiguration.getDefault());

        final long numberOfRequests = 1000;
        // only half of requests will be reported, unknown and update requests will be ignored
        final long expectedNumberOfRequests = 500;
        for (int i = 0; i < numberOfRequests / 4; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
            module.process(updateQueryWithLowerConsistency);
            module.process(unknownQuery);
        }

        long requestRate = 0;
        while (requestRate < expectedNumberOfRequests) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                requestRate += measurement.getValue();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            totalRequests += measurement.getValue();
        }

        assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
    }

    @Test
    public void should_report_request_rates_for_all_statement_types_and_only_all_consistency_value()
            throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

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

        final RequestRateModule module = new RequestRateModule(testConfiguration(1, Arrays.asList("*:ALL")),
                reporters, GlobalConfiguration.getDefault());

        final long numberOfRequests = 1000;
        // only 2/3 of requests will be reported, lower consistency requests will be ignored
        final long expectedNumberOfRequests = 750;
        for (int i = 0; i < numberOfRequests / 4; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
            module.process(updateQueryWithLowerConsistency);
            module.process(unknownQuery);
        }

        long requestRate = 0;
        while (requestRate < expectedNumberOfRequests) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                requestRate += measurement.getValue();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            totalRequests += measurement.getValue();
        }

        assertThat(totalRequests).isEqualTo(expectedNumberOfRequests);
    }

    @Test
    public void should_report_using_log_reporter() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
                add(new LogReporter(null, GlobalConfiguration.getDefault()));
            }
        };

        final RequestRateModule module = new RequestRateModule(
                testConfiguration(1, Arrays.asList("*:*")), reporters,
                GlobalConfiguration.getDefault());
        boolean wait = latch.await(200, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    @Test
    public void should_report_average_request_rate_for_period() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(6);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final RequestRateModule module = new RequestRateModule(
                testConfiguration(2, Arrays.asList("*:*")), reporters,
                GlobalConfiguration.getDefault());

        final long numberOfRequests = 1000;
        for (int i = 0; i < numberOfRequests; i++) {
            module.process(selectQuery);
        }

        double requestRate = 0;
        while (requestRate < numberOfRequests / 2) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                requestRate += measurement.getValue();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        double totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            totalRequests += measurement.getValue();
        }

        assertThat(totalRequests).isEqualTo(numberOfRequests / 2);
    }

    private ModuleConfiguration testConfiguration(final int period, final List<String> requestsToReport) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule";
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "SECONDS");
        configuration.options.put("requestsToReport", requestsToReport);
        return configuration;
    }

    private List<Reporter> testReporters() {
        return new ArrayList<Reporter>() {
            {
                add(new TestReporter(null, GlobalConfiguration.getDefault()));
            }
        };
    }

}
