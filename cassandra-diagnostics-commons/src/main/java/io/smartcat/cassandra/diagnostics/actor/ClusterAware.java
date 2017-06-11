package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.ClusterDomainEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Cluster aware actor.
 */
public abstract class ClusterAware extends AbstractActor {

    /**
     * Logger instance via logging adapter.
     */
    protected final LoggingAdapter logger = Logging.getLogger(getContext().getSystem().eventStream(), this);

    /**
     * Actor cluster instance from current actor system.
     */
    protected final Cluster cluster = Cluster.get(getContext().getSystem());

    /**
     * Method executed prior to start.
     */
    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterDomainEvent.class);
    }

    /**
     * Method executed post stop.
     */
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(MemberUp.class, mUp -> {
            logger.info("Member is Up: {}", mUp.member());
        }).match(UnreachableMember.class, mUnreachable -> {
            logger.info("Member detected as unreachable: {}", mUnreachable.member());
        }).match(MemberRemoved.class, member -> {
            logger.info("Member is Removed: {} after {}", member.member(), member.previousStatus());
        }).match(MemberEvent.class, message -> {
            // ignore
            //        }).match(ClusterMetricsChanged.class, forNode -> {
            //            logMetrics(forNode)
        }).build();
    }

}
