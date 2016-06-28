package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class RequestRateModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException {
        final RequestRateModule module = new RequestRateModule(testConfiguration(), testReporters());
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

        final RequestRateModule module = new RequestRateModule(testConfiguration(), reporters);
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    @Test
    public void should_report_approximate_request_rate_values() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        final RequestRateModule module = new RequestRateModule(testConfiguration(), reporters);
        for (int i = 1; i < 100; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);

        assertThat(wait).isTrue();
        module.stop();

        assertThat(testReporter.reported).hasSize(4);
        assertThat(testReporter.reported.get(2).value()).isBetween(96.0, 100.0);
        assertThat(testReporter.reported.get(3).value()).isBetween(96.0, 100.0);
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule";
        configuration.options.put("period", 1);
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
