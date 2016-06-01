package io.smartcat.cassandra.diagnostics.report;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Reporter;
import io.smartcat.cassandra.diagnostics.ReporterConfiguration;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reporter class that handles initialization of configured reporters and triggers report on each reporter. All
 * reporters are initialized as defined in configuration with LogQueryReporter being default one.
 */
public class ReporterContext {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ReporterContext.class);

    private List<Reporter> reporters = new ArrayList<Reporter>();

    /**
     * Constructor.
     *
     * @param config configuration
     */
    public ReporterContext(Configuration config) {
        for (ReporterConfiguration reporterConfig : config.reporters) {
            try {
                logger.info("Creating reporter for class name {}", reporterConfig.reporter);
                Reporter reporter = (Reporter) Class.forName(reporterConfig.reporter)
                        .getConstructor(ReporterConfiguration.class).newInstance(reporterConfig);
                reporters.add(reporter);
            } catch (Exception e) {
                logger.warn("Failed to create reporter by class name", e);
            }
        }
    }

    /**
     * Report using all configured reporters.
     *
     * @param queryReport Query report
     */
    public void report(Query queryReport) {
        logger.info("ReporterContext: execTime={}", queryReport.executionTimeInMilliseconds());
        for (Reporter reporter : reporters) {
            reporter.report(queryReport);
        }
    }

}
