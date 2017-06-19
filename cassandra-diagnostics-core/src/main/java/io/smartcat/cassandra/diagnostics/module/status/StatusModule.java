package io.smartcat.cassandra.diagnostics.module.status;

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
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.module.ModuleActor;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * Status module collecting node status information.
 */
public class StatusModule extends ModuleActor {

    private static final String STATUS_THREAD_NAME = "status-module";

    private static final String DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME = "compaction_info";

    private static final String DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME = "compaction_settings_info";

    private static final String DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME = "repair_sessions";

    private static final String DEFAULT_NODE_INFO_MEASUREMENT_NAME = "node_info";

    private final Timeout timeout = new Timeout(500, TimeUnit.MILLISECONDS);

    private StatusConfiguration config;

    private ActorRef infoProvider;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public StatusModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = StatusConfiguration.create(moduleConfiguration.options);
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
        if (timer != null) {
            timer.cancel();
        }
    }

    private void infoProviderResponse(final QueryResponse.InfoProviderRef response) {
        this.infoProvider = response.infoProvider;

        timer = new Timer(STATUS_THREAD_NAME);
        timer.scheduleAtFixedRate(new StatusTask(), 0, config.reportingRateInMillis());
    }

    /**
     * Status collector task that's executed at configured period.
     */
    private class StatusTask extends TimerTask {
        @Override
        public void run() {
            if (config.compactionsEnabled()) {
                queryCompactions();
            }
            if (config.tpStatsEnabled()) {
                queryTPStats();
            }
            if (config.repairsEnabled()) {
                queryRepairSessions();
            }
            if (config.nodeInfoEnabled()) {
                queryNodeInfo();
            }
        }

        private void queryCompactions() {
            final Future<Object> future = Patterns.ask(infoProvider, new Query.Compactions(), timeout);
            try {
                final QueryResponse.CompactionsResp result = (QueryResponse.CompactionsResp) Await
                        .result(future, timeout.duration());
                report(createMeasurement(result.compactionSettingsInfo));
                for (CompactionInfo compactionInfo : result.compactionInfo) {
                    report(createMeasurement(compactionInfo));
                }
            } catch (Exception e) {
                logger.error("Failed to query/report compaction info from info provider.", e);
            }
        }

        private void queryTPStats() {
            final Future<Object> future = Patterns.ask(infoProvider, new Query.TPStats(), timeout);
            try {
                final QueryResponse.TPStatsResp result = (QueryResponse.TPStatsResp) Await
                        .result(future, timeout.duration());
                for (TPStatsInfo tpStatsInfo : result.tpStatsInfo) {
                    report(createMeasurement(tpStatsInfo));
                }
            } catch (Exception e) {
                logger.error("Failed to query/report thread pool stats from info provider.", e);
            }
        }

        private void queryRepairSessions() {
            final Future<Object> future = Patterns.ask(infoProvider, new Query.RepairSessions(), timeout);
            try {
                final QueryResponse.RepairSessionsResp result = (QueryResponse.RepairSessionsResp) Await
                        .result(future, timeout.duration());
                report(createSimpleMeasurement(DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME,
                        (double) result.repairSessions));
            } catch (Exception e) {
                logger.error("Failed to query/report repair sessions from info provider.", e);
            }
        }

        private void queryNodeInfo() {
            final Future<Object> future = Patterns.ask(infoProvider, new Query.NodeInfo(), timeout);
            try {
                final QueryResponse.NodeInfoResp result = (QueryResponse.NodeInfoResp) Await
                        .result(future, timeout.duration());
                report(createMeasurement(result.nodeInfo));
            } catch (Exception e) {
                logger.error("Failed to query/report compaction info from info provider.", e);
            }
        }
    }

    private Measurement createMeasurement(CompactionSettingsInfo compactionSettingsInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionThroughput", Integer.toString(compactionSettingsInfo.compactionThroughput));
        fields.put("coreCompactorThreads", Integer.toString(compactionSettingsInfo.coreCompactorThreads));
        fields.put("maximumCompactorThreads", Integer.toString(compactionSettingsInfo.maximumCompactorThreads));
        fields.put("coreValidatorThreads", Integer.toString(compactionSettingsInfo.coreValidatorThreads));
        fields.put("maximumValidatorThreads", Integer.toString(compactionSettingsInfo.maximumValidatorThreads));

        return Measurement
                .createComplex(DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME, System.currentTimeMillis(), tags,
                        fields);
    }

    private Measurement createMeasurement(CompactionInfo compactionInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);
        tags.put("keyspace", compactionInfo.keyspace);
        tags.put("columnfamily", compactionInfo.columnFamily);
        tags.put("taskType", compactionInfo.taskType);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionId", compactionInfo.compactionId);
        fields.put("unit", compactionInfo.unit);
        fields.put("total", Long.toString(compactionInfo.total));
        fields.put("completed", Long.toString(compactionInfo.completed));
        fields.put("completedPercentage", Double.toString(compactionInfo.completedPercentage));

        return Measurement
                .createComplex(DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME, System.currentTimeMillis(), tags, fields);
    }

    private Measurement createMeasurement(TPStatsInfo tpStatsInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("activeTasks", Long.toString(tpStatsInfo.activeTasks));
        fields.put("pendingTasks", Long.toString(tpStatsInfo.pendingTasks));
        fields.put("completedTasks", Long.toString(tpStatsInfo.completedTasks));
        fields.put("currentlyBlockedTasks", Long.toString(tpStatsInfo.currentlyBlockedTasks));
        fields.put("totalBlockedTasks", Long.toString(tpStatsInfo.totalBlockedTasks));

        return Measurement.createComplex(tpStatsInfo.threadPool, System.currentTimeMillis(), tags, fields);
    }

    private Measurement createSimpleMeasurement(String name, double value) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        return Measurement.createSimple(name, value, System.currentTimeMillis(), tags);
    }

    private Measurement createMeasurement(NodeInfo nodeInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("gossipActive", Integer.toString(nodeInfo.isGossipActive()));
        fields.put("thriftActive", Integer.toString(nodeInfo.isThriftActive()));
        fields.put("nativeTransportActive", Integer.toString(nodeInfo.isNativeTransportActive()));
        fields.put("uptimeInSeconds", Long.toString(nodeInfo.uptimeInSeconds));

        return Measurement.createComplex(DEFAULT_NODE_INFO_MEASUREMENT_NAME, System.currentTimeMillis(), tags, fields);
    }

}
