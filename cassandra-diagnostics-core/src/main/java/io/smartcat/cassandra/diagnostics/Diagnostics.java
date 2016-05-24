package io.smartcat.cassandra.diagnostics;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra.diagnostics.config.YamlConfigurationLoader;
import io.smartcat.cassandra.diagnostics.connector.QueryReporter;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBean;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBeanImpl;
import io.smartcat.cassandra.diagnostics.report.ReporterContext;
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

    private final Configuration config;

    private final ReporterContext reporterContext;

    /**
     * Default constructor.
     */
    public Diagnostics() {
        this.config = getConfiguration();
        this.reporterContext = new ReporterContext(config);

        initMXBean();
    }

    private Configuration getConfiguration() {
        ConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration config;
        try {
            config = loader.loadConfig();
        } catch (ConfigurationException e) {
            logger.warn("A problem occured while loading configuration. Using default configuration.", e);
            config = new Configuration();
        }
        logger.info("Effective configuration: {}", config);
        return config;
    }

    /**
     * Initializes the Diagnostics MXBean.
     */
    private void initMXBean() {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName(
                    DiagnosticsMXBean.class.getPackage() + ":type=" + DiagnosticsMXBean.class.getSimpleName());
            final DiagnosticsMXBean mbean = new DiagnosticsMXBeanImpl(config);
            server.registerMBean(mbean, objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException e) {
            logger.error("Unable to register DiagnosticsMBean", e);
        }
    }

    @Override
    public void report(Query query) {
        reporterContext.report(query);
    }

}
