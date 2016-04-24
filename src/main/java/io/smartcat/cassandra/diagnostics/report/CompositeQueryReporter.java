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
   * Configuration.
   */
  private Configuration config;

  /**
   * Constructor.
   *
   * @param config configuration
   */
  @Inject
  public CompositeQueryReporter(Configuration config) {
    this.config = config;
  }

  @Override
  public void report(QueryReport queryReport) {
    logger.info("MultiQueryReporter {}", queryReport.executionTime);
  }

}
