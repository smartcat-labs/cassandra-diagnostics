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
     * NodeInfo class constructor.
     *
     * @param gossipActive              info if gossip is active
     * @param thriftActive              info if thrift is active
     * @param nativeTransportActive     info if native transport is active
     * @param uptimeInSeconds           uptime in seconds
     */
    public NodeInfo(boolean gossipActive, boolean thriftActive, boolean nativeTransportActive, long uptimeInSeconds) {
        super();
        this.gossipActive = gossipActive;
        this.thriftActive = thriftActive;
        this.nativeTransportActive = nativeTransportActive;
        this.uptimeInSeconds = uptimeInSeconds;
    }

}
