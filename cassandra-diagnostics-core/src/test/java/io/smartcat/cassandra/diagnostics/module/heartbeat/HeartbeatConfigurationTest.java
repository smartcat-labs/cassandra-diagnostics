package io.smartcat.cassandra.diagnostics.module.heartbeat;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

public class HeartbeatConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        HeartbeatConfiguration conf = HeartbeatConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(15);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        HeartbeatConfiguration conf = HeartbeatConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void fails_when_incorrect_values_provided() {
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
