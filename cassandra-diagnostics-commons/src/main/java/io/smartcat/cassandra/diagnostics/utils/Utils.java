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

    private Utils() {

    }

    /**
     * Get system hostname.
     *
     * @return system hostname
     */
    public static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            return "UNKNOWN";
        }
    }
}
