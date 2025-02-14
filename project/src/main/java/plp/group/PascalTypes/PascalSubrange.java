package plp.group.PascalTypes;

/**
 * Anything with a Comparable interface can be used to define a subrange.
 */
public class PascalSubrange<T extends Comparable<T>> extends PascalType {
    private T value;
    private final T min;
    private final T max;

    public PascalSubrange(T initialValue, T min, T max) {
        this.min = min;
        this.max = max;
        this.setValue(initialValue);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public void setValue(T value) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException("Value out of range: " + value);
        }
        this.value = value;
    }
}
