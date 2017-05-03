package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

public class RequestRateConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        RequestRateConfiguration conf = RequestRateConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(1);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.requestsToReport()).hasSize(1).contains("*:*");
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        options.put("requestsToReport", Arrays.asList("SELECT:ALL"));
        RequestRateConfiguration conf = RequestRateConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.requestsToReport()).hasSize(1).contains("SELECT:ALL");
    }

    @Test
    public void fails_when_incorrect_values_provided() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        try {
            RequestRateConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConfigurationException.class);
        }
    }

    @Test
    public void fails_when_incorrect_requests_to_report() {
        Map<String, Object> options = new HashMap<>();
        options.put("requestsToReport", Arrays.asList("SOMETHING"));
        try {
            RequestRateConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConfigurationException.class)
                    .hasMessage("Only two configuration parameters supported, statement type and consistency level.");
        }
    }

    @Test
    public void fails_when_incorrect_statement_type() {
        Map<String, Object> options = new HashMap<>();
        options.put("requestsToReport", Arrays.asList("SOMETHING:LOCAL_ONE"));
        try {
            RequestRateConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConfigurationException.class)
                    .hasMessage("Illegal statement type configured: SOMETHING");
        }
    }

    @Test
    public void fails_when_incorrect_consistency_level() {
        Map<String, Object> options = new HashMap<>();
        options.put("requestsToReport", Arrays.asList("SELECT:SOMETHING"));
        try {
            RequestRateConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConfigurationException.class)
                    .hasMessage("Illegal consistency level configured: SOMETHING");
        }
    }

}
