package io.smartcat.cassandra.diagnostics.connector;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.cassandra.cql3.BatchQueryOptions;
import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryHandler;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.statements.BatchStatement;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.transport.messages.ResultMessage.Prepared;
import org.apache.cassandra.utils.MD5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Diagnostics;

/**
 * Custom diagnostics query handler which hooks into query path and reports queries.
 */
public class DiagnosticsQueryHandler implements QueryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsQueryHandler.class);

    private final QueryProcessor queryProcessor = QueryProcessor.instance;

    private static Connector30QueryReporter queryReporter;

    /**
     * Create diagnostics query handler and init diagnostics.
     */
    public DiagnosticsQueryHandler() {
        initDiagnostics();
    }

    /**
     * Create and activate diagnostics as part of this query handler.
     */
    private void initDiagnostics() {
        LOGGER.info("Cassandra Diagnostics starting.");
        final Diagnostics diagnostics = new Diagnostics();
        diagnostics.activate();
        LOGGER.info("Cassandra Diagnostics initialized.");
        queryReporter = new Connector30QueryReporter(diagnostics, diagnostics.getConfiguration().connector);
    }

    @Override
    public ResultMessage process(String query, QueryState state, QueryOptions options,
            Map<String, ByteBuffer> customPayload) throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted process");

        // Code from {@link org.apache.cassandra.cql3.QueryProcessor}
        ParsedStatement.Prepared p = QueryProcessor.getStatement(query, state.getClientState());
        options.prepare(p.boundNames);
        CQLStatement prepared = p.statement;
        if (prepared.getBoundTerms() != options.getValues().size()) {
            throw new InvalidRequestException("Invalid amount of bind variables");
        }

        if (!state.getClientState().isInternal) {
            QueryProcessor.metrics.regularStatementsExecuted.inc();
        }

        final long startTime = System.currentTimeMillis();
        ResultMessage result = queryProcessor.processStatement(prepared, state, options);
        final long execTime = System.currentTimeMillis() - startTime;

        queryReporter.report(startTime, execTime, prepared, query, state);

        return result;
    }

    @Override
    public Prepared prepare(String query, QueryState state, Map<String, ByteBuffer> customPayload)
            throws RequestValidationException {
        LOGGER.info("Intercepted prepare");
        return queryProcessor.prepare(query, state, customPayload);
    }

    @Override
    public org.apache.cassandra.cql3.statements.ParsedStatement.Prepared getPrepared(MD5Digest id) {
        LOGGER.trace("Intercepted getPrepared");
        return queryProcessor.getPrepared(id);
    }

    @Override
    public org.apache.cassandra.cql3.statements.ParsedStatement.Prepared getPreparedForThrift(Integer id) {
        LOGGER.trace("Intercepted getPreparedForThrift");
        return queryProcessor.getPreparedForThrift(id);
    }

    @Override
    public ResultMessage processPrepared(CQLStatement statement, QueryState state, QueryOptions options,
            Map<String, ByteBuffer> customPayload) throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted processPrepared");
        return queryProcessor.processPrepared(statement, state, options, customPayload);
    }

    @Override
    public ResultMessage processBatch(BatchStatement statement, QueryState state, BatchQueryOptions options,
            Map<String, ByteBuffer> customPayload) throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted processBatch");
        return queryProcessor.processBatch(statement, state, options, customPayload);
    }

}
