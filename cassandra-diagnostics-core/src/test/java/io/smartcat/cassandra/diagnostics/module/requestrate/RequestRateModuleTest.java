package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

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
        final RequestRateModule module = new RequestRateModule(testConfiguration(1), testReporters());
        module.stop();
    }

    @Test
    public void should_report_request_rate_when_started() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final RequestRateModule module = new RequestRateModule(testConfiguration(1), reporters);
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    @Test
    public void should_report_exact_request_rate_values() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(20);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        final RequestRateModule module = new RequestRateModule(testConfiguration(1), reporters);

        final long numberOfRequests = 1000;
        for (int i = 0; i < numberOfRequests / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        long requestRate = 0;
        while (requestRate < numberOfRequests) {
            requestRate = 0;
            for (final Measurement measurement : latchTestReporter.reported) {
                requestRate += measurement.value();
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long totalRequests = 0;
        for (final Measurement measurement : latchTestReporter.reported) {
            totalRequests += measurement.value();
        }

        assertThat(totalRequests).isEqualTo(numberOfRequests);
    }

    @Test
    public void should_report_using_log_reporter() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
                add(new LogReporter(null));
            }
        };

        final RequestRateModule module = new RequestRateModule(testConfiguration(1), reporters);
        boolean wait = latch.await(200, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    private ModuleConfiguration testConfiguration(final int period) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule";
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "SECONDS");
        return configuration;
    }

    private List<Reporter> testReporters() {
        return new ArrayList<Reporter>() {
            {
                add(new TestReporter(null));
            }
        };
    }

}
