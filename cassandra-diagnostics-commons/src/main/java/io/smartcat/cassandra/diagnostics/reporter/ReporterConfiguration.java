package io.smartcat.cassandra.diagnostics.reporter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Reporter configuration.
 */
public class ReporterConfiguration {

    /**
     * A reporter implementation's fully qualified Java class name.
     */
    public String reporter;

    /**
     * A map containing optional reporter specific options.
     */
    public Map<String, Object> options = new HashMap<>();

    /**
     * Try to get option from list or return default value if option for key not provided.
     *
     * @param key          Option key
     * @param defaultValue Default value
     * @param <T>          Type of the value
     * @return either option for key or default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getDefaultOption(String key, T defaultValue) {
        Object value = options.get(key);

        return value == null ? defaultValue : (T) value;
    }

    /**
     * Get option value as defined type.
     *
     * @param key Option key.
     * @param <T> Type of return value.
     * @return option value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOption(String key) {
        return (T) options.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", reporter: \"").append(reporter).append("\", reporterOptions: ").append(options).append(" }");
        return sb.toString();
    }
}
