package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class SlowQueryModuleTest {

    @Test
    public void should_transform() throws ConfigurationException {
        ModuleConfiguration conf = new ModuleConfiguration();
        TestReporter reporter = new TestReporter(null);
        SlowQueryModule module = new SlowQueryModule(conf, testReporters(reporter));

        Query query = Query
                .create(1474741407205L, 234L, "/127.0.0.1:40042", Query.StatementType.SELECT, "keyspace", "table",
                        "select count(*) from keyspace.table", null);

        module.process(query);

        Measurement measurement = reporter.reported.get(0);

        assertThat(measurement.fields().keySet()).isEqualTo(Sets.newSet("statement", "client"));
        assertThat(measurement.fields().get("statement")).isEqualTo("select count(*) from keyspace.table");
        assertThat(measurement.fields().get("client")).isEqualTo("/127.0.0.1:40042");
        assertThat(measurement.value()).isEqualTo(234);

        assertThat(measurement.tags().keySet()).isEqualTo(Sets.newSet("host", "id", "statementType"));
        assertThat(measurement.tags().get("statementType")).isEqualTo("SELECT");
    }

    private List<Reporter> testReporters(final Reporter reporter) {
        return new ArrayList<Reporter>() {
            {
                add(reporter);
            }
        };
    }
}
