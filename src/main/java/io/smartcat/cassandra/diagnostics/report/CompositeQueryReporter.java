package io.smartcat.cassandra.diagnostics.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * TBD.
 */
public class CompositeQueryReporter implements QueryReporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CompositeQueryReporter.class);

    /**
     * Constructor.
     *
     * @param config configuration
     */
    @Inject
    public CompositeQueryReporter(Configuration config) {
    }

    @Override
    public void report(QueryReport queryReport) {
        logger.info("MultiQueryReporter {}", queryReport.executionTimeInMilliseconds);
    }

}
