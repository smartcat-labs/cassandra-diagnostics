package io.smartcat.cassandra_diagnostics.jmx;

import io.smartcat.cassandra_diagnostics.config.Configuration;

public class DiagnosticsMXBeanImpl implements DiagnosticsMXBean {
	
	private Configuration config;

	public DiagnosticsMXBeanImpl(Configuration config) {
		this.config = config;
	}
	
	@Override
	public long getSlowQueryTreshold() {
		return config.slow_query_threshold;
	}
	
	@Override
	public void setSlowQueryTreshold(long value) {
		config.slow_query_threshold = value;
	}
	
}