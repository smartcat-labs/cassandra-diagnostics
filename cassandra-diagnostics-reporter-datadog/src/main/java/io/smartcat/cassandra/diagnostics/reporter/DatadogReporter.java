package io.smartcat.cassandra.diagnostics.reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * A Datadog based {@link ReporterActor} implementation. Query reports are reporter via Datadog HTTP API
 */
public class DatadogReporter extends ReporterActor {

    private static final String STATSD_HOST_KEY = "statsDHost";

    private static final String STATSD_PORT_KEY = "statsDPort";

    private static final int DEFAULT_STATSD_PORT = 8125;

    private static final String KEYS_PREFIX_KEY = "keysPrefix";

    private static final String DEFAULT_KEYS_PREFIX = "";

    private static final String FIXED_TAGS_KEY = "fixedTags";

    private static final List<String> DEFAULT_FIXED_TAGS = new ArrayList<>();

    private static String hostname;

    private static StatsDClient client;

    /**
     * Constructor.
     *
     * @param reporterName  Reporter class name
     * @param configuration Configuration
     */
    public DatadogReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);

        logger.debug("Initializing datadog client with config: {}", configuration.toString());

        hostname = configuration.global.hostname;
        if (hostname == null || hostname.isEmpty()) {
            logger.warning("Failed to init Datadog client: cannot resolve hostname. Aborting initialization.");
            return;
        }

        final String statsdHost = reporterConfiguration.getDefaultOption(STATSD_HOST_KEY, hostname);
        final int statsdPort = reporterConfiguration.getDefaultOption(STATSD_PORT_KEY, DEFAULT_STATSD_PORT);
        final String keysPrefix = reporterConfiguration.getDefaultOption(KEYS_PREFIX_KEY, DEFAULT_KEYS_PREFIX);
        final List<String> fixedTags = reporterConfiguration.getDefaultOption(FIXED_TAGS_KEY, DEFAULT_FIXED_TAGS);

        client = new NonBlockingStatsDClient(keysPrefix, statsdHost, statsdPort,
                fixedTags.toArray(new String[fixedTags.size()]));

        logger.info("Initialized Datadog UDP reporter targeting port {}", statsdPort);
    }

    @Override
    protected void stop() {
        logger.debug("Stopping DataDog reporter.");
        client.stop();
    }

    @Override
    protected void report(Measurement measurement) {
        if (client == null) {
            logger.warning("Datadog client is not initialized. Skipping measurement {} with value {}.",
                    measurement.name, measurement.value);
            return;
        }

        try {
            if (measurement.isSimple()) {
                client.recordGaugeValue(measurement.name, measurement.value, convertTagsMap(measurement.tags));
                logger.debug("Reporting measurement {}, value {} and tags {}", measurement.name, measurement.value,
                        convertTagsMap(measurement.tags));
            } else {
                for (String key : measurement.fields.keySet()) {
                    if (!isNumeric(measurement.fields.get(key))) {
                        continue;
                    }

                    client.recordGaugeValue(measurement.name + "." + key,
                            Double.parseDouble(measurement.fields.get(key)), convertTagsMap(measurement.tags));
                }
            }

        } catch (Exception e) {
            logger.warning("Sending measurement failed: execTime={}, exception: {}", measurement.time, e.getMessage());
        }
    }

    private String[] convertTagsMap(final Map<String, String> tags) {
        String[] result = new String[tags.size()];

        int index = 0;
        for (String key : tags.keySet()) {
            result[index] = String.format("%s:%s", key, tags.get(key));
            index++;
        }

        return result;
    }

    private static boolean isNumeric(String string) {
        return string.matches("-?\\d+(\\.\\d+)?");
    }

}
