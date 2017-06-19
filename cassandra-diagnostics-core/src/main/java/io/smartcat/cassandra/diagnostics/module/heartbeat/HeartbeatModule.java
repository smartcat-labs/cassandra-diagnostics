package io.smartcat.cassandra.diagnostics.module.heartbeat;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.smartcat.cassandra.diagnostics.module.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * Heartbeat module providing logged heartbeats at defined intervals.
 */
public class HeartbeatModule extends ModuleActor {

    private static final String DEFAULT_MEASUREMENT_NAME = "heartbeat";

    private static final String HEARTBEAT_THREAD_NAME = "heartbeat-timer";

    private final HeartbeatConfiguration config;

    private final String service;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public HeartbeatModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = HeartbeatConfiguration.create(moduleConfiguration.options);
        service = moduleConfiguration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);

        logger.info("Heartbeat module initialized with {} {} reporting period.", config.period(),
                config.timeunit().name());
    }

    @Override
    protected void start() {
        timer = new Timer(HEARTBEAT_THREAD_NAME);
        timer.schedule(new HeartbeatTask(), 0, config.reportingRateInMillis());
    }

    @Override
    protected void stop() {
        logger.debug("Stopping heartbeat module.");
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
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);
        Measurement measurement = Measurement.createSimple(service, 1.0, System.currentTimeMillis(), tags);
        return measurement;
    }
}
