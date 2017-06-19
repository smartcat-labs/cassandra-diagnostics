package io.smartcat.cassandra.diagnostics.connector;

import java.lang.instrument.Instrumentation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Cassandra Diagnostics Connector proxy class.
 */
public interface ConnectorProxy {

    /**
     * Performs Cassandra classes instrumentation in order to inject Cassandra Diagnostics interceptors.
     *
     * @param inst          instrumentation reference
     * @param system        actor system
     * @param configuration configuration
     * @return connector actor reference
     */
    ActorRef init(Instrumentation inst, ActorSystem system, Configuration configuration);

}
