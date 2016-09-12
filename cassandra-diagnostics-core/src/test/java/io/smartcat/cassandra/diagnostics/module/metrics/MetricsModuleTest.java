package io.smartcat.cassandra.diagnostics.module.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class MetricsModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException, InterruptedException {
        final MetricsModule module = new MetricsModule(testConfiguration(), testReporters());
        module.stop();
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_heartbeat";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.metrics.MetricsModule";
        configuration.options.put("period", 1);
        configuration.options.put("timeunit", "SECONDS");
        configuration.options.put("jmxHost", "127.0.0.1");
        configuration.options.put("jmxPort", 7199);
        configuration.options.put("metricsPatterns",
                Arrays.asList("^org.apache.cassandra.metrics.Cache.+", "^org.apache.cassandra.metrics.ClientRequest.+",
                        "^org.apache.cassandra.metrics.CommitLog.+", "^org.apache.cassandra.metrics.Compaction.+",
                        "^org.apache.cassandra.metrics.ColumnFamily.PendingTasks",
                        "^org.apache.cassandra.metrics.ColumnFamily.ReadLatency",
                        "^org.apache.cassandra.metrics.ColumnFamily.WriteLatency",
                        "^org.apache.cassandra.metrics.ColumnFamily.ReadTotalLatency",
                        "^org.apache.cassandra.metrics.ColumnFamily.WriteTotalLatency",
                        "^org.apache.cassandra.metrics.DroppedMetrics.+", "^org.apache.cassandra.metrics.ReadRepair.+",
                        "^org.apache.cassandra.metrics.Storage.+", "^org.apache.cassandra.metrics.ThreadPools.+"));
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
