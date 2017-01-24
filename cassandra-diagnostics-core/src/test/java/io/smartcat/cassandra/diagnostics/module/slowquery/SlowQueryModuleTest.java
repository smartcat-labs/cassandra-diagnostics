package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.LatchTestReporter;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.TestReporter;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

public class SlowQueryModuleTest {

    private static final String SLOW_QUERY_MESUREMENT_NAME = "slow_query";

    private static final String SLOW_QUERY_COUNT_SUFIX = "_count";

    @Test
    public void should_transform() throws ConfigurationException {
        ModuleConfiguration conf = new ModuleConfiguration();
        TestReporter reporter = new TestReporter(null);
        SlowQueryModule module = new SlowQueryModule(conf, testReporters(reporter));

        Query query = Query
                .create(1474741407205L, 234L, "/127.0.0.1:40042", Query.StatementType.SELECT, "keyspace", "table",
                        "select count(*) from keyspace.table");

        module.process(query);

        Measurement measurement = reporter.reported.get(0);

        assertThat(measurement.fields().keySet()).isEqualTo(Sets.newSet("statement", "client"));
        assertThat(measurement.fields().get("statement")).isEqualTo("select count(*) from keyspace.table");
        assertThat(measurement.fields().get("client")).isEqualTo("/127.0.0.1:40042");
        assertThat(measurement.value()).isEqualTo(234);

        assertThat(measurement.tags().keySet()).isEqualTo(Sets.newSet("host", "id", "statementType"));
        assertThat(measurement.tags().get("statementType")).isEqualTo("SELECT");
    }

    @Test
    public void should_report_number_of_slow_queries() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(110);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);

        final SlowQueryModule module = new SlowQueryModule(testConfiguration(1), reporters);

        final long numberOfSlowQueries = 100;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        long reportedMeasurementCount = 0;
        while (reportedMeasurementCount < numberOfSlowQueries) {
            reportedMeasurementCount = 0;
            for (final Measurement measurement : latchTestReporter.getReported()) {
                if (measurement.name().equals(SLOW_QUERY_MESUREMENT_NAME)) {
                    reportedMeasurementCount++;
                }
            }
            latch.await(1100, TimeUnit.MILLISECONDS);
        }

        module.stop();

        long slowQueryCounts = 0;
        for (final Measurement measurement : latchTestReporter.getReported()) {
            if (measurement.name().equals(SLOW_QUERY_MESUREMENT_NAME + SLOW_QUERY_COUNT_SUFIX)) {
                slowQueryCounts += measurement.value();
            }
        }

        assertThat(numberOfSlowQueries).isEqualTo(reportedMeasurementCount);
        assertThat(numberOfSlowQueries).isEqualTo(slowQueryCounts);
    }

    private ModuleConfiguration testConfiguration(final int period) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryCountReportEnabled", true);
        configuration.options.put("slowQueryCountReportPeriod", period);
        configuration.options.put("slowQueryCountReportTimeunit", "SECONDS");
        return configuration;
    }

    private List<Reporter> testReporters(final Reporter reporter) {
        return new ArrayList<Reporter>() {
            {
                add(reporter);
            }
        };
    }
}
