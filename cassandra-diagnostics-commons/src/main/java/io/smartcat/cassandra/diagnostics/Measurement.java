package io.smartcat.cassandra.diagnostics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a module generated measurement.
 */
public class Measurement {
    private final String name;
    private final double value;
    private final long time;
    private final TimeUnit timeUnit;
    private final Map<String, String> tags;
    private final Map<String, String> fields;

    /**
     * Measurements name.
     *
     * @return measurement name
     */
    public String name() {
        return name;
    }

    /**
     * Measurement value.
     *
     * @return measurement value
     */
    public double value() {
        return value;
    }

    /**
     * Measurement time.
     *
     * @return measurement time
     */
    public long time() {
        return time;
    }

    /**
     * Measurement time unit.
     *
     * @return measurement time unit
     */
    public TimeUnit timeUnit() {
        return timeUnit;
    }

    /**
     * Measurement tags.
     *
     * @return Tag name value pairs
     */
    public Map<String, String> tags() {
        return tags;
    }

    /**
     * Measurement fields.
     *
     * @return Field name value pairs
     */
    public Map<String, String> fields() {
        return fields;
    }

    private Measurement(final String name, final double value, final long time, final TimeUnit timeUnit,
            final Map<String, String> tags, final Map<String, String> fields) {
        this.name = name;
        this.value = value;
        this.time = time;
        this.timeUnit = timeUnit;
        this.tags = tags;
        this.fields = fields;
    }

    /**
     * Create a measurement object.
     *
     * @param name     Measurement name
     * @param value    Measurement value
     * @param time     Measurement time
     * @param timeUnit Measurement time unit
     * @param tags     Tag name value pairs
     * @param fields   Field name value pairs
     * @return Measurement object
     */
    public static Measurement create(final String name, final double value, final long time, final TimeUnit timeUnit,
            final Map<String, String> tags, final Map<String, String> fields) {
        return new Measurement(name, value, time, timeUnit, tags, fields);
    }

    @Override
    public String toString() {
        return "Measurement [ " +
                "name=" + name +
                ", value=" + value +
                ", time=" + time +
                ", timeUnit=" + timeUnit +
                ", tags: " + tags +
                ", fields: " + fields +
                " ]";
    }

    /**
     * Encodes measurement into JSON.
     *
     * @return JSON-formatted string representation of measurement.
     */
    public String toJson() {
        return "{\"name\":\"" + name + "\"" +
                ",\"value\":" + value +
                ",\"time\":" + time +
                ",\"timeUnit\":\"" + timeUnit + "\"" +
                ",\"tags\":" + appendMap(tags) +
                ",\"fields\":" + appendMap(fields) +
                "}";
    }

    private String appendMap(Map<String, String> map) {
        StringBuilder builder = new StringBuilder("{");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":\"");
            builder.append(entry.getValue());
            builder.append("\",");
        }

        if (builder.length() > 1) {
            builder.replace(builder.length() - 1, builder.length(), "}");
        } else {
            builder.append("}");
        }

        return builder.toString();
    }
}
