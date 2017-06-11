package io.smartcat.cassandra.diagnostics.reporter;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * A SLF4J based {@link ReporterActor} implementation. This reporter is using log output to print query reports to a
 * log at {@code INFO} level.
 */
public class LogReporter extends ReporterActor {

    /**
     * String template for logging query report.
     */
    private static final String LOG_TEMPLATE = "%s Measurement %s [time=%s, value=%s, tags=%s, fields=%s]";

    /**
     * Constructor.
     *
     * @param reporterName  reporter class name
     * @param configuration reporter configuration
     */
    public LogReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);
    }

    @Override
    protected void report(Measurement measurement) {
        logger.info(String.format(LOG_TEMPLATE, measurement.type, measurement.name.toUpperCase(), measurement.time,
                measurement.value, measurement.tags.toString(), measurement.fields.toString()));
    }

}
