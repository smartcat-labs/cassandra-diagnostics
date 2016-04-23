package io.smartcat.cassandra_diagnostics.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.smartcat.cassandra_diagnostics.config.Configuration;

public class CompositeQueryReporter implements QueryReporter {

	private static final Logger logger = LoggerFactory.getLogger(CompositeQueryReporter.class);
	
	private Configuration config;
	
	@Inject
	public CompositeQueryReporter(Configuration config) {
		this.config = config;
	}

	@Override
	public void report(QueryReport queryReport) {
		logger.info("MultiQueryReporter {}", queryReport.executionTime);
	}

}
