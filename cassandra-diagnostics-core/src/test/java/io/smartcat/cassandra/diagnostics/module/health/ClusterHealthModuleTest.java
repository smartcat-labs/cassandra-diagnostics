package io.smartcat.cassandra.diagnostics.module.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Cluster health module test.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DiagnosticsAgent.class)
public class ClusterHealthModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        when(infoProvider.getUnreachableNodes()).thenReturn(new ArrayList<String>());
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);
        int periodInMinutes = 1;
        boolean numberOfUnreachableNodesEnabled = true;
        final ClusterHealthModule module = new ClusterHealthModule(
                testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled), testReporters());
        module.stop();

        PowerMockito.verifyStatic();
    }

    @Test
    public void should_report_number_of_unreachable_nodes_when_started()
            throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        List<String> unreachableNodes = new ArrayList<>();
        unreachableNodes.add("127.0.0.1");
        unreachableNodes.add("127.0.0.2");

        when(infoProvider.getUnreachableNodes()).thenReturn(unreachableNodes);
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        int periodInMinutes = 1;
        boolean numberOfUnreachableNodesEnabled = true;
        final ClusterHealthModule module = new ClusterHealthModule(
                testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled), reporters);
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
        assertThat(testReporter.getReported().size()).isEqualTo(1);
    }

    @Test
    public void should_report_two_unreachable_nodes_when_started() throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        List<String> unreachableNodes = new ArrayList<>();
        unreachableNodes.add("127.0.0.1");
        unreachableNodes.add("127.0.0.2");

        when(infoProvider.getUnreachableNodes()).thenReturn(unreachableNodes);
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        int periodInMinutes = 1;
        boolean numberOfUnreachableNodesEnabled = true;
        final ClusterHealthModule module = new ClusterHealthModule(
                testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled), reporters);
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
        assertThat(testReporter.getReported().get(0).getValue()).isEqualTo(2.0);
    }

    @Test
    public void should_report_zero_unreachable_nodes_when_started()
            throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        List<String> unreachableNodes = new ArrayList<>();

        when(infoProvider.getUnreachableNodes()).thenReturn(unreachableNodes);
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        int periodInMinutes = 1;
        boolean numberOfUnreachableNodesEnabled = true;
        final ClusterHealthModule module = new ClusterHealthModule(
                testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled), reporters);
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
        assertThat(testReporter.getReported().get(0).getValue()).isEqualTo(0.0);
    }

    @Test
    public void should_not_report_number_of_unreachable_nodes_if_not_enabled()
            throws ConfigurationException, InterruptedException {
        InfoProvider infoProvider = mock(InfoProvider.class);
        List<String> unreachableNodes = new ArrayList<>();
        unreachableNodes.add("127.0.0.1");
        unreachableNodes.add("127.0.0.2");

        when(infoProvider.getUnreachableNodes()).thenReturn(unreachableNodes);
        PowerMockito.mockStatic(DiagnosticsAgent.class);
        PowerMockito.when(DiagnosticsAgent.getInfoProvider()).thenReturn(infoProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        int periodInMinutes = 1;
        boolean numberOfUnreachableNodesEnabled = false;
        final ClusterHealthModule module = new ClusterHealthModule(
                testConfiguration(periodInMinutes, numberOfUnreachableNodesEnabled), reporters);
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isFalse();
        assertThat(testReporter.getReported().size()).isEqualTo(0);
    }

    private ModuleConfiguration testConfiguration(final int period, final boolean numberOfUnreachableNodesEnabled) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_measurement";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.health.ClusterHealthModule";
        configuration.options.put("period", period);
        configuration.options.put("timeunit", "MINUTES");
        configuration.options.put("numberOfUnreachableNodesEnabled", numberOfUnreachableNodesEnabled);
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
