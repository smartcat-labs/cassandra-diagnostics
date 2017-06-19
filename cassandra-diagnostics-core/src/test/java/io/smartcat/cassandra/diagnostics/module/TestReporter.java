package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import akka.actor.ActorRef;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.ReporterActor;

/**
 * Test reporter class.
 */
public class TestReporter extends ReporterActor {

    private ActorRef target = null;

    public CountDownLatch latch;

    private final List<Measurement> reported = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param configuration reporter configuration
     */
    public TestReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);
    }

    @Override
    public Receive createReceive() {
        return defaultReceive().match(ActorRef.class, actorRef -> {
            target = actorRef;
            getSender().tell("done", getSelf());
        }).build();
    }

    protected void report(final Measurement measurement) {
        if (target != null) {
            target.tell(measurement, getSelf());
        }
        reported.add(measurement);
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * Prevent concurrency issues and return always copy of reported values.
     *
     * @return copy of all reported values.
     */
    public List<Measurement> getReported() {
        return new ArrayList<>(reported);
    }

}
