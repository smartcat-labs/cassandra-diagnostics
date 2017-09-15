package io.smartcat.diagnostics.reporter;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.timgroup.statsd.StatsDClient;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.DatadogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class DatadogReporterTest {

    private DatadogReporter datadogReporterWithMockClient;
    private StatsDClient mockClient;

    @Before
    public void initialize() throws NoSuchFieldException, SecurityException, Exception {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("statsDHost", "localhost");
        config.options.put("statsDPort", 9876);
        config.options.put("keysPrefix", "prefix");
        config.options.put("fixedTags", Arrays.asList("host:somehost,tag2:two,tag3:three"));
        datadogReporterWithMockClient = new DatadogReporter(config, GlobalConfiguration.getDefault());
        mockClient = mock(StatsDClient.class);
        setField(datadogReporterWithMockClient, datadogReporterWithMockClient.getClass().getDeclaredField("client"),
                mockClient);
    }

    @Test(expected = ClassCastException.class)
    public void should_fail_to_load_configuration_port() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("statsDPort", "NONE");

        final DatadogReporter reporter = new DatadogReporter(config, GlobalConfiguration.getDefault());

        reporter.stop();

        assert false;
    }

    @Test(expected = ClassCastException.class)
    public void should_fail_to_load_configuration_tags() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("fixedTags", new Integer(0));

        final DatadogReporter reporter = new DatadogReporter(config, GlobalConfiguration.getDefault());

        reporter.stop();

        assert false;
    }

    @Test
    public void should_send_measurement() throws Exception {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v2", "abc");

        final Measurement measurement = Measurement
                .createSimple("test-metric", 909.0, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        datadogReporterWithMockClient.report(measurement);

        verify(mockClient).recordGaugeValue(eq(measurement.name()), eq(measurement.getValue()), eq("tag1:tv1"),
                eq("tag2:tv2"));
    }

    @Test
    public void should_send_measurement_for_each_field_in_complex_measurement() throws Exception {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v1", "15.0");
        fields.put("v2", "26.0");
        fields.put("v3", "50.0");
        fields.put("v4", "1234.0");

        final Measurement measurement = Measurement
                .createComplex("test-metric", System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        datadogReporterWithMockClient.report(measurement);

        verify(mockClient, times(4)).recordGaugeValue(anyString(), anyDouble(), anyString(), anyString());
    }

    @Test
    public void should_skip_non_number_fields_in_complex_measurement() throws Exception {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v1", "1a");
        fields.put("v2", "df");
        fields.put("v3", ".");
        fields.put("v4", "1234.0");
        fields.put("v5", "-");

        final Measurement measurement = Measurement
                .createComplex("test-metric", System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        datadogReporterWithMockClient.report(measurement);

        verify(mockClient).recordGaugeValue(eq(measurement.name() + ".v4"),
                eq(Double.parseDouble(measurement.fields().get("v4"))), eq("tag1:tv1"), eq("tag2:tv2"));
    }

    private void setField(Object target, Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        field.set(target, newValue);
    }
}
