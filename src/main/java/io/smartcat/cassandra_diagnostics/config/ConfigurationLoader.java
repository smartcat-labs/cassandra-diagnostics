package io.smartcat.cassandra_diagnostics.config;

import java.net.URL;

public interface ConfigurationLoader {
	Configuration loadConfig() throws ConfigurationException;
	Configuration loadConfig(URL url) throws ConfigurationException;
}