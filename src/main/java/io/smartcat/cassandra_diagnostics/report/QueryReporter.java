package io.smartcat.cassandra_diagnostics.report;

public interface QueryReporter {
	void report(QueryReport queryReport);
}
