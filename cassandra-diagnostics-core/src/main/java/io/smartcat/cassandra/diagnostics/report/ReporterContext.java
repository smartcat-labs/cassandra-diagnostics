package io.smartcat.cassandra.diagnostics.report;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.Reporter;
import io.smartcat.cassandra.diagnostics.ReporterConfiguration;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.connector.Query;

/**
 * Reporter class that handles initialization of configured reporters and triggers report on each reporter.
 * All reporters are initialized as defined in configuration with LogQueryReporter being default one.
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
    @Inject
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
     * @param queryReport Query report
     */
    public void report(Query queryReport) {
        logger.info("ReporterContext: execTime={}", queryReport.executionTimeInMilliseconds);
        reporters.forEach(reporter -> reporter.report(queryReport));
    }

}
