package io.smartcat.cassandra.diagnostics.module.hiccup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class HiccupModuleTest {

    @Test
    public void should_initialize_module() throws ConfigurationException, InterruptedException {
        final HiccupModule module = new HiccupModule(testConfiguration(), testReporters(),
                GlobalConfiguration.getDefault());
        module.stop();
    }

    @Test
    public void should_report_hiccup_when_started() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(), latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final HiccupModule module = new HiccupModule(testConfiguration(), reporters, GlobalConfiguration.getDefault());
        boolean wait = latch.await(1100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_hiccup";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule";
        configuration.options.put("startDelayInMs", 0);
        configuration.options.put("period", 1);
        configuration.options.put("timeunit", "SECONDS");
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
