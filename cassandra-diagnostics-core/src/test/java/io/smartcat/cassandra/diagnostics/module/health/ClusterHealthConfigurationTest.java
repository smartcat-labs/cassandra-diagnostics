package io.smartcat.cassandra.diagnostics.module.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

/**
 * Cluster health module configuration test.
 */
public class ClusterHealthConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        ClusterHealthConfiguration conf = ClusterHealthConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(1);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.MINUTES);
        assertThat(conf.numberOfUnreachableNodesEnabled()).isFalse();
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "HOURS");
        options.put("numberOfUnreachableNodesEnabled", true);
        ClusterHealthConfiguration conf = ClusterHealthConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.HOURS);
        assertThat(conf.numberOfUnreachableNodesEnabled()).isTrue();
    }

    @Test
    public void fails_when_incorrect_values_provided() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        options.put("numberOfUnreachableNodesEnabled", "123");
        try {
            ClusterHealthConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstructorException.class);
        }
    }

}
