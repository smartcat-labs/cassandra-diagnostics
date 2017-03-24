package io.smartcat.cassandra.diagnostics;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import io.smartcat.cassandra.diagnostics.api.DiagnosticsApi;
import io.smartcat.cassandra.diagnostics.api.DiagnosticsApiImpl;
import io.smartcat.cassandra.diagnostics.api.HttpHandler;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra.diagnostics.config.YamlConfigurationLoader;
import io.smartcat.cassandra.diagnostics.connector.QueryReporter;
import io.smartcat.cassandra.diagnostics.utils.Utils;

/**
 * This class implements the Diagnostics module initialization.
 */
public class Diagnostics implements QueryReporter {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Diagnostics.class);

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private ObjectName mxbeanName;

    private Configuration config;

    private DiagnosticsProcessor diagnosticsProcessor = null;

    private HttpHandler httpApi;

    /**
     * Constructor.
     */
    public Diagnostics() {
        config = loadConfiguration();
        if (config.hostname != null && !config.hostname.isEmpty()) {
            Utils.setHostname(config.hostname);
        }
    }

    /**
     * Returns the diagnostics configuration.
     *
     * @return the diagnostics configuration
     */
    public Configuration getConfiguration() {
        return config;
    }

    /**
     * Completes the initialization and activates the query processing.
     */
    public void activate() {
        this.diagnosticsProcessor = new DiagnosticsProcessor(config);
        this.isRunning.set(true);
        initEndpoints();
    }

    private Configuration loadConfiguration() {
        ConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration config;
        try {
            config = loader.loadConfig();
        } catch (ConfigurationException e) {
            logger.error("A problem occured while loading configuration.", e);
            throw new IllegalStateException(e);
        }
        logger.info("Effective configuration: {}", config);
        return config;
    }

    /**
     * Initializes endpoints.
     */
    private void initEndpoints() {
        logger.info("Intializing Diagnostics MXBean.");
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final DiagnosticsApi diagnosticsApi = new DiagnosticsApiImpl(config, this);

        try {
            final StandardMBean smbean = new StandardMBean(diagnosticsApi, DiagnosticsApi.class);
            mxbeanName = new ObjectName(
                    DiagnosticsApi.class.getPackage().getName() + ":type=" + DiagnosticsApi.class.getSimpleName());
            server.registerMBean(smbean, mxbeanName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
                NotCompliantMBeanException e) {
            logger.error("Unable to register DiagnosticsMBean", e);
        }

        if (config.httpApiEnabled && httpApi == null) {
            logger.info("Starting Diagnostics HTTP API at {}:{}", config.httpApiHost, config.httpApiPort);
            httpApi = new HttpHandler(config, diagnosticsApi);
            try {
                httpApi.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException ioe) {
                logger.error("Unable to start Diagnostics HTTP API", ioe);
            }
        }
    }

    private void unregisterEndpoints() {
        logger.info("Unregistering Diagnostics MXBean.");
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            server.unregisterMBean(mxbeanName);
        } catch (MBeanRegistrationException | InstanceNotFoundException e) {
            logger.error("Unable to unregister DiagnosticsMBean", e);
        }
    }

    @Override
    public void report(Query query) {
        if (isRunning.get()) {
            diagnosticsProcessor.process(query);
        }
    }

    /**
     * Reloads configuration and reinitialize modules and reporters.
     */
    public void reload() {
        isRunning.set(false);
        diagnosticsProcessor.shutdown();

        logger.info("Reloading diagnostics configuation.");
        Configuration newConfig;
        try {
            newConfig = loadConfiguration();
            if (newConfig == null) {
                logger.error("Reload operation unsuccessful. Fix configuration and reload again");
                return;
            }
        } catch (IllegalStateException ex) {
            logger.error("Reload operation failed. Fix configuration and reload again");
            return;
        }

        unregisterEndpoints();

        config = newConfig;
        diagnosticsProcessor = new DiagnosticsProcessor(config);
        initEndpoints();
        logger.info("Configuration realoaded");
        isRunning.set(true);
    }

}
