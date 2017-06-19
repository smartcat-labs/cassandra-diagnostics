package io.smartcat.cassandra.diagnostics.info;

import java.util.List;

import akka.cluster.pubsub.DistributedPubSubMediator;
import io.smartcat.cassandra.diagnostics.actor.BaseActor;
import io.smartcat.cassandra.diagnostics.actor.Topics;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.actor.messages.Query;
import io.smartcat.cassandra.diagnostics.actor.messages.QueryResponse;
import io.smartcat.cassandra.diagnostics.connector.ConnectorConfiguration;

/**
 * Info provider actor.
 */
public abstract class InfoProviderActor extends BaseActor {

    /**
     * Connector configuration.
     */
    protected final ConnectorConfiguration configuration;

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public InfoProviderActor(final ConnectorConfiguration configuration) {
        this.configuration = configuration;

        mediator.tell(new DistributedPubSubMediator.Subscribe(Topics.INFO_PROVIDER_TOPIC, getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class, msg -> logger.info("Subscribed"))
                .match(Command.Start.class, o -> start()).match(Command.Stop.class, o -> stop())
                .match(Query.InfoProviderRef.class, o -> getInfoProviderReference()).match(Query.Compactions.class,
                        o -> getSender()
                                .tell(new QueryResponse.CompactionsResp(getCompactionSettingsInfo(), getCompactions()),
                                        getSelf())).match(Query.TPStats.class,
                        o -> getSender().tell(new QueryResponse.TPStatsResp(getTPStats()), getSelf()))
                .match(Query.RepairSessions.class,
                        o -> getSender().tell(new QueryResponse.RepairSessionsResp(getRepairSessions()), getSelf()))
                .match(Query.UnreachableNodes.class,
                        o -> getSender().tell(new QueryResponse.UnreachableNodesResp(getUnreachableNodes()), getSelf()))
                .match(Query.NodeInfo.class,
                        o -> getSender().tell(new QueryResponse.NodeInfoResp(getNodeInfo()), getSelf())).build();
    }

    private void start() {

    }

    private void stop() {

    }

    private void getInfoProviderReference() {
        getSender().tell(new QueryResponse.InfoProviderRef(getSelf()), getSelf());
    }

    /**
     * Gets the list of all active compactions.
     *
     * @return compaction info list
     */
    protected abstract List<CompactionInfo> getCompactions();

    /**
     * Get the status of all thread pools.
     *
     * @return thread pools info list
     */
    protected abstract List<TPStatsInfo> getTPStats();

    /**
     * Gets number of repair sessions pending (repair on node has multiple repair sessions and by monitoring number of
     * pending repair sessions and active repair sessions we can monitor progress of repair).
     *
     * @return repair session info
     */
    protected abstract long getRepairSessions();

    /**
     * Get compaction settings info for.
     *
     * @return compaction settings info.
     */
    protected abstract CompactionSettingsInfo getCompactionSettingsInfo();

    /**
     * Get unreachable nodes from the node's point of view (using the node's failure detection mechanism).
     *
     * @return unreachable nodes list
     */
    protected abstract List<String> getUnreachableNodes();

    /**
     * Get the information if the native transport is active on the node.
     * Get the information about node such as which protocols are active and uptime.
     *
     * @return NodeInfo for the node
     */
    protected abstract NodeInfo getNodeInfo();

}
