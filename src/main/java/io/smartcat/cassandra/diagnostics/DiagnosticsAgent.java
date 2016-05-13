package io.smartcat.cassandra.diagnostics;

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
 * {@code DiagnosticAgent} acts as a Java agent used to instrument original
 * Cassandra classes in order to extend them with Cassandra Diagnostics additions.
 */
public class DiagnosticsAgent {

  /**
   * Prevents class instantiation.
   */
  private DiagnosticsAgent() {
  }

  /**
   * Entry point for agent when it is started upon VM start.
   *
   * @param args agent arguments
   * @param inst instrumentation handle
   */
  public static void premain(String args, Instrumentation inst) {
    System.out.println("Cassandra Diagnostics Agent: starting");
    setIntercepters(args, inst);
    Diagnostics.init();
  }

  /**
   * Installs intercepter for the target classes.
   *
   * @param args agent arguments
   * @param inst instrumentation handle
   */
  private static void setIntercepters(String args, Instrumentation inst) {
    System.out.println("Cassandra Diagnostics Agenet: injecting org.apache.cassandra.cql3.QueryProcessor interceptor");

    final ElementMatcher.Junction<NamedElement> type = ElementMatchers
        .named("org.apache.cassandra.cql3.QueryProcessor");

    // Transformer for QueryProcessor
    final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
    new AgentBuilder.Default().with(byteBuddy).with(AgentBuilder.InitializationStrategy.Minimal.INSTANCE)
        .with(AgentBuilder.RedefinitionStrategy.DISABLED).with(AgentBuilder.TypeStrategy.Default.REDEFINE).type(type)
        .transform(new AgentBuilder.Transformer() {
          @Override
          public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
              ClassLoader classloader) {
            return builder.method(named("processStatement"))
                .intercept(MethodDelegation.to(QueryProcessorInterceptor.class));
          }
        }).installOn(inst);
  }
}
