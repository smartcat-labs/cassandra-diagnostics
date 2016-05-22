package io.smartcat.cassandra.diagnostics.jmx;

/**
 * JMX MXBean for monitoring and managing Cassandra Diagnostics module.
 */
public interface DiagnosticsMXBean {
    /**
     * Reads the value of the slow query threshold in milliseconds.
     *
     * @return the slow query threshold in milliseconds
     */
    long getSlowQueryThresholdInMilliseconds();

    /**
     * Sets the value of the slow query threshold.
     *
     * @param value a new slow query threshold value
     */
    void setSlowQueryThresholdInMilliseconds(long value);
}
