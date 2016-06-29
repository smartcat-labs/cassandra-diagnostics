package io.smartcat.cassandra.diagnostics.module.heartbeat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class HeartbeatModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException {
        final HeartbeatModule module = new HeartbeatModule(testConfiguration(), testReporters());
        module.stop();
    }

    @Test
    public void should_report_heartbeat_when_started() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final HeartbeatModule module = new HeartbeatModule(testConfiguration(), reporters);
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_heartbeat";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule";
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
