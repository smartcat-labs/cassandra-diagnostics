package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

public class SlowQueryConfigurationTest {

    @Test
    public void testDefaultValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        SlowQueryConfiguration conf = SlowQueryConfiguration.create(options);
        assertThat(conf.logAllQueries()).isTrue();
        assertThat(conf.slowQueryThreshold()).isEqualTo(25);
        assertThat(conf.tablesForLogging()).isEmpty();
    }

    @Test
    public void testProvidedValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("slowQueryThresholdInMilliseconds", 10);
        options.put("logAllQueries", false);
        options.put("tablesForLogging", Arrays.asList("keyspace1.table1", "keyspace2.table2"));

        SlowQueryConfiguration conf = SlowQueryConfiguration.create(options);
        assertThat(conf.logAllQueries()).isFalse();
        assertThat(conf.slowQueryThreshold()).isEqualTo(10);
        assertThat(conf.tablesForLogging().size()).isEqualTo(2);
        assertThat(conf.tablesForLogging()).contains("keyspace1.table1");
        assertThat(conf.tablesForLogging()).contains("keyspace2.table2");
    }

    @Test
    public void testProvidedValuesWithExtras() {
        Map<String, Object> options = new HashMap<>();
        options.put("slowQueryThresholdInMilliseconds", 11);
        options.put("logAllQueries", false);
        options.put("tablesForLogging", Arrays.asList("keyspace.table"));
        options.put("extra", "value");

        try {
            SlowQueryConfiguration.create(options);
            fail("Configuration loading should fail beacuse of incorrect input values.");
        } catch (ConfigurationException e) {
        }
    }
}
