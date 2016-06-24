package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.Date;
import java.util.HashMap;
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
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
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

    private final long rateFactor;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters)
            throws ConfigurationException {
        super(configuration, reporters);

        RequestRateConfiguration config = RequestRateConfiguration.create(configuration.options);
        service = configuration.measurement;
        period = config.period();
        timeunit = config.timeunit();
        rateFactor = timeunit.toSeconds(1);

        logger.info("RequestRate module initialized with {} period and {} timeunit.", period, timeunit.name());
        requests = metricsRegistry.meter(service);
        timer = new Timer();
        timer.schedule(new RequestRateTask(), timeunit.toMillis(period));
    }

    @Override
    protected boolean isForReporting(Query query) {
        return false;
    }

    @Override
    public Measurement transform(Query query) {
        // Future work: Separate marks for request types
        requests.mark();
        return null;
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    /**
     * Request rate reporter task that's executed at configured period.
     */
    private class RequestRateTask extends TimerTask {
        @Override
        public void run() {
            double rate = convertRate(requests.getMeanRate());
            logger.debug("Request rate: {}/{}", rate, timeunit.name());
            Measurement signal = Measurement
                    .create(service, rate, new Date().getTime(), TimeUnit.MILLISECONDS, new HashMap<String, String>(),
                            new HashMap<String, String>());
            for (Reporter reporter : reporters) {
                reporter.report(signal);
            }
        }
    }
}
