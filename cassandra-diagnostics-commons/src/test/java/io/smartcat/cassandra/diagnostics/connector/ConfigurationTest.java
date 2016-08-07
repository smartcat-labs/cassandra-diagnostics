package io.smartcat.cassandra.diagnostics.connector;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void deafult_configuration() {
        ConnectorConfiguration conf = ConnectorConfiguration.getDefault();
        assertThat(conf.queuedEventsOverflowThreshold).isEqualTo(1000);
        assertThat(conf.numWorkerThreads).isEqualTo(2);
    }

}
