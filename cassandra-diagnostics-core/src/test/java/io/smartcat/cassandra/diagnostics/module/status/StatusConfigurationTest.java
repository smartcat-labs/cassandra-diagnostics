package io.smartcat.cassandra.diagnostics.module.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

/**
 * Status module configuration test.
 */
public class StatusConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        StatusConfiguration conf = StatusConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(1);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.MINUTES);
        assertThat(conf.compactionsEnabled()).isFalse();
        assertThat(conf.tpStatsEnabled()).isFalse();
        assertThat(conf.repairsEnabled()).isFalse();
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "HOURS");
        options.put("compactionsEnabled", true);
        options.put("tpStatsEnabled", true);
        options.put("repairsEnabled", true);
        options.put("nodeInfoEnabled", true);
        StatusConfiguration conf = StatusConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.HOURS);
        assertThat(conf.compactionsEnabled()).isTrue();
        assertThat(conf.tpStatsEnabled()).isTrue();
        assertThat(conf.repairsEnabled()).isTrue();
        assertThat(conf.nodeInfoEnabled()).isTrue();
    }

    @Test
    public void fails_when_incorrect_values_provided() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        options.put("compactionsEnabled", "123");
        try {
            StatusConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstructorException.class);
        }
    }

}
