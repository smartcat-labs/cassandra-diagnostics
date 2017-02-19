package io.smartcat.cassandra.diagnostics.info;

import java.util.List;

/**
 * Info provider interface.
 */
public interface InfoProvider {

    /**
     * Gets the list of all active compactions.
     *
     * @return compaction info list
     */
    List<CompactionInfo> getCompactions();

    /**
     * Get the status of all thread pools.
     *
     * @return thread pools info list
     */
    List<TPStatsInfo> getTPStats();

}
