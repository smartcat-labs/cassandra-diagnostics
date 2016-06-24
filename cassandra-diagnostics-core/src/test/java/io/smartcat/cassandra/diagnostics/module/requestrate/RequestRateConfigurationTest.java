package io.smartcat.cassandra.diagnostics.module.requestrate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

public class RequestRateConfigurationTest {

    @Test
    public void loads_default_configuration() throws Exception {
        Map<String, Object> options = new HashMap<>();
        RequestRateConfiguration conf = RequestRateConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(1);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.separateByRequestType()).isEqualTo(true);
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        options.put("separateByRequestType", "true");
        RequestRateConfiguration conf = RequestRateConfiguration.create(options);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.separateByRequestType()).isEqualTo(true);
    }

    @Test
    public void fails_when_incorrect_values_provided() {
        Map<String, Object> options = new HashMap<>();
        options.put("period", 2);
        options.put("timeunit", "ERR");
        options.put("separateByRequestType", "tru");
        try {
            RequestRateConfiguration.create(options);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstructorException.class);
        }
    }

}
