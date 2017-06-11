package io.smartcat.cassandra.diagnostics.module.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.smartcat.cassandra.diagnostics.actor.Topics;
import io.smartcat.cassandra.diagnostics.actor.messages.Query;
import io.smartcat.cassandra.diagnostics.actor.messages.QueryResponse;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleActor;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * Cluster health module collecting information about the liveness of the nodes in the cluster.
 */
public class ClusterHealthModule extends ModuleActor {

    private static final String STATUS_THREAD_NAME = "unreachable-nodes-module";

    private static final String DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME = "number_of_unreachable_nodes";

    private final Timeout timeout = new Timeout(500, TimeUnit.MILLISECONDS);

    private ClusterHealthConfiguration config;

    private ActorRef infoProvider;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public ClusterHealthModule(final String moduleName, final Configuration configuration)
            throws ConfigurationException {
        super(moduleName, configuration);

        config = ClusterHealthConfiguration.create(moduleConfiguration.options);
    }

    @Override
    public Receive createReceive() {
        return defaultReceive().match(QueryResponse.InfoProviderRef.class, this::infoProviderResponse).build();
    }

    @Override
    protected void start() {
        mediator.tell(new DistributedPubSubMediator.Publish(Topics.INFO_PROVIDER_TOPIC, new Query.InfoProviderRef()),
                getSelf());
    }

    @Override
    protected void stop() {
        logger.debug("Stopping status module.");
        timer.cancel();
    }

    private void infoProviderResponse(final QueryResponse.InfoProviderRef response) {
        this.infoProvider = response.infoProvider;

        timer = new Timer(STATUS_THREAD_NAME);
        timer.scheduleAtFixedRate(new ClusterHealthTask(), 0, config.reportingRateInMillis());
    }

    /**
     * Cluster health collector task that's executed at configured period.
     */
    private class ClusterHealthTask extends TimerTask {
        @Override
        public void run() {
            if (config.numberOfUnreachableNodesEnabled()) {
                queryUnreachableNodes();
            }
        }

        private void queryUnreachableNodes() {
            final Future<Object> future = Patterns.ask(infoProvider, new Query.UnreachableNodes(), timeout);
            try {
                final QueryResponse.UnreachableNodesResp result = (QueryResponse.UnreachableNodesResp) Await
                        .result(future, timeout.duration());
                report(createMeasurement(result.unreachableNodes.size()));
            } catch (Exception e) {
                logger.error("Failed to query/report unreachable nodes from info provider.");
            }
        }
    }

    private Measurement createMeasurement(long numberOfUnreachableNode) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        return Measurement
                .createSimple(DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME, (double) numberOfUnreachableNode,
                        System.currentTimeMillis(), tags);
    }

}
