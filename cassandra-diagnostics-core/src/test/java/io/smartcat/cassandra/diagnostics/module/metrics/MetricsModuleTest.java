package io.smartcat.cassandra.diagnostics.module.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.junit.Test;

import akka.actor.ActorRef;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestMXBean;
import io.smartcat.cassandra.diagnostics.module.TestMXBeanImpl;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class MetricsModuleTest extends BaseActorTest {

    private static final String MODULE_NAME = "io.smartcat.cassandra.diagnostics.module.metrics.MetricsModule";
    private static final String TEST_REPORTER_NAME = "io.smartcat.cassandra.diagnostics.module.TestReporter";

    @Test
    public void should_load_default_configuration_and_initialize()
            throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration();
        initializeTestMBean(configuration.modules.get(0));

        new TestKit(system) {{
            final ActorRef module = system.actorOf(ActorFactory.moduleProps(MODULE_NAME, configuration));

            module.tell(new Command.Start(), getRef());

            module.tell(new Command.Stop(), getRef());
        }};

        deinitializeTestMBean();
    }

    @Test
    public void should_report_metrics_when_started() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration();
        TestMXBeanImpl bean = (TestMXBeanImpl) initializeTestMBean(configuration.modules.get(0));

        new TestKit(system) {{
            final TestActorRef<TestReporter> testReporter = TestActorRef
                    .create(system, ActorFactory.reporterProps(TEST_REPORTER_NAME, configuration));

            final TestActorRef<MetricsModule> testModule = TestActorRef
                    .create(system, ActorFactory.moduleProps(MODULE_NAME, configuration));

            final TestKit probe = new TestKit(system);
            testReporter.tell(probe.getRef(), getRef());
            expectMsg(duration("1 second"), "done");

            testModule.underlyingActor().start();

            within(dilated(duration("2000 milliseconds")), () -> {
                probe.expectMsgAnyClassOf(Measurement.class);

                testModule.underlyingActor().stop();

                return null;
            });
        }};

        assertThat(bean.called).isEqualTo(true);

        deinitializeTestMBean();
    }

    private Configuration testConfiguration() {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = "test_metrics";
        configuration.module = MODULE_NAME;
        configuration.options.put("period", 1);
        configuration.options.put("timeunit", "SECONDS");
        configuration.options.put("jmxHost", "127.0.0.1");
        configuration.options.put("jmxPort", 7199);
        configuration.options.put("metricsPackageNames", Arrays.asList("io.smartcat.cassandra.diagnostics.module"));
        configuration.options
                .put("metricsPatterns", Arrays.asList("^io.smartcat.cassandra.diagnostics.module.TestMXBean+"));
        configuration.reporters.add(TEST_REPORTER_NAME);
        final Configuration config = new Configuration();
        config.modules.add(configuration);
        config.reporters.add(testReporterConfiguration());
        return config;
    }

    private ReporterConfiguration testReporterConfiguration() {
        final ReporterConfiguration reporterConfiguration = new ReporterConfiguration();
        reporterConfiguration.reporter = TEST_REPORTER_NAME;
        return reporterConfiguration;
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
