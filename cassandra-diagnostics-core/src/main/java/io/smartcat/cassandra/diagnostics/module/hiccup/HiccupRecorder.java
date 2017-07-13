package io.smartcat.cassandra.diagnostics.module.hiccup;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.SingleWriterRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hiccup recorder class. Taken from https://github.com/giltene/jHiccup.
 * All credits go to Gil Tene of Azul Systems.
 */
public class HiccupRecorder extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(HiccupRecorder.class);

    private static final String HICCUP_RECORDER_THREAD_NAME = "hiccup-recorder";

    /**
     * Thread running.
     */
    public volatile boolean doRun;

    /**
     * Allocation object.
     */
    public volatile Long lastSleepTimeObj; // public volatile to make sure allocs are not optimized away...

    private final SingleWriterRecorder recorder;

    private final HiccupConfiguration config;

    /**
     * Hiccup recorder constructor.
     *
     * @param config   Hiccup configuration
     * @param recorder Histogram single writer recorder
     */
    public HiccupRecorder(final HiccupConfiguration config, final SingleWriterRecorder recorder) {
        this.setDaemon(true);
        this.setName(HICCUP_RECORDER_THREAD_NAME);
        this.recorder = recorder;
        this.config = config;
        doRun = true;
    }

    /**
     * Terminate running thread.
     */
    public void terminate() {
        doRun = false;
    }

    /**
     * Current time with delay getter.
     *
     * @param nextReportingTime next histogram reporting time
     * @return current time with delay
     * @throws InterruptedException Interrupted exception
     */
    public long getCurrentTimeMsecWithDelay(final long nextReportingTime) throws InterruptedException {
        final long now = System.currentTimeMillis();
        if (now < nextReportingTime) {
            Thread.sleep(nextReportingTime - now);
        }
        return now;
    }

    @Override
    public void run() {
        final long resolutionNsec = (long) (config.resolutionInMs() * 1000L * 1000L);
        try {
            long shortestObservedDeltaTimeNsec = Long.MAX_VALUE;
            while (doRun) {
                final long timeBeforeMeasurement = System.nanoTime();
                if (config.resolutionInMs() != 0) {
                    TimeUnit.NANOSECONDS.sleep(resolutionNsec);
                    if (config.allocateObjects()) {
                        // Allocate an object to make sure potential allocation stalls are measured.
                        lastSleepTimeObj = new Long(timeBeforeMeasurement);
                    }
                }
                final long timeAfterMeasurement = System.nanoTime();
                final long deltaTimeNsec = timeAfterMeasurement - timeBeforeMeasurement;

                if (deltaTimeNsec < shortestObservedDeltaTimeNsec) {
                    shortestObservedDeltaTimeNsec = deltaTimeNsec;
                }

                long hiccupTimeNsec = deltaTimeNsec - shortestObservedDeltaTimeNsec;

                recorder.recordValueWithExpectedInterval(hiccupTimeNsec, resolutionNsec);
            }
        } catch (InterruptedException e) {
            logger.debug("# HiccupRecorder interrupted/terminating...");
        }
    }
}
