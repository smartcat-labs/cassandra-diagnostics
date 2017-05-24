package io.smartcat.cassandra.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class MeasurementTest {

    @Test
    public void should_build_simple_measurement_with_value() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.createSimple("m1", 1.0, 1434055662, TimeUnit.SECONDS, tags, fields);

        assertThat(measurement.isSimple()).isTrue();
        assertThat(measurement.getValue()).isEqualTo(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_building_simple_measurement_without_value() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement.createSimple("m1", null, 1434055662, TimeUnit.SECONDS, tags, fields);
    }

    @Test
    public void should_build_complex_measurement() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.createComplex("m1", 1434055662, TimeUnit.SECONDS, tags, fields);

        assertThat(measurement.isComplex()).isTrue();
    }

    @Test(expected = IllegalStateException.class)
    public void should_fail_fetching_value_from_complex_measurement() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.createComplex("m1", 1434055662, TimeUnit.SECONDS, tags, fields);

        measurement.getValue();
    }

}
