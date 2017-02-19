package io.smartcat.cassandra.diagnostics.utils;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Option class used for wrapping Java 1.7 API scalar values.
 *
 * @param <T> wrapped value type
 */
public class Option<T> {

    /**
     * Empty instance.
     */
    private static final Option<?> EMPTY = new Option<>();

    /**
     * If non-null, the value; if null, indicates no value is present.
     */
    public final T value;

    /**
     * Constructs an empty instance.
     */
    private Option() {
        this.value = null;
    }

    /**
     * Returns an empty {@code Option} instance.  No value is present for this
     * Option.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code Option}
     */
    public static <T> Option<T> empty() {
        @SuppressWarnings("unchecked")
        Option<T> val = (Option<T>) EMPTY;
        return val;
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     * @throws NullPointerException if value is null
     */
    private Option(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Returns an {@code Optional} with the specified present non-null value.
     *
     * @param <T>   the class of the value
     * @param value the value to be present, which must be non-null
     * @return an {@code Optional} with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> Option<T> of(T value) {
        return new Option<>(value);
    }

    /**
     * Returns an {@code Optional} describing the specified value, if non-null,
     * otherwise returns an empty {@code Optional}.
     *
     * @param <T>   the class of the value
     * @param value the possibly-null value to describe
     * @return an {@code Optional} with a present value if the specified value is non-null, otherwise an empty {@code
     * Optional}
     */
    public static <T> Option<T> ofNullable(T value) {
        return (value == null) ? (Option<T>) empty() : of(value);
    }

    /**
     * Returns the value if not empty otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value
     * @throws NoSuchElementException if there is no value present
     * @see Option#hasValue()
     */
    public T getValue() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Return the value if present, otherwise return {@code defaultValue}.
     *
     * @param defaultValue the value to be returned if there is no value present, may be null
     * @return the value, if present, otherwise {@code defaultValue}
     */
    public T getOrDefault(T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Indicates whether some other object is "equal to" this Option. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Option} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Option)) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this Option suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return value != null ? String.format("Option[%s]", value) : "Option.empty";
    }
}
