package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A Prometheus based {@link Reporter} implementation. Note that this reporter does not set the timestamp from the
 * measurement but generates its own at the moment of converting Measurement object to Prometheus compatible object.
 */
public class PrometheusReporter extends Reporter {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusReporter.class);

    private static final String HTTP_SERVER_HOST_KEY = "httpServerHost";
    private static final String HTTP_SERVER_PORT_KEY = "httpServerPort";
    private static final int DEFAULT_HTTP_SERVER_PORT = 9091;
    private static final String ILLEGAL_CHARACTERS_REGEX = "-|\\.";
    private static final String ALLOWED_DELIMITER = "_";

    private final HTTPServer server;
    final Map<String, Gauge> metricNameGuageMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     * @param globalConfiguration Global configuration
     * @throws IOException if HTTPServer cannot be initialized
     */
    public PrometheusReporter(ReporterConfiguration configuration, GlobalConfiguration globalConfiguration)
            throws IOException {
        super(configuration, globalConfiguration);
        logger.debug("Initializing prometheus client with config: {}", configuration.toString());

        final String httpServerHost = configuration.getOption(HTTP_SERVER_HOST_KEY);
        final int httpServerPort = configuration.getDefaultOption(HTTP_SERVER_PORT_KEY, DEFAULT_HTTP_SERVER_PORT);

        server = new HTTPServer(httpServerHost, httpServerPort);

        logger.info("Initialized prometheus reporter on host and port: {}", httpServerHost + ":" + httpServerPort);
    }

    @Override
    public void report(Measurement measurement) {
        if (measurement.isSimple()) {
            String name = measurement.name().replaceAll(ILLEGAL_CHARACTERS_REGEX, ALLOWED_DELIMITER);
            Gauge gauge = getOrCreateGaugeMeasurement(measurement, name);
            String[] tagValues = convertTagValues(measurement.tags());
            double value = measurement.getValue();
            gauge.labels(tagValues).set(value);
        } else {
            String baseName = measurement.name().replaceAll(ILLEGAL_CHARACTERS_REGEX, ALLOWED_DELIMITER);
            for (String key : measurement.fields().keySet()) {
                if (!isNumeric(measurement.fields().get(key))) {
                    continue;
                }
                String metricName = baseName + ":" + key.replaceAll(ILLEGAL_CHARACTERS_REGEX, ALLOWED_DELIMITER);
                Gauge gauge = getOrCreateGaugeMeasurement(measurement, metricName);
                double value = Double.parseDouble(measurement.fields().get(key));
                String[] tagValues = convertTagValues(measurement.tags());
                gauge.labels(tagValues).set(value);
            }
        }
    }

    /**
     * Get the Gauge object for the given measurementName from the metricNameGuageMap. If the object does not exist:
     * create and register the metric and put it into the metricNameGuageMap.
     *
     * @param measurement
     * @param measurementName parameter is added (instead of being fetched from Measurement object) because name of the
     *            complex measurement is calculated for each field
     * @return Gauge object for the given measurementName
     */
    private Gauge getOrCreateGaugeMeasurement(final Measurement measurement, String measurementName) {
        Gauge gauge = metricNameGuageMap.get(measurementName);
        if (gauge == null) {
            String[] tagKeys = measurement.tags().keySet().toArray(new String[0]);
            gauge = Gauge.build().labelNames(tagKeys).name(measurementName).help(measurement.name()).register();
            metricNameGuageMap.put(measurementName, gauge);
        }
        return gauge;
    }

    private String[] convertTagValues(Map<String, String> tags) {
        String[] result = new String[tags.values().size()];
        int i = 0;
        for (String tagValue : tags.values()) {
            String labelValidValue = tagValue.replaceAll(ILLEGAL_CHARACTERS_REGEX, ALLOWED_DELIMITER);
            result[i] = labelValidValue;
            i++;
        }
        return result;
    }

    private static boolean isNumeric(String string) {
        return string.matches("-?\\d+(\\.\\d+)?");
    }

    @Override
    public void stop() {
        logger.trace("Stopping DataDog reporter.");
        server.stop();
    }

}
