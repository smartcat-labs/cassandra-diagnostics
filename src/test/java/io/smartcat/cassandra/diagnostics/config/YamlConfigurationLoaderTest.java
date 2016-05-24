package io.smartcat.cassandra.diagnostics.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class YamlConfigurationLoaderTest {

    @Test
    public void default_configuration_load_test() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();

        assertThat(configuration).isNotNull();
    }

    @Test
    public void loads_tables_to_use() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        loader.loadConfig();
        Configuration configuration = loader.loadConfig();

        assertThat(configuration.tables).hasSize(1);
        assertThat(configuration.tables).extracting("table").contains("some_table");
        assertThat(configuration.tables).extracting("keyspace").contains("some_keyspace");
    }

}
