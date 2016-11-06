package io.smartcat.cassandra.diagnostics.reporter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.coursera.metrics.datadog.DefaultMetricNameFormatter;
import org.coursera.metrics.datadog.MetricNameFormatter;
import org.coursera.metrics.datadog.model.DatadogGauge;
import org.coursera.metrics.datadog.transport.HttpTransport;
import org.coursera.metrics.datadog.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A Datadog based {@link Reporter} implementation. Query reports are reporter via Datadog HTTP API
 */
public class DatadogReporter extends Reporter {

    private static final Logger logger = LoggerFactory.getLogger(DatadogReporter.class);

    private static final String API_KEY_PROP = "apiKey";

    private static MetricNameFormatter metricNameFormatter = new DefaultMetricNameFormatter();

    private static String hostname;

    private static HttpTransport httpTransport;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public DatadogReporter(ReporterConfiguration configuration) {
        super(configuration);

        logger.debug("Initializing datadog client with config: {}", configuration.toString());

        if (!configuration.options.containsKey(API_KEY_PROP)) {
            logger.warn("Failed to init Datadog client: missing api key. Aborting initialization.");
            return;
        }

        hostname = getHostname();
        if (hostname == null || hostname.isEmpty()) {
            logger.warn("Failed to init Datadog client: cannot resolve hostname. Aborting initialization.");
            return;
        }

        final String apiKey = configuration.options.get(API_KEY_PROP);
        httpTransport = new HttpTransport.Builder().withApiKey(apiKey).build();
    }

    @Override
    public void report(Measurement measurement) {
        try {
            Transport.Request request = httpTransport.prepare();
            request.addGauge(new DatadogGauge(metricNameFormatter.format(measurement.name()), measurement.value(),
                    measurement.time(), hostname, null));
            request.send();
        } catch (Exception e) {
            logger.error("Error reporting metrics to Datadog", e);
        }
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            return null;
        }
    }
}
