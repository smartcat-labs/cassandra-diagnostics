package io.smartcat.cassandra.diagnostics.module.metrics;

import java.util.Timer;
import java.util.TimerTask;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.actor.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Metrics module collecting Cassandra metrics exposed over JMX. Requires local JMX connection.
 */
public class MetricsModule extends ModuleActor {

    private static final String DEFAULT_MEASUREMENT_NAME = "metrics";

    private static final String METRICS_THREAD_NAME = "metrics-timer";

    private final MetricsConfiguration config;

    private final MetricsCollector metricsCollector;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public MetricsModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = MetricsConfiguration.create(moduleConfiguration.options);
        metricsCollector = new MetricsCollector(moduleConfiguration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME),
                config, configuration);

        logger.info("Metrics module initialized with {} {} reporting period.", config.period(),
                config.timeunit().name());
    }

    @Override
    protected void start() {
        if (metricsCollector.connect()) {
            timer = new Timer(METRICS_THREAD_NAME);
            timer.schedule(new MetricsTask(), 0, config.reportingRateInMillis());
        } else {
            logger.warning("Failed to start metrics module.");
            timer = null;
        }
    }

    @Override
    public void stop() {
        logger.debug("Stopping metrics module.");
        if (timer != null) {
            timer.cancel();
        }
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
