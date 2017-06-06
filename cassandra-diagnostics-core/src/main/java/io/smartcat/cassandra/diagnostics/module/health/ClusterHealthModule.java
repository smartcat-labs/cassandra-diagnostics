package io.smartcat.cassandra.diagnostics.module.health;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.actor.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;

/**
 * Cluster health module collecting information about the liveness of the nodes in the cluster.
 */
public class ClusterHealthModule extends ModuleActor {

    private static final String STATUS_THREAD_NAME = "unreachable-nodes-module";

    private static final String DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME = "number_of_unreachable_nodes";

    private ClusterHealthConfiguration config;

    private Timer timer;

    private InfoProvider infoProvider;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public ClusterHealthModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = ClusterHealthConfiguration.create(moduleConfiguration.options);
    }

    @Override
    protected void start() {
        infoProvider = DiagnosticsAgent.getInfoProvider();
        if (infoProvider == null) {
            logger.warning("Failed to initialize StatusModule. Info provider is null");
            timer = null;
        } else {
            timer = new Timer(STATUS_THREAD_NAME);
            timer.scheduleAtFixedRate(new ClusterHealthTask(), 0, config.reportingRateInMillis());
        }
    }

    @Override
    protected void stop() {
        logger.debug("Stopping status module.");
        timer.cancel();
    }

    /**
     * Cluster health collector task that's executed at configured period.
     */
    private class ClusterHealthTask extends TimerTask {
        @Override
        public void run() {
            if (config.numberOfUnreachableNodesEnabled()) {
                report(createMeasurement(infoProvider.getUnreachableNodes().size()));
            }
        }
    }

    private Measurement createMeasurement(long numberOfUnreachableNode) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>();

        return Measurement.createSimple(DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME,
                (double) numberOfUnreachableNode, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

}
