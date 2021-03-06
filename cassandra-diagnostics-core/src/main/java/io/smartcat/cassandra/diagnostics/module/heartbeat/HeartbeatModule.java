package io.smartcat.cassandra.diagnostics.module.heartbeat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Heartbeat module providing logged heartbeats at defined intervals.
 */
public class HeartbeatModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatModule.class);

    private static final String DEFAULT_MEASUREMENT_NAME = "heartbeat";

    private static final String HEARTBEAT_THREAD_NAME = "heartbeat-timer";

    private final String service;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration        Module configuration
     * @param reporters            Reporter list
     * @param globalConfiguration  Global diagnostics configuration
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public HeartbeatModule(ModuleConfiguration configuration, List<Reporter> reporters,
            final GlobalConfiguration globalConfiguration) throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        HeartbeatConfiguration config = HeartbeatConfiguration.create(configuration.options);
        service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);

        logger.info("Heartbeat module initialized with {} {} reporting period.", config.period(),
                config.timeunit().name());
        timer = new Timer(HEARTBEAT_THREAD_NAME);
        timer.schedule(new HeartbeatTask(), 0, config.reportingRateInMillis());
    }

    @Override
    public void stop() {
        logger.trace("Stopping heartbeat module.");
        timer.cancel();
    }

    /**
     * Heartbeat task that's executed at configured periods.
     */
    private class HeartbeatTask extends TimerTask {
        @Override
        public void run() {
            report(createMeasurement());
        }
    }

    private Measurement createMeasurement() {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        Measurement measurement = Measurement
                .createSimple(service, 1.0, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                        new HashMap<String, String>());
        return measurement;
    }
}
