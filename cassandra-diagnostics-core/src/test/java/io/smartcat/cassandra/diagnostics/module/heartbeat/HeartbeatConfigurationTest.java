package io.smartcat.cassandra.diagnostics.module.heartbeat;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

public class HeartbeatConfigurationTest {

    @Test
    public void testDefaultValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        HeartbeatConfiguration conf = HeartbeatConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(15);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void testProvidedValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        HeartbeatConfiguration conf = HeartbeatConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void testProvidedValuesIncorrect() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        try {
            HeartbeatConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstructorException.class);
        }
    }

}
