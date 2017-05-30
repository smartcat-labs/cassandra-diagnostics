package io.smartcat.cassandra.diagnostics.module.hiccup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

public class HiccupConfigurationTest {

    @Test
    public void loads_default_configuration() throws ConfigurationException {
        Map<String, Object> options = new HashMap<>();
        HiccupConfiguration conf = HiccupConfiguration.create(options);
        assertThat(conf.resolutionInMs()).isEqualTo(1.0d);
        assertThat(conf.startDelayInMs()).isEqualTo(30000);
        assertThat(conf.allocateObjects()).isEqualTo(false);
        assertThat(conf.lowestTrackableValueInNanos()).isEqualTo(1000L * 20L);
        assertThat(conf.highestTrackableValueInNanos()).isEqualTo(3600 * 1000L * 1000L * 1000L);
        assertThat(conf.numberOfSignificantValueDigits()).isEqualTo(2);
        assertThat(conf.period()).isEqualTo(5);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void provides_all_values() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("resolutionInMs", 1000.0d);
        options.put("startDelayInMs", 10000);
        options.put("allocateObjects", true);
        options.put("lowestTrackableValueInNanos", 250L);
        options.put("highestTrackableValueInNanos", 100000);
        options.put("numberOfSignificantValueDigits", 10);
        options.put("period", 2);
        options.put("timeunit", "SECONDS");
        HiccupConfiguration conf = HiccupConfiguration.create(options);
        assertThat(conf.resolutionInMs()).isEqualTo(1000.0d);
        assertThat(conf.startDelayInMs()).isEqualTo(10000);
        assertThat(conf.allocateObjects()).isEqualTo(true);
        assertThat(conf.lowestTrackableValueInNanos()).isEqualTo(250);
        assertThat(conf.highestTrackableValueInNanos()).isEqualTo(100000);
        assertThat(conf.numberOfSignificantValueDigits()).isEqualTo(10);
        assertThat(conf.period()).isEqualTo(2);
        assertThat(conf.timeunit()).isEqualTo(TimeUnit.SECONDS);
        assertThat(conf.reportingIntervalInMillis()).isEqualTo(2000);
    }

}
