package io.smartcat.cassandra.diagnostics.reporter;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Reporter;
import io.smartcat.cassandra.diagnostics.ReporterConfiguration;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * An InfluxDB based {@link Reporter} implementation. Query reports are sent to influxdb.
 */
public class InfluxReporter implements Reporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(InfluxReporter.class);

    private static final String ADDRESS_PROP = "influxDbAddress";

    private static final String USERNAME_PROP = "influxUsername";

    private static final String PASSWORD_PROP = "influxPassword";

    private static final String DB_NAME_PROP = "influxDbName";

    private static final String DEFAULT_DB_NAME = "slowQueries";

    private static final String MEASUREMENT_NAME_PROP = "influxMeasurement";

    private static final String DEFAULT_MEASUREMENT_NAME = "queryReport";

    private static final String RETENTION_POLICY_PROP = "influxRetentionPolicy";

    private static final String DEFAULT_RETENTION_POLICY = "default";

    private ReporterConfiguration config;

    private String dbAddress;

    private String username;

    private String password;

    private String dbName;

    private String retentionPolicy;

    private String measurementName;

    private String hostname;

    private static InfluxDB influx;

    /**
     * Constructor.
     *
     * @param config Reporter configuration
     */
    public InfluxReporter(ReporterConfiguration config) {
        this.config = config;

        if (!config.options.containsKey(ADDRESS_PROP)) {
            logger.warn("Not properly configured. Missing influx address. Aborting initialization.");
            return;
        }

        if (!config.options.containsKey(USERNAME_PROP)) {
            logger.warn("Not properly configured. Missing influx username. Aborting initialization.");
            return;
        }

        dbAddress = config.options.get(ADDRESS_PROP);
        username = config.options.getOrDefault(USERNAME_PROP, "");
        password = config.options.getOrDefault(PASSWORD_PROP, "");

        dbName = config.options.getOrDefault(DB_NAME_PROP, DEFAULT_DB_NAME);
        retentionPolicy = config.options.getOrDefault(RETENTION_POLICY_PROP, DEFAULT_RETENTION_POLICY);
        measurementName = config.options.getOrDefault(MEASUREMENT_NAME_PROP, DEFAULT_MEASUREMENT_NAME);
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            return;
        }

        influx = InfluxDBFactory.connect(dbAddress, username, password);
        influx.createDatabase(dbName);
    }

    @Override
    public void report(Query queryReport) {
        if (influx == null) {
            logger.warn("InfluxDB client is not initialized");
            return;
        }

        logger.debug("Sending Query: {}", queryReport.toString());
        try {
            influx.write(dbName, retentionPolicy,
                    Point.measurement(measurementName)
                            .time(queryReport.getStartTimeInMilliseconds(), TimeUnit.MILLISECONDS)
                            .tag("host", hostname).tag("id", UUID.randomUUID().toString())
                            .addField("client", queryReport.getClientAddress())
                            .addField("statement", queryReport.getStatement())
                            .addField("value", queryReport.getExecutionTimeInMilliseconds()).build());
        } catch (Exception e) {
            logger.warn("Failed to send report to influx", e);
        }
    }

}
