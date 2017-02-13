package io.smartcat.cassandra.diagnostics.info;

import java.util.Map;

/**
 * Compaction info class.
 */
public class CompactionInfo {

    /**
     * Total compaction size in {@code unit} units.
     */
    public final long total;

    /**
     * Compaction completed size in {@code unit} units.
     */
    public final long completed;

    /**
     * Compaction completed percentage.
     */
    public final double completedPercentage;

    /**
     * Unit of compaction size.
     */
    public final String unit;

    /**
     * Compaction type.
     */
    public final String taskType;

    /**
     * Compaction keyspace.
     */
    public final String keyspace;

    /**
     * Compaction table.
     */
    public final String columnFamily;

    /**
     * Compaction id.
     */
    public final String id;

    /**
     * Compaction class constructor.
     *
     * @param compaction compaction map object retrieved from compaction manager MXBean
     */
    public CompactionInfo(Map<String, String> compaction) {
        total = Long.parseLong(compaction.get("total"));
        completed = Long.parseLong(compaction.get("completed"));
        completedPercentage = total == 0 ? 0 : (double) completed / total * 100;
        unit = compaction.get("unit");
        taskType = compaction.get("taskType");
        keyspace = compaction.get("keyspace");
        columnFamily = compaction.get("columnfamily");
        id = compaction.get("compactionId");
    }

}
