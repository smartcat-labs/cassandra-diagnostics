package io.smartcat.cassandra.diagnostics.info;

/**
 * Thread pool stats info class.
 */
public class TPStatsInfo {

    /**
     * Thread pool name.
     */
    public final String threadPool;

    /**
     * Thread pool active tasks.
     */
    public final long activeTasks;

    /**
     * Thread pool pending tasks.
     */
    public final long pendingTasks;

    /**
     * Thread pool completed tasks.
     */
    public final long completedTasks;

    /**
     * Thread pool blocked tasks.
     */
    public final long currentlyBlockedTasks;

    /**
     * Thread pool all time blocked tasks.
     */
    public final long totalBlockedTasks;

    /**
     * Thread pool stats info.
     *
     * @param threadPool            thread pool name
     * @param activeTasks           active tasks
     * @param pendingTasks          pending tasks
     * @param completedTasks        completed tasks
     * @param currentlyBlockedTasks currently blocked tasks
     * @param totalBlockedTasks     total blocked tasks
     */
    public TPStatsInfo(String threadPool, long activeTasks, long pendingTasks, long completedTasks,
            long currentlyBlockedTasks, long totalBlockedTasks) {
        this.threadPool = threadPool;
        this.activeTasks = activeTasks;
        this.pendingTasks = pendingTasks;
        this.completedTasks = completedTasks;
        this.currentlyBlockedTasks = currentlyBlockedTasks;
        this.totalBlockedTasks = totalBlockedTasks;
    }

}
