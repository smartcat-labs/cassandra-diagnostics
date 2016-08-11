package io.smartcat.cassandra.diagnostics.config;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class ConfigurationTest {

    @Test
    public void deafult_configuration() {
        Configuration conf = Configuration.getDefaultConfiguration();
        assertThat(conf.connector.queuedEventsOverflowThreshold).isEqualTo(1000);
        assertThat(conf.connector.numWorkerThreads).isEqualTo(2);
    }

}
