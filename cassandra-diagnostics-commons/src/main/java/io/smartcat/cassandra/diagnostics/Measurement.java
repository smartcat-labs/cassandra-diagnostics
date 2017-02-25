package io.smartcat.cassandra.diagnostics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.utils.Option;

/**
 * This class represents a module generated measurement.
 * There are two types of measurements:
 * * Simple measurement: Usually scalar values defining a single measurement
 * * Complex measurement: Provides rich data for a single point in time
 *
 * Measurement type is differentiated by actually holding a scalar value.
 */
public class Measurement {
    private final String name;
    private final Option<Double> value;
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
     * Defines if measurement has a value making it a simple measurement.
     *
     * @return has value
     */
    public boolean hasValue() {
        return value.hasValue();
    }

    /**
     * If measurement contains a value, retrieve it.
     *
     * @return measurement value
     */
    public double getValue() {
        return value.getValue();
    }

    /**
     * If measurement contains a value, retrieve it or return a provided value.
     *
     * @param defaultValue default value in case measurement value is null.
     * @return measurement or default value
     */
    public double getOrDefault(Double defaultValue) {
        return value.getOrDefault(defaultValue);
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

    private Measurement(final String name, final Double value, final long time, final TimeUnit timeUnit,
            final Map<String, String> tags, final Map<String, String> fields) {
        this.name = name;
        this.value = Option.ofNullable(value);
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
    public static Measurement create(final String name, final Double value, final long time, final TimeUnit timeUnit,
            final Map<String, String> tags, final Map<String, String> fields) {
        return new Measurement(name, value, time, timeUnit, tags, fields);
    }

    @Override
    public String toString() {
        return "Measurement [ " + "name=" + name + ", value=" + (value.hasValue() ? value.getValue() : "null")
                + ", time=" + time + ", timeUnit=" + timeUnit + ", tags: " + tags + ", fields: " + fields + " ]";
    }

    /**
     * Encodes measurement into JSON.
     *
     * @return JSON-formatted string representation of measurement.
     */
    public String toJson() {
        return "{\"name\":\"" + name + "\"" + ",\"value\":" + (value.hasValue() ? value.getValue() : "null")
                + ",\"time\":" + time + ",\"timeUnit\":\"" + timeUnit + "\"" + ",\"tags\":" + appendMap(tags)
                + ",\"fields\":" + appendMap(fields) + "}";
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
