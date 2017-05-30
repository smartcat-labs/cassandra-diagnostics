package io.smartcat.cassandra.diagnostics.module.hiccup;

import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P100;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P90_0;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P95_0;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P99_0;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P99_9;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P99_99;
import static io.smartcat.cassandra.diagnostics.module.hiccup.HiccupModule.Percentage.P99_999;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Hiccup module running a hiccup meter measuring platform pauses.
 */
public class HiccupModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(HiccupModule.class);

    private static final String DEFAULT_MEASUREMENT_NAME = "hiccup";

    private static final String HICCUP_THREAD_NAME = "hiccup-thread";

    private static final double NANOS_IN_MILLIS = 1000000d;

    private final HiccupConfiguration config;

    private final String service;

    private final Thread thread;

    /**
     * Constructor.
     *
     * @param configuration       Module configuration
     * @param reporters           Reporter list
     * @param globalConfiguration Global diagnostics configuration
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public HiccupModule(ModuleConfiguration configuration, List<Reporter> reporters,
            GlobalConfiguration globalConfiguration) throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        this.config = HiccupConfiguration.create(configuration.options);
        this.service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);

        logger.info("Hiccup module initialized with {} {} reporting period.", config.period(),
                config.timeunit().name());

        this.thread = new Thread(new HiccupMeter(), HICCUP_THREAD_NAME);
        this.thread.start();
    }

    @Override
    public void stop() {
        logger.trace("Stopping hiccup module.");
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            logger.error("Hiccup thread failed to stop", e);
        }
    }

    /**
     * Hiccup meter running hiccup recorder and collecting measurements.
     */
    private class HiccupMeter implements Runnable {

        private HiccupRecorder hiccupRecorder = null;
        private Histogram intervalHistogram = null;

        @Override
        public void run() {
            final SingleWriterRecorder recorder = new SingleWriterRecorder(config.lowestTrackableValueInNanos(),
                    config.highestTrackableValueInNanos(), config.numberOfSignificantValueDigits());

            final long uptimeAtInitialStartTime = ManagementFactory.getRuntimeMXBean().getUptime();
            long now = System.currentTimeMillis();
            long jvmStartTime = now - uptimeAtInitialStartTime;

            hiccupRecorder = new HiccupRecorder(config, recorder);

            try {
                final long startTime;

                if (config.startDelayInMs() > 0) {
                    // Run hiccup recorder during startDelayInMs time to let code warm up:
                    hiccupRecorder.start();
                    while (config.startDelayInMs() > System.currentTimeMillis() - jvmStartTime) {
                        Thread.sleep(100);
                    }
                    hiccupRecorder.terminate();
                    hiccupRecorder.join();

                    recorder.reset();
                    hiccupRecorder = new HiccupRecorder(config, recorder);
                }

                hiccupRecorder.start();
                startTime = System.currentTimeMillis();

                long nextReportingTime = startTime + config.reportingIntervalInMillis();

                while (now > 0) {
                    now = hiccupRecorder.getCurrentTimeMsecWithDelay(nextReportingTime);

                    if (now > nextReportingTime) {
                        // Get the latest interval histogram and give the recorder a fresh Histogram for the next
                        // interval
                        intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);

                        while (now > nextReportingTime) {
                            nextReportingTime += config.reportingIntervalInMillis();
                        }

                        if (intervalHistogram.getTotalCount() > 0) {
                            report(createMeasurement(intervalHistogram));
                        }
                    }
                }

            } catch (InterruptedException ex) {
                logger.debug("HiccupMeter interrupted/terminating");
            }

            try {
                hiccupRecorder.terminate();
                hiccupRecorder.join();
            } catch (InterruptedException ex) {
                logger.debug("HiccupMeter terminate/join interrupted.");
            }
        }
    }

    private Measurement createMeasurement(Histogram histogram) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        final Map<String, String> fields = new HashMap<>();
        fields.put(P90_0.name, String.valueOf(histogram.getValueAtPercentile(P90_0.percentage) / NANOS_IN_MILLIS));
        fields.put(P95_0.name, String.valueOf(histogram.getValueAtPercentile(P95_0.percentage) / NANOS_IN_MILLIS));
        fields.put(P99_0.name, String.valueOf(histogram.getValueAtPercentile(P99_0.percentage) / NANOS_IN_MILLIS));
        fields.put(P99_9.name, String.valueOf(histogram.getValueAtPercentile(P99_9.percentage) / NANOS_IN_MILLIS));
        fields.put(P99_99.name, String.valueOf(histogram.getValueAtPercentile(P99_99.percentage) / NANOS_IN_MILLIS));
        fields.put(P99_999.name, String.valueOf(histogram.getValueAtPercentile(P99_999.percentage) / NANOS_IN_MILLIS));
        fields.put(P100.name, String.valueOf(histogram.getValueAtPercentile(P100.percentage) / NANOS_IN_MILLIS));
        return Measurement.createComplex(service, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

    /**
     * Percentage enum.
     */
    enum Percentage {
        P90_0("90.0", 90.0),
        P95_0("95.0", 95.0),
        P99_0("99.0", 99.0),
        P99_9("99.9", 99.9),
        P99_99("99.99", 99.99),
        P99_999("99.999", 99.999),
        P100("100.0", 100.0);

        public final String name;
        public final double percentage;

        Percentage(final String name, final double percentage) {
            this.name = name;
            this.percentage = percentage;
        }
    }

}
