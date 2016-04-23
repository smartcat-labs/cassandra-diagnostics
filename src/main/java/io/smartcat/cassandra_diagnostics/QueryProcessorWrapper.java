package io.smartcat.cassandra_diagnostics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import io.smartcat.cassandra_diagnostics.config.Configuration;
import io.smartcat.cassandra_diagnostics.report.QueryReport;
import io.smartcat.cassandra_diagnostics.report.QueryReporter;

public class QueryProcessorWrapper {

	private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	private static int EXECUTOR_NO_THREADS = 2;
	private static ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_NO_THREADS, new ThreadFactoryBuilder()
            .setNameFormat("Cassandra-Diagnostics-%d").build());

	@Inject
	private Configuration config;
			
	@Inject
	private QueryReporter reporter;
	
	public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options, Logger origLogger)
		    throws RequestExecutionException, RequestValidationException {

		ResultMessage result;
		final long startTime = System.nanoTime();
		try {
			
			// original implementation
			origLogger.trace("Process {} @CL.{}", statement, options.getConsistency());
	        ClientState clientState = queryState.getClientState();
	        statement.checkAccess(clientState);
	        statement.validate(clientState);

	        result = statement.execute(queryState, options);			
			// end of the original implementation
	        
			final long execTime = System.nanoTime() - startTime;
			report(startTime, execTime, statement, queryState, options, null);
			
			return result == null ? new ResultMessage.Void() : result;
			
		} catch (Exception err) {
			final long execTime = System.nanoTime() - startTime;
			report(startTime, execTime, statement, queryState, options, err.getMessage());
			throw err;
		}

	}
	
	private void report(final long startTime, final long execTime, final CQLStatement statement, final QueryState queryState, final QueryOptions options, final String errorMessage) {
		if (execTime >= config.slow_query_threshold) {
			// execute the rest of the action later, in a separate thread 
			Future<Void> task = executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					reportExec(startTime, execTime, statement, queryState, options, errorMessage);
					return null;
				}
			});
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("An error occured while executing audit.", e);
			}
		}
	}
	
	private void reportExec(final long startTime, final long execTime, final CQLStatement statement, final QueryState queryState, final QueryOptions options, final String errorMessage) {
		QueryReport report = new QueryReport();
		report.startTime = startTime;
		report.executionTime = execTime;
		report.clientAddress = queryState.getClientState().getRemoteAddress().toString();
		report.statement = statement.getClass().getSimpleName();
		reporter.report(report);
	}

}
