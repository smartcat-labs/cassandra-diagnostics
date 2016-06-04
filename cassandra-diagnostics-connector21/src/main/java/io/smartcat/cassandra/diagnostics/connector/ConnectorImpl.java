package io.smartcat.cassandra.diagnostics.connector;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Connector implementation.
 */
public class ConnectorImpl implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorImpl.class);

    /**
     * {@link org.apache.cassandra.cql3.QueryProcessor} diagnostics wrapper.
     */
    private static QueryProcessorWrapper queryProcessorWrapper;

    /**
     * Initialize connector instance using the provided instrumentation.
     *
     * @param inst an Instrumentation reference
     * @param queryReporter QueryReporter implementation reference
     */
    public void init(Instrumentation inst, QueryReporter queryReporter) {
        queryProcessorWrapper = new QueryProcessorWrapper(queryReporter);
        setIntercepters(inst);
    }

    /**
     * Installs intercepter for the target classes.
     *
     * @param inst instrumentation handle
     */
    private static void setIntercepters(Instrumentation inst) {

        logger.info("Cassandra Diagnostics Agenet: injecting org.apache.cassandra.cql3.QueryProcessor interceptor");

        final ElementMatcher.Junction<NamedElement> type = ElementMatchers
                .named("org.apache.cassandra.cql3.QueryProcessor");

        // Transformer for QueryProcessor
        final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
        new AgentBuilder.Default().with(byteBuddy).with(AgentBuilder.InitializationStrategy.Minimal.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.DISABLED).with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .type(type)
                .transform(new Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader) {
                        return builder.method(named("processStatement"))
                                .intercept(MethodDelegation.to(ConnectorImpl.class));
                    }
                })
                .installOn(inst);

    }

    /**
     * Intercepter for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}. Every
     * invocation is being delegated to {@link QueryProcessorWrapper}.
     *
     * @param statement QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param options QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param logger internal class logger of {@link org.apache.cassandra.cql3.QueryProcessor}
     * @return QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestExecutionException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestValidationException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     */
    @RuntimeType
    public static ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
            @RuntimeType @FieldValue("logger") Logger logger)
                    throws RequestExecutionException, RequestValidationException {
        return ConnectorImpl.queryProcessorWrapper.processStatement(statement, queryState, options, logger);
    }
}
