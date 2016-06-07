package io.smartcat.cassandra.diagnostics.reporter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Reporter Configuration.
 */
public class ReporterConfiguration {

    /**
     * A reporter implementation's fully qualified Java class name
     */
    public String reporter;

    /**
     * A map containing optional reporter specific options.
     */
    public Map<String, String> options = new HashMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", reporter: \"").append(reporter).append("\", reporterOptions: ").append(options).append(" }");
        return sb.toString();
    }
}
