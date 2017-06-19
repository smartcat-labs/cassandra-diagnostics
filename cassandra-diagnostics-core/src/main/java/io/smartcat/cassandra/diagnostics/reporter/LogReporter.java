package io.smartcat.cassandra.diagnostics.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.config.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * A SLF4J based {@link Reporter} implementation. This reporter is using log output to print query reports to a
 * log at {@code INFO} level.
 */
public class LogReporter extends Reporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogReporter.class);

    /**
     * String template for logging query report.
     */
    private static final String LOG_TEMPLATE = "{} Measurement {} [time={}, value={}, tags={}, fields={}]";

    /**
     * Constructor.
     *
     * @param reporterConfiguration reporter specific configuration
     * @param globalConfiguration   global configuration
     */
    public LogReporter(final ReporterConfiguration reporterConfiguration,
            final GlobalConfiguration globalConfiguration) {
        super(reporterConfiguration, globalConfiguration);
    }

    @Override
    public void report(Measurement measurement) {
        logger.info(LOG_TEMPLATE, measurement.type.toString(), measurement.name.toUpperCase(), measurement.time,
                measurement.value, measurement.tags.toString(), measurement.fields.toString());
    }

}
