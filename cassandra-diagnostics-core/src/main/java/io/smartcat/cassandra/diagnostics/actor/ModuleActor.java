package io.smartcat.cassandra.diagnostics.actor;

import java.lang.reflect.Constructor;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.japi.Creator;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;

/**
 * Module actor definition.
 */
public abstract class ModuleActor extends BaseActor {

    private final ActorRef mediator;

    private final String moduleName;

    protected Configuration configuration;

    protected ModuleConfiguration moduleConfiguration;

    /**
     * Create actor props from module actor class name.
     *
     * @param className     Module actor class name
     * @param configuration configuration
     * @return Module actor instance
     * @throws ClassNotFoundException no class found
     * @throws NoSuchMethodException  no such method
     */
    public static Props props(final String className, final Configuration configuration)
            throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = Class.forName(className).getConstructor(String.class, Configuration.class);
        return Props.create(ModuleActor.class, new Creator<ModuleActor>() {
            @Override
            public ModuleActor create() throws Exception {
                return (ModuleActor) constructor.newInstance(className, configuration);
            }
        });
    }

    /**
     * Constructor.
     *
     * @param moduleName    module class name
     * @param configuration configuration
     */
    public ModuleActor(String moduleName, Configuration configuration) {
        this.moduleName = moduleName;
        this.configuration = configuration;

        // Subscribe to process query topic
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(Topics.QUERY_PROCESS_TOPIC, getSelf()), getSelf());

        Optional<ModuleConfiguration> optional = configuration.modules.stream()
                .filter(mconf -> mconf.module.equals(moduleName)).findFirst();
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
        return receiveBuilder().match(DistributedPubSubMediator.SubscribeAck.class, msg -> logger.info("Subscribed"))
                .match(Messages.Start.class, o -> start()).match(Messages.Stop.class, o -> stop())
                .match(Query.class, this::process).match(Terminated.class, o -> terminate()).build();
    }

    /**
     * Module start method.
     */
    protected abstract void start();

    /**
     * Module stop method.
     */
    protected abstract void stop();

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
