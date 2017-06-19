package io.smartcat.cassandra.diagnostics.reporter;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.BaseActorTest;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

public class LogReporterTest extends BaseActorTest {

    private static final String REPORTER_NAME = "io.smartcat.cassandra.diagnostics.reporter.LogReporter";

    @Test
    public void should_initialize() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration();
        final LogReporter reporter = new LogReporter(null, null);
    }

    @Test
    public void should_report_measurement() throws NoSuchMethodException, ClassNotFoundException {
        final Configuration configuration = testConfiguration();
        final LogReporter reporter = new LogReporter(null, null);

        final Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        final Map<String, String> fields = new HashMap<>();
        fields.put("key3", "value3");
        fields.put("key4", "value4");

        final Measurement measurement = Measurement.createSimple("test-measurement", 123.0d, 1234567890L, tags, fields);
        reporter.report(measurement);
    }

    private Configuration testConfiguration() {
        final Configuration configuration = Configuration.getDefaultConfiguration();
        return configuration;
    }

}
