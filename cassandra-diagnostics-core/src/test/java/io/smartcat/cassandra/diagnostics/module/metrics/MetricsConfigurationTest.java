package io.smartcat.cassandra.diagnostics.module.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

public class MetricsConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        MetricsConfiguration conf = MetricsConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(1);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.jmxHost()).isEqualTo("127.0.0.1");
        assertThat(conf.jmxPort()).isEqualTo(7199);
        assertThat(conf.jmxSslEnabled()).isEqualTo(false);
        assertThat(conf.metricsPackageName()).isEqualTo("org.apache.cassandra.metrics");
        assertThat(conf.metricsPatterns()).isNotNull();
        assertThat(conf.metricsPatterns()).isEmpty();
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        MetricsConfiguration conf = MetricsConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void fails_when_incorrect_values_provided() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        try {
            MetricsConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstructorException.class);
        }
    }

}
