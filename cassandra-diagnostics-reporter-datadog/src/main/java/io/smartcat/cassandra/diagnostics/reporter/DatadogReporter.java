package io.smartcat.cassandra.diagnostics.reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.utils.Utils;

/**
 * A Datadog based {@link Reporter} implementation. Query reports are reporter via Datadog HTTP API
 */
public class DatadogReporter extends Reporter {

    private static final Logger logger = LoggerFactory.getLogger(DatadogReporter.class);

    private static final String STATSD_HOST_KEY = "statsDHost";

    private static final String STATSD_PORT_KEY = "statsDPort";

    private static final int DEFAULT_STATSD_PORT = 8125;

    private static final String KEYS_PREFIX_KEY = "keysPerfix";

    private static final String DEFAULT_KEYS_PREFIX = "";

    private static final String FIXED_TAGS_KEY = "fixedTags";

    private static final List<String> DEFAULT_FIXED_TAGS = new ArrayList<>();

    private static String hostname;

    private static StatsDClient client;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public DatadogReporter(ReporterConfiguration configuration) {
        super(configuration);

        logger.debug("Initializing datadog client with config: {}", configuration.toString());

        hostname = Utils.getHostname();
        if (hostname == null || hostname.isEmpty()) {
            logger.warn("Failed to init Datadog client: cannot resolve hostname. Aborting initialization.");
            return;
        }

        final String statsdHost = configuration.getDefaultOption(STATSD_HOST_KEY, hostname);
        final int statsdPort = configuration.getDefaultOption(STATSD_PORT_KEY, DEFAULT_STATSD_PORT);
        final String keysPrefix = configuration.getDefaultOption(KEYS_PREFIX_KEY, DEFAULT_KEYS_PREFIX);
        final List<String> fixedTags = configuration.getDefaultOption(FIXED_TAGS_KEY, DEFAULT_FIXED_TAGS);

        client = new NonBlockingStatsDClient(keysPrefix, statsdHost, statsdPort,
                fixedTags.toArray(new String[fixedTags.size()]));

        logger.info("Initialized Datadog UDP reporter targeting port {}", statsdPort);
    }

    @Override
    public void report(Measurement measurement) {
        if (client == null) {
            logger.warn("Datadog client is not initialized. Skipping measurement {} with value {}.",
                    measurement.name(), measurement.value());
            return;
        }

        try {
            client.recordGaugeValue(measurement.name(), measurement.value(), convertTagsMap(measurement.tags()));
            logger.debug("Reporting measurement {}, value {} and tags {}", measurement.name(), measurement.value(),
                    convertTagsMap(measurement.tags()));
        } catch (Exception e) {
            logger.warn("Sending measurement failed: execTime={}, exception: {}", measurement.time(), e.getMessage());
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

}
