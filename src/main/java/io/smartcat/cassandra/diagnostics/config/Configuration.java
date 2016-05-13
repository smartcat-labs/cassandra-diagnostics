package io.smartcat.cassandra.diagnostics.config;

import java.util.HashMap;
import java.util.Map;

import io.smartcat.cassandra.diagnostics.report.LogQueryReporter;

/**
 * This class represents the Cassandra Diagnostics configuration.
 */
public class Configuration {

  /**
   * Query execution time threshold. A query whose execution time is grater than
   * this value is reported. The execution time is expressed in nanoseconds.
   */
  public long slowQueryThreshold = 50 * 1000 * 1000; // 50ms (given in
                                                       // nanoseconds)

  /**
   * A fully qualified Java class name used for reporting slow queries.
   * {@code LogQueryReporter} is the default value.
   */
  public String reporter = LogQueryReporter.class.getName();

  /**
   * A map containing optional reporter specific options.
   */
  public Map<String, String> reporterOptions = new HashMap<String, String>();

  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ slowQueryThreshold: ").append(slowQueryThreshold)
        .append(", reporter: \"").append(reporter)
        .append("\", reporterOptions: ").append(reporterOptions)
        .append(" }");
      return sb.toString();
  }

}
