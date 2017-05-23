package io.smartcat.cassandra.diagnostics.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A SLF4J based {@link Reporter} implementation. This reporter is using {@link Logger} to print query reports to a log
 * at {@code INFO} level.
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
     * @param configuration        configuration
     * @param globalConfiguration  Global diagnostics configuration
     */
    public LogReporter(ReporterConfiguration configuration, GlobalConfiguration globalConfiguration) {
        super(configuration, globalConfiguration);
    }

    @Override
    public void report(Measurement measurement) {
        logger.info(LOG_TEMPLATE, measurement.type().toString(), measurement.name().toUpperCase(), measurement.time(),
                measurement.getOrDefault(0d), measurement.tags().toString(), measurement.fields().toString());
    }

}
