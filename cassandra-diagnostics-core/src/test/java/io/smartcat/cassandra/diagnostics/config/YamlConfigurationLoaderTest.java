package io.smartcat.cassandra.diagnostics.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test for yaml configuration loader.
 *
 */
public class YamlConfigurationLoaderTest {

    @Test
    public void defaultConfigurationLoadTest() throws ConfigurationException {
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertNotNull(configuration);
    }

}
