package io.smartcat.cassandra.diagnostics.reporter;

import org.slf4j.Logger;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.actor.ReporterActor;

/**
 * A SLF4J based {@link Reporter} implementation. This reporter is using {@link Logger} to print query reports to a log
 * at {@code INFO} level.
 */
public class LogReporter extends ReporterActor {

    /**
     * Class logger.
     */
    //    private static final Logger logger = LoggerFactory.getLogger(LogReporter.class);

    /**
     * String template for logging query report.
     */
    //    private static final String LOG_TEMPLATE = "{} Measurement {} [time={}, value={}, tags={}, fields={}]";
    private static final String LOG_TEMPLATE = "%s Measurement %s [time=%s, value=%s, tags=%s, fields=%s]";

    /**
     * Constructor.
     *
     * @param configuration reporter configuration
     */
    public LogReporter(ReporterConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void report(Measurement measurement) {
        logger.info(String.format(LOG_TEMPLATE, measurement.type().toString(), measurement.name().toUpperCase(),
                measurement.time(), measurement.getOrDefault(0d), measurement.tags().toString(),
                measurement.fields().toString()));
    }

}
