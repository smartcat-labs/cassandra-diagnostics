package io.smartcat.cassandra.diagnostics.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Test for yaml configuration loader.
 *
 */
public class YamlConfigurationLoaderTest {

    @Test
    public void loads_default_configuration() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration).isNotNull();
    }

    @Test
    public void load_invalid_external_configuratio_uri() {
        System.setProperty("cassandra.diagnostics.config", "invalid-cassandra-diagnostics-path.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        ConfigurationException exception = null;
        try {
            loader.loadConfig();
        } catch (ConfigurationException e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
    }

    @Test
    public void load_invalid_external_configuratio() {
        System.setProperty("cassandra.diagnostics.config", "invalid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        ConfigurationException exception = null;
        try {
            loader.loadConfig();
        } catch (ConfigurationException e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
    }

    @Test
    public void load_external_valid_configuration() throws ConfigurationException {
        System.setProperty("cassandra.diagnostics.config", "valid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration.global.hostname).isEqualTo("test-hostname");
        assertThat(configuration.global.systemName).isEqualTo("smartcat-cassandra-cluster");
        assertThat(configuration.global.httpApiEnabled).isTrue();
        assertThat(configuration.global.httpApiHost).isEqualTo("10.0.0.1");
        assertThat(configuration.global.httpApiPort).isEqualTo(8001);
    }
}
