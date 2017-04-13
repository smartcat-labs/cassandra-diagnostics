package io.smartcat.cassandra.diagnostics.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diagnostics utilities.
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static String hostname = null;

    private static String systemname = "cassandra-cluster";

    private Utils() {

    }

    /**
     * Get system hostname if no hostname has been set.
     *
     * @return system hostname
     */
    public static String getHostname() {
        if (hostname != null) {
            return hostname;
        }

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            hostname = "UNKNOWN";
        }

        return hostname;
    }

    /**
     * Set system hostname and override {@code InetAddress} calls.
     *
     * @param hostname hostname value
     */
    public static void setHostname(final String hostname) {
        Utils.hostname = hostname;
    }

    /**
     * Get system name.
     *
     * @return system name
     */
    public static String getSystemname() {
        return systemname;
    }

    /**
     * Set system name.
     *
     * @param systemname systemname value
     */
    public static void setSystemname(final String systemname) {
        Utils.systemname = systemname;
    }

}
