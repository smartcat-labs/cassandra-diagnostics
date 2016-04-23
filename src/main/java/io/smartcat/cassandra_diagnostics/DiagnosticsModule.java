package io.smartcat.cassandra_diagnostics;

import com.google.inject.AbstractModule;

import io.smartcat.cassandra_diagnostics.config.Configuration;
import io.smartcat.cassandra_diagnostics.config.ConfigurationException;
import io.smartcat.cassandra_diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra_diagnostics.config.YamlConfigurationLoader;
import io.smartcat.cassandra_diagnostics.report.LogQueryReporter;
import io.smartcat.cassandra_diagnostics.report.QueryReporter;

public class DiagnosticsModule extends AbstractModule {

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		ConfigurationLoader loader = new YamlConfigurationLoader();
		Configuration config;
		
		try {
			config = loader.loadConfig();
		} catch (ConfigurationException e) {
			config = new Configuration();
		}		
		
		bind(ConfigurationLoader.class).toInstance(loader);
		bind(Configuration.class).toInstance(config);
		
		try {
			bind(QueryReporter.class).to((Class<? extends QueryReporter>) Class.forName(config.query_reporter_class));
		} catch (ClassNotFoundException e) {
			bind(QueryReporter.class).to(LogQueryReporter.class);
		}		
	}

}
