package io.smartcat.cassandra_diagnostics;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;

import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class QueryProcessorInterceptor {
	
	@RuntimeType
	public static ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options, 
			@RuntimeType @FieldValue("logger") Logger logger)
		    throws RequestExecutionException, RequestValidationException {

		return Diagnostics.queryProcessorWrapper.processStatement(statement, queryState, options, logger);

	}

}
