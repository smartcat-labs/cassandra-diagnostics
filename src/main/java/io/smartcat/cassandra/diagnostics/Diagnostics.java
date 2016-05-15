package io.smartcat.cassandra.diagnostics;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * This class implements the Diagnostics module initialization.
 */
public class Diagnostics {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Diagnostics.class);

    /**
     * The module's DI injector.
     */
    private static final Injector INJECTOR = Guice.createInjector(new DiagnosticsModule());

    /**
     * {@link org.apache.cassandra.cql3.QueryProcessor} diagnostics wrapper.
     */
    public static QueryProcessorWrapper queryProcessorWrapper;

    /**
     * Prevents instantiation.
     */
    private Diagnostics() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Initializes Diagnostics module.
     */
    public static void init() {
        queryProcessorWrapper = INJECTOR.getInstance(QueryProcessorWrapper.class);

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

}
