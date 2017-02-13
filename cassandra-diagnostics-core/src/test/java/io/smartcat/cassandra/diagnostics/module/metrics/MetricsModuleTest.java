package io.smartcat.cassandra.diagnostics.module.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestMXBean;
import io.smartcat.cassandra.diagnostics.module.TestMXBeanImpl;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class MetricsModuleTest {

    @Test
    public void should_load_default_configuration_and_initialize() throws ConfigurationException, InterruptedException {
        final ModuleConfiguration config = testConfiguration();
        initializeTestMBean(config);

        final MetricsModule module = new MetricsModule(config, testReporters());

        module.stop();

        deinitializeTestMBean();
    }

    @Test
    public void should_report_metrics_when_started() throws ConfigurationException, InterruptedException {
        final ModuleConfiguration config = testConfiguration();
        TestMXBeanImpl bean = (TestMXBeanImpl) initializeTestMBean(config);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter testReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(testReporter);
            }
        };

        final MetricsModule module = new MetricsModule(config, reporters);
        boolean wait = latch.await(2000, TimeUnit.MILLISECONDS);
        module.stop();
        assertThat(wait).isTrue();
        assertThat(bean.called).isEqualTo(true);

        deinitializeTestMBean();
    }

    private ModuleConfiguration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_metrics";
        configuration.module = "io.smartcat.cassandra.diagnostics.module.metrics.MetricsModule";
        configuration.options.put("period", 1);
        configuration.options.put("timeunit", "SECONDS");
        configuration.options.put("jmxHost", "127.0.0.1");
        configuration.options.put("jmxPort", 7199);
        configuration.options.put("metricsPackageName", "io.smartcat.cassandra.diagnostics.module");
        configuration.options
                .put("metricsPatterns", Arrays.asList("^io.smartcat.cassandra.diagnostics.module.TestMXBean+"));
        return configuration;
    }

    private List<Reporter> testReporters() {
        return new ArrayList<Reporter>() {
            {
                add(new TestReporter(null));
            }
        };
    }

    private TestMXBean initializeTestMBean(ModuleConfiguration config) {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final TestMXBean bean = new TestMXBeanImpl(config);
        try {
            final StandardMBean mbean = new StandardMBean(bean, TestMXBean.class);
            final ObjectName mbeanName = new ObjectName(
                    TestMXBean.class.getPackage().getName() + ":type=" + TestMXBean.class.getSimpleName());
            server.registerMBean(mbean, mbeanName);

            return bean;
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
                NotCompliantMBeanException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void deinitializeTestMBean() {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName mbeanName = new ObjectName(
                    TestMXBean.class.getPackage().getName() + ":type=" + TestMXBean.class.getSimpleName());
            server.unregisterMBean(mbeanName);
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanRegistrationException e) {

        }
    }

}
