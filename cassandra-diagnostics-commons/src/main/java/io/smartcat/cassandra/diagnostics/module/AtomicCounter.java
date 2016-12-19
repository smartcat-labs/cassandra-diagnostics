package io.smartcat.cassandra.diagnostics.module;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Atomic counter implementation.
 */
public class AtomicCounter {

    private AtomicLong counter = new AtomicLong();

    /**
     * Increment value by {@code 1}.
     */
    public void increment() {
        counter.incrementAndGet();
    }

    /**
     * Decrement value by {@code 1}.
     */
    public void decrement() {
        counter.decrementAndGet();
    }

    /**
     * Get sum then reset counter to {@code 0}.
     *
     * @return Returns sum.
     */
    public long sumThenReset() {
        return counter.getAndSet(0);
    }

}
