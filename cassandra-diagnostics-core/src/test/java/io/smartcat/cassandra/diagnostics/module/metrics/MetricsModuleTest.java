package io.smartcat.cassandra.diagnostics.module.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestMXBean;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class MetricsModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException, InterruptedException {
        final MetricsModule module = new MetricsModule(testConfiguration(), testReporters());
        module.stop();
    }

    @Test
    public void should_report_request_rate_when_started() throws ConfigurationException, InterruptedException {
        final ModuleConfiguration config = testConfiguration();
        // TODO: Run test mxbean to test jmx connection for metrics module
        initializeTestMBean(config);

        final CountDownLatch latch = new CountDownLatch(2);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final MetricsModule module = new MetricsModule(config, reporters);
        boolean wait = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_metrics";
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

    private void initializeTestMBean(ModuleConfiguration config) {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName(
                    TestMXBean.class.getPackage() + ":type=" + TestMXBean.class.getSimpleName());
            final TestMXBean mbean = new TestMXBean(config);
            server.registerMBean(mbean, objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException e) {
        }
    }

}
