package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class LatchTestReporter extends Reporter {

    private CountDownLatch latch;

    private final List<Measurement> reported = new ArrayList<>();

    public LatchTestReporter(ReporterConfiguration configuration, CountDownLatch latch) {
        super(configuration);
        this.latch = latch;
    }

    @Override
    public void report(Measurement measurement) {
        reported.add(measurement);
        System.out.println(measurement.toString());
        latch.countDown();
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
