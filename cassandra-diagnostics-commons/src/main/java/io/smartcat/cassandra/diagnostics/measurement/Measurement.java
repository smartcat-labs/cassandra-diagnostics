package io.smartcat.cassandra.diagnostics.measurement;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a module generated measurement.
 * There are two types of measurements:
 * * Simple measurement: Usually scalar values defining a single measurement
 * * Complex measurement: Provides rich data for a single point in time
 */
public final class Measurement {

    /**
     * Defines possible measurement types.
     */
    public enum MeasurementType {
        /**
         * Simple measurement holds timestamp and value.
         */
        SIMPLE,

        /**
         * Complex measurement is a group of measurements which are in fields and have names and values.
         */
        COMPLEX
    }

    /**
     * Measurements name.
     */
    public final String name;

    /**
     * Measurements type.
     */
    public final MeasurementType type;

    /**
     * Measurement value.
     */
    public final double value;

    /**
     * Measurement time in milliseconds since epoch.
     */
    public final long time;

    /**
     * Measurement tags.
     */
    public final Map<String, String> tags;

    /**
     * Measurement fields.
     */
    public final Map<String, String> fields;

    private Measurement(final String name, final MeasurementType type, final double value, final long time,
            final Map<String, String> tags, final Map<String, String> fields) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.time = time;
        this.tags = tags;
        this.fields = fields;
    }

    /**
     * Create a simple measurement object.
     *
     * @param name   Measurement name
     * @param time   Measurement time
     * @param tags   Tag name value pairs
     * @param fields Field name value pairs
     * @return Measurement object
     */
    public static Measurement createComplex(final String name, final long time, final Map<String, String> tags,
            final Map<String, String> fields) {
        return new Measurement(name, MeasurementType.COMPLEX, 0, time, tags, fields);
    }

    /**
     * Create a simple measurement object.
     *
     * @param name   Measurement name
     * @param value  Measurement value
     * @param time   Measurement time
     * @param tags   Tag name value pairs
     * @param fields Field name value pairs
     * @return Measurement object
     */
    public static Measurement createSimple(final String name, final double value, final long time,
            final Map<String, String> tags, final Map<String, String> fields) {
        return new Measurement(name, MeasurementType.SIMPLE, value, time, tags, fields);
    }

    /**
     * Create a simple measurement object.
     *
     * @param name  Measurement name
     * @param value Measurement value
     * @param time  Measurement time
     * @param tags  Tag name value pairs
     * @return Measurement object
     */
    public static Measurement createSimple(final String name, final double value, final long time,
            final Map<String, String> tags) {
        return new Measurement(name, MeasurementType.SIMPLE, value, time, tags, new HashMap<>());
    }

    /**
     * Check if measurement is simple measurement.
     *
     * @return has value
     */
    public boolean isSimple() {
        return this.type == MeasurementType.SIMPLE;
    }

    @Override
    public String toString() {
        return "Measurement [ " + "name=" + name + ", type=" + type + ", value=" + value + ", time=" + time + ", tags: "
                + tags + ", fields: " + fields + " ]";
    }

    /**
     * Encodes measurement into JSON.
     *
     * @return JSON-formatted string representation of measurement.
     */
    public String toJson() {
        return "{\"name\":\"" + name + "\"" + ",\"type\":\"" + type + "\"" + ",\"value\":" + value + ",\"time\":" + time
                + ",\"tags\":" + appendMap(tags) + ",\"fields\":" + appendMap(fields) + "}";
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
