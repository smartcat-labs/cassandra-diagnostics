package io.smartcat.cassandra.diagnostics.info;

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
    public final String compactionId;

    /**
     * Compaction class constructor.
     *
     * @param total        total compaction size
     * @param completed    completed compacted size
     * @param unit         compaction size unit
     * @param taskType     compaction type
     * @param keyspace     compaction keyspace
     * @param columnFamily compaction column family
     * @param id           compaction id
     */
    public CompactionInfo(long total, long completed, String unit, String taskType, String keyspace,
            String columnFamily, String id) {
        this.total = total;
        this.completed = completed;
        this.completedPercentage = total == 0 ? 0 : (double) completed / total * 100;
        this.unit = unit;
        this.taskType = taskType;
        this.keyspace = keyspace;
        this.columnFamily = columnFamily;
        this.compactionId = id;
    }

}
