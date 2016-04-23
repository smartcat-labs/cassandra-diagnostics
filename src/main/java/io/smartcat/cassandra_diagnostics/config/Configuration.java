package io.smartcat.cassandra_diagnostics.config;

import io.smartcat.cassandra_diagnostics.report.LogQueryReporter;

public class Configuration {

	/**
	 * Query execution time threshold. A query whose execution time is grater than this value
	 * is reported. The execution time is expressed in nanoseconds.
	 */
	public long slow_query_threshold = 50 * 1000 * 1000; // 50ms (given in nanoseconds)
	
	/**
	 * A fully qualified Java class name used for reporting slow queries. {@code LogQueryReporter}
	 * is the default value.
	 */
	public String query_reporter_class = LogQueryReporter.class.getName();

}
