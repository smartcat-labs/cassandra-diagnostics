package io.smartcat.cassandra.diagnostics;

/**
 * This class represents a module generated measurement.
 */
public class Measurement {

    private String name;
    private Query query;

    /**
     * Measurements name.
     *
     * @return measurement name
     */
    public String name() {
        return name;
    }

    /**
     * Query that triggered measurement.
     *
     * @return query object
     */
    public Query query() {
        return query;
    }

    private Measurement(final String name, final Query query) {
        this.name = name;
        this.query = query;
    }

    /**
     * Create a measurement object.
     * @param name Measurement name
     * @param query Query that this measurement represents
     * @return Measurement object
     */
    public static Measurement create(final String name, final Query query) {
        return new Measurement(name, query);
    }

    @Override
    public String toString() {
        return "Measurement [ " +
                "name=" + name +
                ", query=" + query.toString() +
                " ]";
    }

}
