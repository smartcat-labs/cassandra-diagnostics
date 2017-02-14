package io.smartcat.cassandra.diagnostics.module.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Status module test.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DiagnosticsAgent.class)
public class StatusModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        when(infoProvider.getCompactions()).thenReturn(new ArrayList<CompactionInfo>());
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final StatusModule module = new StatusModule(testConfiguration(1, true), testReporters());
        module.stop();

        PowerMockito.verifyStatic();
    }

    @Test
    public void should_report_status_when_started() throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        when(infoProvider.getCompactions()).thenReturn(getCompactions());
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final StatusModule module = new StatusModule(testConfiguration(1, true), reporters);
        boolean wait = latch.await(1000, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
        assertThat(testReporter.getReported().size()).isEqualTo(1);
    }

    @Test
    public void should_not_report_compactions_when_disabled() throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        when(infoProvider.getCompactions()).thenReturn(getCompactions());
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final StatusModule module = new StatusModule(testConfiguration(1, false), reporters);
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isFalse();
        assertThat(testReporter.getReported().size()).isEqualTo(0);
    }

    private ModuleConfiguration testConfiguration(final int period, final boolean compactionsEnabled) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.status.StatusModule";
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "MINUTES");
        configuration.options.put("compactionsEnabled", compactionsEnabled);
        return configuration;
    }

    private List<Reporter> testReporters() {
        return new ArrayList<Reporter>() {
            {
                add(new TestReporter(null));
            }
        };
    }

    private List<CompactionInfo> getCompactions() {
        return new ArrayList<CompactionInfo>() {
            {
                Map<String, String> fields = new HashMap<>();
                fields.put("total", "1000000");
                fields.put("completed", "1000");
                fields.put("unit", "bytes");
                fields.put("taskType", "Validation");
                fields.put("keyspace", "test_ks");
                fields.put("columnfamily", "test_cf");
                fields.put("compactionId", "5010e2f6-8feb-4e11-87e5-b14570339a67");
                add(new CompactionInfo(fields));
            }
        };
    }

}
