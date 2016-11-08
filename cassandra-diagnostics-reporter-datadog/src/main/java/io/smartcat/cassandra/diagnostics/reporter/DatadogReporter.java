package io.smartcat.cassandra.diagnostics.reporter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.coursera.metrics.datadog.DefaultMetricNameFormatter;
import org.coursera.metrics.datadog.MetricNameFormatter;
import org.coursera.metrics.datadog.model.DatadogGauge;
import org.coursera.metrics.datadog.transport.Transport;
import org.coursera.metrics.datadog.transport.UdpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A Datadog based {@link Reporter} implementation. Query reports are reporter via Datadog HTTP API
 */
public class DatadogReporter extends Reporter {

    private static final Logger logger = LoggerFactory.getLogger(DatadogReporter.class);

    private static final String STATSD_HOST_KEY = "statsDHost";

    private static final String UDP_PORT_KEY = "udpPort";

    private static final String DEFAULT_UDP_PORT = "8125";

    private static MetricNameFormatter metricNameFormatter = new DefaultMetricNameFormatter();

    private static String hostname;

    private static Transport transport;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public DatadogReporter(ReporterConfiguration configuration) {
        super(configuration);

        logger.debug("Initializing datadog client with config: {}", configuration.toString());

        hostname = getHostname();
        if (hostname == null || hostname.isEmpty()) {
            logger.warn("Failed to init Datadog client: cannot resolve hostname. Aborting initialization.");
            return;
        }

        final String statsdHost = configuration.getDefaultOption(STATSD_HOST_KEY, "localhost");
        final int udpPort = Integer.parseInt(configuration.getDefaultOption(UDP_PORT_KEY, DEFAULT_UDP_PORT));

        transport = new UdpTransport.Builder().withStatsdHost(statsdHost).withPort(udpPort).build();

        logger.info("Initialized Datadog UDP reporter targeting port {}", udpPort);
    }

    @Override
    public void report(Measurement measurement) {
        if (transport == null) {
            logger.warn("Datadog client is not connected. Skipping measurement {} with value {}.", measurement.name(),
                    measurement.value());
            return;
        }

        try {
            Transport.Request request = transport.prepare();
            request.addGauge(new DatadogGauge(metricNameFormatter.format(measurement.name()), measurement.value(),
                    measurement.time(), hostname, null));
            request.send();
        } catch (Exception e) {
            logger.warn("Sending measurement failed: execTime={}, exception: {}", measurement.time(), e.getMessage());
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
