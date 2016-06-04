package io.smartcat.cassandra.diagnostics.reporter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * An InfluxDB based {@link Reporter} implementation. Query reports are sent to influxdb.
 */
public class InfluxReporter extends Reporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(InfluxReporter.class);

    private static final String ADDRESS_PROP = "influxDbAddress";

    private static final String USERNAME_PROP = "influxUsername";

    private static final String PASSWORD_PROP = "influxPassword";

    private static final String DB_NAME_PROP = "influxDbName";

    private static final String DEFAULT_DB_NAME = "slowQueries";

    private static final String RETENTION_POLICY_PROP = "influxRetentionPolicy";

    private static final String DEFAULT_RETENTION_POLICY = "default";

    private String dbAddress;

    private String username;

    private String password;

    private String dbName;

    private String retentionPolicy;

    private String hostname;

    private static InfluxDB influx;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public InfluxReporter(ReporterConfiguration configuration) {
        super(configuration);

        if (!configuration.options.containsKey(ADDRESS_PROP)) {
            logger.warn("Not properly configured. Missing influx address. Aborting initialization.");
            return;
        }

        if (!configuration.options.containsKey(USERNAME_PROP)) {
            logger.warn("Not properly configured. Missing influx username. Aborting initialization.");
            return;
        }

        dbAddress = configuration.options.get(ADDRESS_PROP);
        username = configuration.options.getOrDefault(USERNAME_PROP, "");
        password = configuration.options.getOrDefault(PASSWORD_PROP, "");

        dbName = configuration.options.getOrDefault(DB_NAME_PROP, DEFAULT_DB_NAME);
        retentionPolicy = configuration.options.getOrDefault(RETENTION_POLICY_PROP, DEFAULT_RETENTION_POLICY);
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
    public void report(Measurement measurement) {
        if (influx == null) {
            logger.warn("InfluxDB client is not initialized");
            return;
        }

        logger.debug("Sending Query: {}", measurement.toString());
        try {
            influx.write(dbName, retentionPolicy, Point.measurement(measurement.name())
                    .time(measurement.query().startTimeInMilliseconds(), TimeUnit.MILLISECONDS).tag("host", hostname)
                    .tag("id", UUID.randomUUID().toString()).addField("client", measurement.query().clientAddress())
                    .addField("statement", measurement.query().statement())
                    .addField("value", measurement.query().executionTimeInMilliseconds()).build());
        } catch (Exception e) {
            logger.warn("Failed to send report to influx", e);
        }
    }

}
