package io.smartcat.cassandra.diagnostics.reporter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * An InfluxDB based {@link ReporterActor} implementation. Query reports are sent to influxdb.
 */
public class InfluxReporter extends ReporterActor {

    private static final String ADDRESS_PROP = "influxDbAddress";

    private static final String USERNAME_PROP = "influxUsername";

    private static final String PASSWORD_PROP = "influxPassword";

    private static final String DB_NAME_PROP = "influxDbName";

    private static final String POINTS_IN_BATCH_PROP = "influxPointsInBatch";

    private static final int DEFAULT_POINTS_IN_BATCH = 1000;

    private static final String FLUSH_PERIOD_IN_SECONDS_PROP = "influxFlushPeriodInSeconds";

    private static final int DEFAULT_FLUSH_PERIOD_IN_SECONDS = 5;

    private static final String DEFAULT_DB_NAME = "cassandradb";

    private static final String RETENTION_POLICY_PROP = "influxRetentionPolicy";

    private static final String DEFAULT_RETENTION_POLICY = "autogen";

    private String dbAddress;

    private String username;

    private String password;

    private String dbName;

    private String retentionPolicy;

    private static InfluxDB influx;

    /**
     * Constructor.
     *
     * @param reporterName  Reporter class name
     * @param configuration Configuration
     */
    public InfluxReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);

        if (!reporterConfiguration.options.containsKey(ADDRESS_PROP)) {
            logger.warning("Not properly configured. Missing influx address. Aborting initialization.");
            return;
        }

        if (!reporterConfiguration.options.containsKey(USERNAME_PROP)) {
            logger.warning("Not properly configured. Missing influx username. Aborting initialization.");
            return;
        }

        if (!reporterConfiguration.options.containsKey(DB_NAME_PROP)) {
            logger.warning("Not properly configured. Missing influx db name. Aborting initialization.");
            return;
        }

        dbAddress = reporterConfiguration.getOption(ADDRESS_PROP);
        username = reporterConfiguration.getDefaultOption(USERNAME_PROP, "");
        password = reporterConfiguration.getDefaultOption(PASSWORD_PROP, "");

        dbName = reporterConfiguration.getDefaultOption(DB_NAME_PROP, DEFAULT_DB_NAME);
        retentionPolicy = reporterConfiguration.getDefaultOption(RETENTION_POLICY_PROP, DEFAULT_RETENTION_POLICY);

        influx = InfluxDBFactory.connect(dbAddress, username, password);
        influx.createDatabase(dbName);

        final int pointsInBatch = reporterConfiguration.getDefaultOption(POINTS_IN_BATCH_PROP, DEFAULT_POINTS_IN_BATCH);
        final int flushPeriodInSeconds = reporterConfiguration
                .getDefaultOption(FLUSH_PERIOD_IN_SECONDS_PROP, DEFAULT_FLUSH_PERIOD_IN_SECONDS);

        influx.enableBatch(pointsInBatch, flushPeriodInSeconds, TimeUnit.SECONDS);
    }

    @Override
    protected void stop() {
        influx.close();
    }

    @Override
    protected void report(Measurement measurement) {
        if (influx == null) {
            logger.warning("InfluxDB client is not initialized");
            return;
        }

        logger.debug("Sending Query: {}", measurement.toString());
        try {
            final Point.Builder builder = Point.measurement(measurement.name);
            builder.time(measurement.time, TimeUnit.MILLISECONDS);
            builder.tag("type", measurement.type.toString());
            for (Map.Entry<String, String> tag : measurement.tags.entrySet()) {
                builder.tag(tag.getKey(), tag.getValue());
            }
            for (Map.Entry<String, String> field : measurement.fields.entrySet()) {
                builder.addField(field.getKey(), field.getValue());
            }
            if (measurement.isSimple()) {
                builder.addField("value", measurement.value);
            }

            influx.write(dbName, retentionPolicy, builder.build());
        } catch (Exception e) {
            logger.warning("Failed to send report to influx", e);
        }
    }

}
