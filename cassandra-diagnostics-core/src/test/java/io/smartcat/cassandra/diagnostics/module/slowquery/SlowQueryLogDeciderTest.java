package io.smartcat.cassandra.diagnostics.module.slowquery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Query.StatementType;

/**
 * Test for slow query log decider.
 *
 */
public class SlowQueryLogDeciderTest {

    @Test
    public void do_not_log_query_when_execution_below_default_threshold() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("logAllQueries", "false");
        SlowQueryLogDecider slowQueryLogDecider = buildSlowLogDecider(options);
        Query query = buildQuery(24, StatementType.SELECT, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isFalse();
    }

    @Test
    public void do_not_log_query_when_execution_below_configured_threshold() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("logAllQueries", "false");
        options.put("slowQueryThresholdInMilliseconds", "7");
        SlowQueryLogDecider slowQueryLogDecider = buildSlowLogDecider(options);
        Query query = buildQuery(6, StatementType.SELECT, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isFalse();
    }

    @Test
    public void log_query_when_log_all_queries_turned_on() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("logAllQueries", "true");
        SlowQueryLogDecider slowQueryLogDecider = buildSlowLogDecider(options);
        Query query = buildQuery(50, StatementType.SELECT, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isTrue();
    }

    @Test
    public void do_not_log_query_when_statement_type_is_not_for_reporting() throws Exception {
        SlowQueryLogDecider slowQueryLogDecider = buildDefaultSlowQueryLogDecider();
        Query query = buildQuery(50, StatementType.UNKNOWN, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isFalse();
    }

    @Test
    public void log_select_query_when() throws Exception {
        SlowQueryLogDecider slowQueryLogDecider = buildDefaultSlowQueryLogDecider();
        Query query = buildQuery(50, StatementType.SELECT, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isTrue();
    }

    @Test
    public void log_update_query_when() throws Exception {
        SlowQueryLogDecider slowQueryLogDecider = buildDefaultSlowQueryLogDecider();
        Query query = buildQuery(50, StatementType.UPDATE, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isTrue();
    }

    @Test
    public void log_query_when_statement_is_for_logged_table() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("tablesForLogging", Arrays.asList("keyspace.table", "keyspace.table2"));
        SlowQueryLogDecider slowQueryLogDecider = buildSlowLogDecider(options);
        Query query = buildQuery(50, StatementType.SELECT, "keyspace", "table");

        assertThat(slowQueryLogDecider.isForReporting(query)).isTrue();
    }

    @Test
    public void do_not_log_query_when_statement_is_not_for_logged_table() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("tablesForLogging", Arrays.asList("keyspace.table", "keyspace.table2"));
        SlowQueryLogDecider slowQueryLogDecider = buildSlowLogDecider(options);
        Query query = buildQuery(50, StatementType.SELECT, "keyspace", "table3");

        assertThat(slowQueryLogDecider.isForReporting(query)).isFalse();
    }

    private Query buildQuery(long executionTime, StatementType type, String keyspace, String table) {
        return Query.create(1, executionTime, "clientAddress", type, keyspace, table, "statement", "error");
    }

    private SlowQueryLogDecider buildSlowLogDecider(final Map<String, Object> options) throws Exception {
        return SlowQueryLogDecider.create(SlowQueryConfiguration.create(options));
    }

    private SlowQueryLogDecider buildDefaultSlowQueryLogDecider() throws Exception {
        Map<String, Object> options = new HashMap<>();
        return SlowQueryLogDecider.create(SlowQueryConfiguration.create(options));
    }
}
