package io.smartcat.cassandra.diagnostics.module;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Query;
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
     * @param reporters     Reporter list
     */
    public HeartbeatModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);

        period = Integer.parseInt(configuration.options.getOrDefault(PERIOD_PROP, DEFAULT_PERIOD));
        timeunit = TimeUnit.valueOf(configuration.options.getOrDefault(TIMEUNIT_PROP, DEFAULT_TIMEUNIT));
        service = configuration.measurement;

        logger.debug("Heartbeat module initialized with {} period and {} timeunit.", period, timeunit.name());
        timer = new Timer();
        timer.schedule(new HeartbeatTask(), timeunit.toMillis(period));
    }

    @Override
    public void process(Query query) {

    }

    private class HeartbeatTask extends TimerTask {
        public void run() {
            logger.info("Heartbeat signal.");
            for (Reporter reporter: reporters) {

            }
        }
    }
}
