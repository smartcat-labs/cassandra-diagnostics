package io.smartcat.cassandra_diagnostics.jmx;

public interface DiagnosticsMXBean {
	long getSlowQueryTreshold();
	void setSlowQueryTreshold(long value);
}