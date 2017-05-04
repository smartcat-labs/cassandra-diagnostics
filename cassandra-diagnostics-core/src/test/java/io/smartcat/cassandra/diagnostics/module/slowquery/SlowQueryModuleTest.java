package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Query.ConsistencyLevel;
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
        conf.options.put("slowQueryReportEnabled", true);
        conf.options.put("slowQueryCountReportEnabled", false);
        TestReporter reporter = new TestReporter(null, GlobalConfiguration.getDefault());
        SlowQueryModule module = new SlowQueryModule(conf, testReporters(reporter), GlobalConfiguration.getDefault());

        Query query = Query.create(1474741407205L, 234L, "/127.0.0.1:40042", Query.StatementType.SELECT, "keyspace",
                "table", "select count(*) from keyspace.table", ConsistencyLevel.ONE);

        module.process(query);
        module.stop();

        Measurement measurement = reporter.reported.get(0);

        assertThat(measurement.fields().keySet()).isEqualTo(Sets.newSet("statement", "client", "consistencyLevel"));
        assertThat(measurement.fields().get("statement")).isEqualTo("select count(*) from keyspace.table");
        assertThat(measurement.fields().get("client")).isEqualTo("/127.0.0.1:40042");
        assertThat(measurement.fields().get("consistencyLevel")).isEqualTo("ONE");
        assertThat(measurement.hasValue()).isTrue();
        assertThat(measurement.getValue()).isEqualTo(234);

        assertThat(measurement.tags().keySet()).isEqualTo(Sets.newSet("host", "statementType", "systemName"));
        assertThat(measurement.tags().get("statementType")).isEqualTo("SELECT");
    }

    @Test
    public void should_report_number_of_slow_queries() throws ConfigurationException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(110);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(testConfiguration(1), reporters,
                GlobalConfiguration.getDefault());

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
                slowQueryCounts += measurement.getValue();
            }
        }

        assertThat(numberOfSlowQueries).isEqualTo(reportedMeasurementCount);
        assertThat(numberOfSlowQueries).isEqualTo(slowQueryCounts);
    }

    @Test
    public void should_not_report_any_slow_queries() throws InterruptedException, ConfigurationException {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", false);
        configuration.options.put("slowQueryCountReportEnabled", false);

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(configuration, reporters, GlobalConfiguration.getDefault());

        final long numberOfSlowQueries = 100;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        boolean waited = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();

        assertThat(waited).isFalse();
        assertThat(latchTestReporter.getReported()).isEmpty();
    }

    @Test
    public void should_not_log_all_types_if_slow_query_logging_is_disabled()
            throws InterruptedException, ConfigurationException {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", false);
        configuration.options.put("slowQueryCountReportEnabled", false);
        configuration.options.put("queryTypesToLog", Arrays.asList("ALL"));

        final CountDownLatch latch = new CountDownLatch(1);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(configuration, reporters, GlobalConfiguration.getDefault());

        final long numberOfSlowQueries = 100;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        boolean waited = latch.await(100, TimeUnit.MILLISECONDS);
        module.stop();

        assertThat(waited).isFalse();
        assertThat(latchTestReporter.getReported()).isEmpty();
    }

    @Test
    public void should_log_only_mutation_statements_when_only_mutation_type_logging_is_enabled()
            throws InterruptedException, ConfigurationException {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", true);
        configuration.options.put("slowQueryCountReportEnabled", false);
        configuration.options.put("queryTypesToLog", Arrays.asList("UPDATE"));

        final CountDownLatch latch = new CountDownLatch(500);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(configuration, reporters, GlobalConfiguration.getDefault());

        final long numberOfSlowQueries = 1000;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        boolean waited = latch.await(1000, TimeUnit.MILLISECONDS);
        module.stop();

        assertThat(waited).isTrue();
        assertThat(latchTestReporter.getReported().size()).isEqualTo(500);
        for (Measurement measurement : latchTestReporter.getReported()) {
            assertThat(measurement.tags().get("statementType")).isEqualTo("UPDATE");
        }
    }

    @Test
    public void should_log_all_statements_when_all_types_logging_is_enabled()
            throws InterruptedException, ConfigurationException {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", true);
        configuration.options.put("slowQueryCountReportEnabled", false);
        configuration.options.put("queryTypesToLog", Arrays.asList("ALL"));

        final CountDownLatch latch = new CountDownLatch(100);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(configuration, reporters, GlobalConfiguration.getDefault());

        final long numberOfSlowQueries = 100;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        boolean waited = latch.await(1000, TimeUnit.MILLISECONDS);
        module.stop();

        assertThat(waited).isTrue();
        assertThat(latchTestReporter.getReported().size()).isEqualTo(100);
        int numberOfMutationsReported = 0;
        int numberOfSelectsReported = 0;
        for (Measurement measurement : latchTestReporter.getReported()) {
            if (measurement.tags().get("statementType").equals("UPDATE")) {
                numberOfMutationsReported++;
            } else if (measurement.tags().get("statementType").equals("SELECT")) {
                numberOfSelectsReported++;
            }
        }
        assertThat(numberOfMutationsReported).isEqualTo(50);
        assertThat(numberOfSelectsReported).isEqualTo(50);
    }

    @Test
    public void should_log_mutation_and_select_statements_when_mutation_and_select_types_logging_is_enabled()
            throws InterruptedException, ConfigurationException {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", true);
        configuration.options.put("slowQueryCountReportEnabled", false);
        configuration.options.put("queryTypesToLog", Arrays.asList("UPDATE", "SELECT"));

        final CountDownLatch latch = new CountDownLatch(100);
        final LatchTestReporter latchTestReporter = new LatchTestReporter(null, GlobalConfiguration.getDefault(),
                latch);
        final List<Reporter> reporters = new ArrayList<Reporter>() {
            {
                add(latchTestReporter);
            }
        };

        final Query selectQuery = mock(Query.class);
        when(selectQuery.statementType()).thenReturn(Query.StatementType.SELECT);
        when(selectQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);
        final Query updateQuery = mock(Query.class);
        when(updateQuery.statementType()).thenReturn(Query.StatementType.UPDATE);
        when(updateQuery.consistencyLevel()).thenReturn(Query.ConsistencyLevel.ALL);

        final SlowQueryModule module = new SlowQueryModule(configuration, reporters, GlobalConfiguration.getDefault());

        final long numberOfSlowQueries = 100;
        for (int i = 0; i < numberOfSlowQueries / 2; i++) {
            module.process(selectQuery);
            module.process(updateQuery);
        }

        boolean waited = latch.await(1000, TimeUnit.MILLISECONDS);
        module.stop();

        assertThat(waited).isTrue();
        assertThat(latchTestReporter.getReported().size()).isEqualTo(100);
        int numberOfMutationsReported = 0;
        int numberOfSelectsReported = 0;
        for (Measurement measurement : latchTestReporter.getReported()) {
            if (measurement.tags().get("statementType").equals("UPDATE")) {
                numberOfMutationsReported++;
            } else if (measurement.tags().get("statementType").equals("SELECT")) {
                numberOfSelectsReported++;
            }
        }
        assertThat(numberOfMutationsReported).isEqualTo(50);
        assertThat(numberOfSelectsReported).isEqualTo(50);
    }

    private ModuleConfiguration testConfiguration(final int period) {
        final ModuleConfiguration configuration = new ModuleConfiguration();
        configuration.measurement = SLOW_QUERY_MESUREMENT_NAME;
        configuration.module = "io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule";
        configuration.options.put("slowQueryThresholdInMilliseconds", 0);
        configuration.options.put("slowQueryReportEnabled", true);
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
