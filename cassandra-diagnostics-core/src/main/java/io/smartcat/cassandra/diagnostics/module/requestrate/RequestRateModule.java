package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(RequestRateModule.class);

    private static final String PERIOD_PROP = "period";

    private static final String DEFAULT_PERIOD = "1";

    private static final String TIMEUNIT_PROP = "timeunit";

    private static final String DEFAULT_TIMEUNIT = "SECONDS";

    private final MetricRegistry metricsRegistry = new MetricRegistry();

    private final Meter requests;

    private final String service;

    private final int period;

    private final TimeUnit timeunit;

    private final double rateFactor;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);

        period = Integer.parseInt(configuration.options.getOrDefault(PERIOD_PROP, DEFAULT_PERIOD));
        timeunit = TimeUnit.valueOf(configuration.options.getOrDefault(TIMEUNIT_PROP, DEFAULT_TIMEUNIT));
        service = configuration.measurement;
        rateFactor = timeunit.toSeconds(1);

        logger.debug("RequestRate module initialized with {} period and {} timeunit.", period, timeunit.name());
        requests = metricsRegistry.meter(service);
        timer = new Timer();
        timer.schedule(new RequestRateTask(), timeunit.toMillis(period));
    }

    @Override
    public Measurement transform(Query query) {
        requests.mark();
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    /**
     * Request rate reporter task that's executed at configured period.
     */
    private class RequestRateTask extends TimerTask {
        public void run() {
            double rate = convertRate(requests.getMeanRate());
            logger.info("Request rate: {}/{}", rate, timeunit.name());
            // Add reporting
        }
    }
}
