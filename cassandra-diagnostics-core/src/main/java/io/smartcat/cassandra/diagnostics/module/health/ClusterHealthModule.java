package io.smartcat.cassandra.diagnostics.module.health;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Cluster health module collecting information about the liveness of the nodes in the cluster.
 */
public class ClusterHealthModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(ClusterHealthModule.class);

    private static final String STATUS_THREAD_NAME = "unreachable-nodes-module";

    private static final String DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME = "number_of_unreachable_nodes";

    private final int period;

    private final TimeUnit timeunit;

    private final boolean numberOfUnreachableNodesEnabled;

    private final Timer timer;

    private final InfoProvider infoProvider;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters Reporter list
     * @throws ConfigurationException configuration parsing exception
     */
    public ClusterHealthModule(ModuleConfiguration configuration, List<Reporter> reporters)
            throws ConfigurationException {
        super(configuration, reporters);

        ClusterHealthConfiguration config = ClusterHealthConfiguration.create(configuration.options);
        period = config.period();
        timeunit = config.timeunit();
        numberOfUnreachableNodesEnabled = config.numberOfUnreachableNodesEnabled();

        infoProvider = DiagnosticsAgent.getInfoProvider();
        if (infoProvider == null) {
            logger.warn("Failed to initialize StatusModule. Info provider is null");
            timer = null;
        } else {
            timer = new Timer(STATUS_THREAD_NAME);
            timer.scheduleAtFixedRate(new ClusterHealthTask(), 0, config.reportingRateInMillis());
        }
    }

    @Override
    public void stop() {
        logger.trace("Stopping status module.");
        timer.cancel();
    }

    /**
     * Cluster health collector task that's executed at configured period.
     */
    private class ClusterHealthTask extends TimerTask {
        @Override
        public void run() {
            if (numberOfUnreachableNodesEnabled) {
                report(createMeasurement(infoProvider.getUnreachableNodes().size()));
            }
        }
    }

    private Measurement createMeasurement(long numberOfUnreachableNode) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", hostname);

        final Map<String, String> fields = new HashMap<>();

        return Measurement.create(DEFAULT_NUMBER_OF_UNREACHABLE_NODES_MEASUREMENT_NAME,
                (double) numberOfUnreachableNode, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

}
