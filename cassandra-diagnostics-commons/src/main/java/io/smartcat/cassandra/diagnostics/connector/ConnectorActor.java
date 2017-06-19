package io.smartcat.cassandra.diagnostics.connector;

import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.pf.ReceiveBuilder;
import io.smartcat.cassandra.diagnostics.actor.BaseActor;
import io.smartcat.cassandra.diagnostics.actor.Topics;
import io.smartcat.cassandra.diagnostics.actor.messages.Notification;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.query.Query;

/**
 * Connector actor definition.
 */
public abstract class ConnectorActor extends BaseActor {

    /**
     * Connector specific configuration.
     */
    protected final Configuration configuration;

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public ConnectorActor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Receive createReceive() {
        return defaultReceive().build();
    }

    /**
     * Default receive builder with default matching.
     *
     * @return default receive builder
     */
    protected ReceiveBuilder defaultReceive() {
        return receiveBuilder()
                .match(DistributedPubSubMediator.SubscribeAck.class, msg -> logger.info("Subscribed"))
                .match(Notification.CassandraStartupComplete.class, o -> cassandraStartupComplete())
                .match(Query.class, this::process)
                .match(Terminated.class, o -> terminate());
    }

    /**
     * Method that gets called when cassandra setup is completed.
     */
    protected abstract void cassandraStartupComplete();

    private void process(final Query query) {
        mediator.tell(new DistributedPubSubMediator.Publish(Topics.PROCESS_QUERY_TOPIC, query), getSelf());
    }

    private void terminate() {

    }

}
