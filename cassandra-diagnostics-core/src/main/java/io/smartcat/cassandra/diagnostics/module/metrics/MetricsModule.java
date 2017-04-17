package io.smartcat.cassandra.diagnostics.module.metrics;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Metrics module collecting Cassandra metrics exposed over JMX. Requires local JMX connection.
 */
public class MetricsModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(MetricsModule.class);

    private static final String METRICS_THREAD_NAME = "metrics-timer";

    private final MetricsConfiguration config;

    private final MetricsCollector metricsCollector;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration        Module configuration
     * @param reporters            Reporter list
     * @param globalConfiguration  Global diagnostics configuration
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public MetricsModule(ModuleConfiguration configuration, List<Reporter> reporters,
            final GlobalConfiguration globalConfiguration) throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        config = MetricsConfiguration.create(configuration.options);
        metricsCollector = new MetricsCollector(config, globalConfiguration);

        logger.info("Metrics module initialized with {} {} reporting period.", config.period(),
                config.timeunit().name());

        timer = new Timer(METRICS_THREAD_NAME);
        if (metricsCollector.connect()) {
            timer.schedule(new MetricsTask(), 0, config.reportingRateInMillis());
        }
    }

    @Override
    public void stop() {
        logger.trace("Stopping metrics module.");
        timer.cancel();
        metricsCollector.close();
    }

    /**
     * Metrics reporter task that's executed at configured period.
     */
    private class MetricsTask extends TimerTask {
        @Override
        public void run() {
            for (Measurement measurement : metricsCollector.collectMeasurements()) {
                report(measurement);
            }
        }
    }

}
