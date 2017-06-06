package io.smartcat.cassandra.diagnostics.actor;

import java.lang.reflect.Constructor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.Creator;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * Reporter actor definition.
 */
public abstract class ReporterActor extends BaseActor {

    private final ReporterConfiguration configuration;

    /**
     * Create actor props from reporter actor class name.
     *
     * @param reporterConfiguration Reporter actor configuration
     * @return Reporter actor instance
     * @throws ClassNotFoundException no class found
     * @throws NoSuchMethodException  no such method
     */
    public static Props props(final ReporterConfiguration reporterConfiguration)
            throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = Class.forName(reporterConfiguration.reporter)
                .getConstructor(ReporterConfiguration.class);
        return Props.create(ReporterActor.class, new Creator<ReporterActor>() {
            @Override
            public ReporterActor create() throws Exception {
                return (ReporterActor) constructor.newInstance(reporterConfiguration);
            }
        });
    }

    /**
     * Constructor.
     *
     * @param configuration reporter configuration
     */
    public ReporterActor(ReporterConfiguration configuration) {
        this.configuration = configuration;

        // Subscribe to reporter name topic
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(configuration.reporter, getSelf()), getSelf());
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class,
                msg -> logger.info("Subscribed to topic {}", configuration.reporter))
                .match(Terminated.class, o -> terminate()).match(Measurement.class, this::report).build();
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
