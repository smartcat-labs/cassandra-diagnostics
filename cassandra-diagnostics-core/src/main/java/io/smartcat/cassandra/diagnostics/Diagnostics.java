package io.smartcat.cassandra.diagnostics;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.smartcat.cassandra.diagnostics.connector.QueryReporter;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * This class implements the Diagnostics module initialization.
 */
public class Diagnostics implements QueryReporter {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Diagnostics.class);

    /**
     * The module's DI injector.
     */
    private static final Injector INJECTOR = Guice.createInjector(new DiagnosticsModule());

    /**
     * Default constructor.
     */
    public Diagnostics() {
        initMXBean();
    }

    /**
     * Initializes the Diagnostics MXBean.
     */
    private static void initMXBean() {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName(
                    DiagnosticsMXBean.class.getPackage() + ":type=" + DiagnosticsMXBean.class.getSimpleName());
            final DiagnosticsMXBean mbean = INJECTOR.getInstance(DiagnosticsMXBean.class);
            server.registerMBean(mbean, objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException e) {
            logger.error("Unable to register DiagnosticsMBean", e);
        }
    }

    @Override
    public void report(Query query) {
        // TODO Auto-generated method stub
    }

}
