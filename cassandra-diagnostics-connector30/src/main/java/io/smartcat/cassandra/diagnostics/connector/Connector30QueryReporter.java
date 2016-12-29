package io.smartcat.cassandra.diagnostics.connector;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.statements.ModificationStatement;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.service.QueryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * Connector which is reporting query in low priority thread.
 */
public class Connector30QueryReporter extends AbstractEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(Connector30QueryReporter.class);

    /**
     * Constructor.
     *
     * @param queryReporter QueryReporter used to report queries
     * @param configuration Connector configuration
     */
    public Connector30QueryReporter(QueryReporter queryReporter, ConnectorConfiguration configuration) {
        super(queryReporter, configuration);
    }

    /**
     * Submits a query reports asynchronously.
     *
     * @param startTime       execution start time, in milliseconds
     * @param execTime        execution time, in milliseconds
     * @param statement       CQL statement
     * @param statementString text representation of statement
     * @param queryState      state of query
     */
    public void report(final long startTime, final long execTime, final CQLStatement statement,
            final String statementString, final QueryState queryState) {
        if (queryState.getClientState().isInternal) {
            return;
        }

        report(new Runnable() {
            @Override
            public void run() {
                try {
                    Query query = extractQuery(startTime, execTime, statement, statementString, queryState);
                    logger.debug("Reporting query: {}.", query);
                    queryReporter.report(query);
                } catch (Exception e) {
                    logger.warn("An error occured while reporting query", e);
                }
            }
        });
    }

    private Query extractQuery(final long startTime, final long execTime, final CQLStatement statement,
            final String statementString, final QueryState queryState) {
        if (statement instanceof SelectStatement) {
            return Query.create(
                    startTime,
                    execTime,
                    queryState.getClientState().getRemoteAddress().toString(),
                    Query.StatementType.SELECT,
                    ((SelectStatement) statement).keyspace(),
                    ((SelectStatement) statement).columnFamily(),
                    statementString);
        } else if (statement instanceof ModificationStatement) {
            return Query.create(
                    startTime,
                    execTime,
                    queryState.getClientState().getRemoteAddress().toString(),
                    Query.StatementType.UPDATE,
                    ((ModificationStatement) statement).keyspace(),
                    ((ModificationStatement) statement).columnFamily(),
                    statementString);
        } else {
            return Query.create(
                    startTime,
                    execTime,
                    queryState.getClientState().getRemoteAddress().toString(),
                    Query.StatementType.UNKNOWN,
                    "",
                    "",
                    statementString);
        }
    }
}
