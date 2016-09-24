package io.smartcat.cassandra.diagnostics.module.slowquery;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

public class SlowQueryModuleTest {

    private SlowQueryModule module = null;

    @Before
    public void init() throws ConfigurationException {
        ModuleConfiguration conf = new ModuleConfiguration();
        Reporter reporter = new TestReporter(new ReporterConfiguration());

        module = new SlowQueryModule(conf, Collections.singletonList(reporter));
    }

    @Test
    public void testTransform() {
        Query query = Query.create(1474741407205L, 234L, "/127.0.0.1:40042",
                Query.StatementType.SELECT, "keyspace", "table", "select count(*) from keyspace.table", null);

        Measurement measurement = module.transform(query);

        assertThat(measurement.fields().keySet()).isEqualTo(Sets.newSet("statement", "client"));
        assertThat(measurement.fields().get("statement")).isEqualTo("select count(*) from keyspace.table");
        assertThat(measurement.fields().get("client")).isEqualTo("/127.0.0.1:40042");
        assertThat(measurement.value()).isEqualTo(234);

        assertThat(measurement.tags().keySet()).isEqualTo(Sets.newSet("host", "id", "statementType"));
        assertThat(measurement.tags().get("statementType")).isEqualTo("SELECT");
    }
}
