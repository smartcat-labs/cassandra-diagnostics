package io.smartcat.cassandra.diagnostics.connector;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import io.smartcat.cassandra.diagnostics.actor.ActorFactory;
import io.smartcat.cassandra.diagnostics.actor.Topics;
import io.smartcat.cassandra.diagnostics.actor.messages.Notification;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Cassandra connector class.
 */
public class Connector extends ConnectorActor {

    private ActorRef nodeProbeWrapper;

    /**
     * Constructor.
     *
     * @param configuration Configuration
     */
    public Connector(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void cassandraStartupComplete() {
        logger.info("Cassandra setup process completed.");
        initializeNodeProbe();
        mediator.tell(new DistributedPubSubMediator.Publish(Topics.CASSANDRA_SETUP_COMPLETED_TOPIC,
                new Notification.CassandraStartupComplete()), getSelf());
    }

    private void initializeNodeProbe() {
        logger.info("Initializing NodeProbe");
        try {
            nodeProbeWrapper = getContext().getSystem()
                    .actorOf(ActorFactory.infoProviderProps(NodeProbeWrapper.class, configuration.connector));
        } catch (Exception e) {
            logger.error("Failed to create node probe wrapper. Reason: " + e.getMessage());
        }
    }

}
