package io.smartcat.cassandra.diagnostics.info;

/**
 * Wrapper around nodetool info command output. Encapsulates node information, such as transport information and uptime.
 */
public class NodeInfo {

    /**
     * Information about status of the gossip (active or not).
     */
    public final boolean gossipActive;

    /**
     * Information about status of the thrift (active or not).
     */
    public final boolean thriftActive;

    /**
     * Information about status of the native transport (active or not).
     */
    public final boolean nativeTransportActive;

    /**
     * Node uptime in seconds.
     */
    public final long uptimeInSeconds;

    /**
     * Count of uncaught exceptions.
     */
    public final int exceptionCount;

    /**
     * NodeInfo class constructor.
     *
     * @param gossipActive info if gossip is active
     * @param thriftActive info if thrift is active
     * @param nativeTransportActive info if native transport is active
     * @param uptimeInSeconds uptime in seconds
     * @param exceptionCount count of uncaught exceptions
     */
    public NodeInfo(boolean gossipActive, boolean thriftActive, boolean nativeTransportActive, long uptimeInSeconds,
            int exceptionCount) {
        super();
        this.gossipActive = gossipActive;
        this.thriftActive = thriftActive;
        this.nativeTransportActive = nativeTransportActive;
        this.uptimeInSeconds = uptimeInSeconds;
        this.exceptionCount = exceptionCount;
    }

    /**
     * Return info about Gossip protocol.
     *
     * @return 1 in case gossip is active, 0 otherwise.
     */
    public int isGossipActive() {
        return gossipActive ? 1 : 0;
    }

    /**
     * Return info about Thrift protocol.
     *
     * @return 1 in case thrift is active, 0 otherwise.
     */
    public int isThriftActive() {
        return thriftActive ? 1 : 0;
    }

    /**
     * Return info about native transport protocol.
     *
     * @return 1 in case native transport is active, 0 otherwise.
     */
    public int isNativeTransportActive() {
        return nativeTransportActive ? 1 : 0;
    }

}
