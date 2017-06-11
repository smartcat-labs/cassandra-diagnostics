package io.smartcat.cassandra.diagnostics.module;

import java.util.Optional;

import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.pf.ReceiveBuilder;
import io.smartcat.cassandra.diagnostics.actor.BaseActor;
import io.smartcat.cassandra.diagnostics.actor.Topics;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.query.Query;

/**
 * Module actor definition.
 */
public abstract class ModuleActor extends BaseActor {

    private final String moduleName;

    /**
     * System configuration.
     */
    protected Configuration configuration;

    /**
     * Module specific configuration.
     */
    protected ModuleConfiguration moduleConfiguration;

    /**
     * Constructor.
     *
     * @param moduleName    module class name
     * @param configuration configuration
     */
    public ModuleActor(final String moduleName, final Configuration configuration) {
        this.moduleName = moduleName;
        this.configuration = configuration;

        // Subscribe to process query topic
        this.mediator.tell(new DistributedPubSubMediator.Subscribe(Topics.PROCESS_QUERY_TOPIC, getSelf()), getSelf());

        Optional<ModuleConfiguration> optional = configuration.modules.stream()
                .filter(mconf -> mconf.module.equals(this.moduleName)).findFirst();
        if (optional.isPresent()) {
            moduleConfiguration = optional.get();
        } else {
            throw new RuntimeException("No module configuration present for module class " + moduleName);
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
        return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class, msg -> logger.info("Subscribed"))
                .match(Command.Start.class, o -> start()).match(Command.Stop.class, o -> stop())
                .match(Query.class, this::process).match(Terminated.class, o -> terminate());
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
     * Process query method.
     *
     * @param query query object
     */
    protected void process(Query query) {

    }

    /**
     * Report measurement to all predefined reporters.
     *
     * @param measurement measurement object
     */
    protected void report(Measurement measurement) {
        moduleConfiguration.reporters.stream().forEach(
                (reporter) -> mediator.tell(new DistributedPubSubMediator.Publish(reporter, measurement), getSelf()));
    }

    private void terminate() {

    }

}
