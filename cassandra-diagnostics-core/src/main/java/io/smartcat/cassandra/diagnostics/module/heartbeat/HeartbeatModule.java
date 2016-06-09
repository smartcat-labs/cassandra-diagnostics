package io.smartcat.cassandra.diagnostics.module.heartbeat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Heartbeat module providing logged heartbeats at defined intervals.
 */
public class HeartbeatModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatModule.class);

    private static final String PERIOD_PROP = "period";

    private static final String DEFAULT_PERIOD = "15";

    private static final String TIMEUNIT_PROP = "timeunit";

    private static final String DEFAULT_TIMEUNIT = "MINUTES";

    private final String service;

    private final int period;

    private final TimeUnit timeunit;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters Reporter list
     */
    public HeartbeatModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);

        period = Integer.parseInt(configuration.getDefaultOption(PERIOD_PROP, DEFAULT_PERIOD));
        timeunit = TimeUnit.valueOf(configuration.getDefaultOption(TIMEUNIT_PROP, DEFAULT_TIMEUNIT));
        service = configuration.measurement;

        logger.debug("Heartbeat module initialized with {} period and {} timeunit.", period, timeunit.name());
        timer = new Timer();
        timer.schedule(new HeartbeatTask(), timeunit.toMillis(period));
    }

    /**
     * Heartbeat task that's executed at configured periods.
     */
    private class HeartbeatTask extends TimerTask {
        @Override
        public void run() {
            logger.info("Heartbeat signal.");
            for (Reporter reporter : reporters) {

            }
        }
    }

    @Override
    public Measurement transform(Query query) {
        return null;
    }
}
