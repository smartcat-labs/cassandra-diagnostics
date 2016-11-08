package io.smartcat.diagnostics.reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.DatadogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

@Ignore
public class DatadogReporterTest {

    @Test
    public void should_send_measurement() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("apiKey", "123");
        config.options.put("udpPort", "8215");
        final DatadogReporter reporter = new DatadogReporter(config);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        final Measurement measurement = Measurement
                .create("test-metric", 909, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        reporter.report(measurement);
    }

}
