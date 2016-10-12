package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Module configuration.
 */
public class ModuleConfiguration {

    /**
     * A fully qualified Java class name of module implementation.
     */
    public String module;

    /**
     * A measurement name which defines how reporters report it.
     */
    public String measurement;

    /**
     * List of module specific options.
     */
    public Map<String, Object> options = new HashMap<>();

    /**
     * List of reporters for this module defined in the configuration.
     */
    public List<String> reporters = new ArrayList<>();

    /**
     * Get measurement name or default provided value.
     *
     * @param defaultMeasurement Default measurement name value
     * @return measurement name value
     */
    public String getMeasurementOrDefault(String defaultMeasurement) {
        return measurement == null || measurement.isEmpty() ? defaultMeasurement : measurement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", module: \"").append(module).append("\", measurement: ").append(measurement)
                .append(", moduleOptions: ").append(options).append(", moduleReporters: ").append(reporters)
                .append(" }");
        return sb.toString();
    }
}
