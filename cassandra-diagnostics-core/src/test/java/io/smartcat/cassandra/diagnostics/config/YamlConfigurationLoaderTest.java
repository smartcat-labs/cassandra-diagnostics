package io.smartcat.cassandra.diagnostics.config;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class YamlConfigurationLoaderTest {

    @Test
    public void defaultConfigurationLoadTest() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertNotNull(configuration);
    }

}
