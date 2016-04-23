package io.smartcat.cassandra_diagnostics;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * {@code DiagnosticAgent} acts as a Java agent used to instrument 
 * original Cassandra classes in order to inject Cassandra Diagnostics. 
 */
public class DiagnosticsAgent {
	
	public static void premain(String options, Instrumentation inst) {
		init(options, inst);
	}
	
	public static void agentmain(String options, Instrumentation inst) {
		init(options, inst);
    }
	
	private static void init(String options, Instrumentation inst) {
		setInterceptors(options, inst);
		Diagnostics.init();
	}
	
	private static void setInterceptors(String arg, Instrumentation inst) {
		final ElementMatcher.Junction<NamedElement> type = ElementMatchers.named("org.apache.cassandra.cql3.QueryProcessor");
	             
		// Transformer for QueryProcessor
		final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
		new AgentBuilder.Default()
        .with(byteBuddy)
        .with(AgentBuilder.InitializationStrategy.Minimal.INSTANCE)
        .with(AgentBuilder.RedefinitionStrategy.DISABLED)
        .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
		.type(type)
		.transform(new AgentBuilder.Transformer() {
			@Override
			public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
					TypeDescription typeDescription, ClassLoader classloader) {
				return builder
						.method(named("processStatement"))
						.intercept(MethodDelegation.to(QueryProcessorInterceptor.class));				
			}
		})
		.installOn(inst);		
	}
}
