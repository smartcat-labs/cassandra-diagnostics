package io.smartcat.cassandra.diagnostics.reporter;

import java.util.Optional;

import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.pf.ReceiveBuilder;
import io.smartcat.cassandra.diagnostics.actor.BaseActor;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * Reporter actor definition.
 */
public abstract class ReporterActor extends BaseActor {

    private final String reporterName;

    private final Reporter reporterInstance;

    /**
     * System configuration.
     */
    protected final Configuration configuration;

    /**
     * Constructor.
     *
     * @param reporterName  reporter class name
     * @param configuration reporter configuration
     */
    public ReporterActor(final String reporterName, final Configuration configuration) {
        this.reporterName = reporterName;
        this.configuration = configuration;

        // Subscribe to reporter name topic
        this.mediator.tell(new DistributedPubSubMediator.Subscribe(reporterName, getSelf()), getSelf());

        Optional<ReporterConfiguration> optional = configuration.reporters.stream()
                .filter(rconf -> rconf.reporter.equals(reporterName)).findFirst();

        if (optional.isPresent()) {
            final ReporterConfiguration reporterConfiguration = optional.get();

            logger.info("Creating reporter for class name {}", reporterName);
            try {
                reporterInstance = (Reporter) Class.forName(reporterName)
                        .getConstructor(ReporterConfiguration.class, GlobalConfiguration.class)
                        .newInstance(reporterConfiguration, configuration.global);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create reporter for class name " + reporterName, e);
            }
        } else {
            throw new RuntimeException("No reporter configuration present for reporter class " + reporterName);
        }
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
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
        return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class,
                msg -> logger.info("Subscribed to topic {}", reporterName))
                .match(Command.Start.class, o -> reporterInstance.start())
                .match(Command.Stop.class, o -> reporterInstance.stop())
                .match(Measurement.class, reporterInstance::report).match(Terminated.class, o -> terminate());

    }

    private void terminate() {

    }
}
