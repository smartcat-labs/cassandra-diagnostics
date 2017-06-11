package io.smartcat.cassandra.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.measurement.Measurement;

public class MeasurementTest {

    @Test
    public void should_build_simple_measurement_with_value() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.createSimple("m1", 1.0, 1434055662000L, tags, fields);

        assertThat(measurement.isSimple()).isTrue();
        assertThat(measurement.value).isEqualTo(1.0);
        assertThat(measurement.fields.size()).isEqualTo(1);
    }

    @Test
    public void should_build_simple_measurement_with_value_without_fields() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Measurement measurement = Measurement.createSimple("m1", 1.0, 1434055662000L, tags);

        assertThat(measurement.isSimple()).isTrue();
        assertThat(measurement.value).isEqualTo(1.0);
        assertThat(measurement.fields.isEmpty()).isTrue();
    }

    @Test
    public void should_build_complex_measurement() {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.createComplex("m1", 1434055662000L, tags, fields);

        assertThat(measurement.isSimple()).isFalse();
    }

}
