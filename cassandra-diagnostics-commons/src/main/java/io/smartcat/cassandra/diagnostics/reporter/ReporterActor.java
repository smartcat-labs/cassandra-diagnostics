package io.smartcat.cassandra.diagnostics.reporter;

import java.util.Optional;

import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.pf.ReceiveBuilder;
import io.smartcat.cassandra.diagnostics.actor.BaseActor;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * Reporter actor definition.
 */
public abstract class ReporterActor extends BaseActor {

    private final String reporterName;

    /**
     * System configuration.
     */
    protected Configuration configuration;

    /**
     * Reporter specific configuration.
     */
    protected final ReporterConfiguration reporterConfiguration;

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
            reporterConfiguration = optional.get();
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
                msg -> logger.info("Subscribed to topic {}", reporterName)).match(Command.Start.class, o -> start())
                .match(Command.Stop.class, o -> stop()).match(Measurement.class, this::report)
                .match(Terminated.class, o -> terminate());

    }

    /**
     * Module start method.
     */
    protected void start() {

    }

    /**
     * Module stop method.
     */
    protected void stop() {

    }

    /**
     * Report measurement.
     *
     * @param measurement measurement object
     */
    protected abstract void report(Measurement measurement);

    private void terminate() {

    }
}
