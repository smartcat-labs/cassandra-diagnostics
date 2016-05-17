package io.smartcat.cassandra.diagnostics.report;

import com.google.inject.Inject;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ReporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reporter class that handles initialization of configured reporters and triggers report on each reporter.
 * All reporters are initialized as defined in configuration with LogQueryReporter being default one.
 */
public class CompositeQueryReporter implements QueryReporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CompositeQueryReporter.class);

    private List<QueryReporter> reporters = new ArrayList<QueryReporter>();

    /**
     * Constructor.
     *
     * @param config configuration
     */
    @Inject
    public CompositeQueryReporter(Configuration config) {
        for (ReporterConfiguration reporterConfig : config.reporters) {
            try {
                logger.info("Creating reporter for class name {}", reporterConfig.reporter);
                QueryReporter reporter = (QueryReporter) Class.forName(reporterConfig.reporter)
                        .getConstructor(ReporterConfiguration.class).newInstance(reporterConfig);
                reporters.add(reporter);
            } catch (Exception e) {
                logger.warn("Failed to create reporter by class name", e);
            }
        }
    }

    @Override
    public void report(QueryReport queryReport) {
        logger.info("CompositeQueryReporter: execTime={}", queryReport.executionTimeInMilliseconds);
        for (QueryReporter reporter: reporters) {
            reporter.report(queryReport);
        }
    }

}
