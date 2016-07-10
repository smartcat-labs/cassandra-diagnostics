package io.smartcat.cassandra.diagnostics.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 */
public class Meter {
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final LongAdder count = new LongAdder();
    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    /**
     * Creates a new {@link Meter}.
     */
    public Meter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock the clock to use for the meter ticks
     */
    public Meter(Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        tickIfNecessary();
        count.add(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    m1Rate.tick();
                    m5Rate.tick();
                    m15Rate.tick();
                }
            }
        }
    }

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    public long getCount() {
        return count.sum();
    }

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the fifteen-minute load average in the
     * {@code top} Unix command.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created
     */
    public double getFifteenMinuteRate() {
        tickIfNecessary();
        return m15Rate.getRate(TimeUnit.SECONDS);
    }

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the five-minute load average in the {@code
     * top} Unix command.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created
     */
    public double getFiveMinuteRate() {
        tickIfNecessary();
        return m5Rate.getRate(TimeUnit.SECONDS);
    }

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = (clock.getTick() - startTime);
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the one-minute load average in the {@code
     * top} Unix command.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created
     */
    public double getOneMinuteRate() {
        tickIfNecessary();
        return m1Rate.getRate(TimeUnit.SECONDS);
    }
}
