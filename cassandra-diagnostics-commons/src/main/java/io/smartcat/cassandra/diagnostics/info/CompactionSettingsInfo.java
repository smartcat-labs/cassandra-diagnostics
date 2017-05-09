package io.smartcat.cassandra.diagnostics.info;

/**
 * Info about all compactions on node.
 */
public class CompactionSettingsInfo {

    /**
     * Compaction throughput.
     */
    public final int compactionThroughput;

    /**
     * Core compactor threads.
     */
    public final int coreCompactorThreads;

    /**
     * Maximum compactor threads.
     */
    public final int maximumCompactorThreads;

    /**
     * Core validator threads.
     */
    public final int coreValidatorThreads;

    /**
     * Maximum validator threads.
     */
    public final int maximumValidatorThreads;

    /**
     * Compaction class constructor.
     *
     * @param compactionThroughput      compaction throughput
     * @param coreCompactorThreads      core compactor threads
     * @param maximumCompactorThreads   maximum compactor threads
     * @param coreValidatorThreads core validator threads
     * @param maximumValidatorThreads   maximum validator threads
     */
    public CompactionSettingsInfo(int compactionThroughput, int coreCompactorThreads, int maximumCompactorThreads,
            int coreValidatorThreads, int maximumValidatorThreads) {
        this.compactionThroughput = compactionThroughput;
        this.coreCompactorThreads = coreCompactorThreads;
        this.maximumCompactorThreads = maximumCompactorThreads;
        this.coreValidatorThreads = coreValidatorThreads;
        this.maximumValidatorThreads = maximumValidatorThreads;
    }

}
