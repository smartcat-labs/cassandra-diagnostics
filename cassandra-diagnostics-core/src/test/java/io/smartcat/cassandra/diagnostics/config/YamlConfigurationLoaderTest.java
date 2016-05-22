package io.smartcat.cassandra.diagnostics.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class YamlConfigurationLoaderTest {

    @Test
    public void defaultConfigurationLoadTest() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertNotNull(configuration);
    }

}
