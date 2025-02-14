package plp.group.PascalTypes.Scalars.UserDefined;

import java.util.Map;

import plp.group.PascalTypes.PascalType;

public class PascalEnum extends PascalType implements Comparable<PascalEnum> {
    // enumValues is a map of the name to an integer 'value'.
    // Example:
    // RED -> 0
    // GREEN -> 1
    // BLUE -> 2
    private final Map<String, Integer> enumValues;
    private final String value;

    public PascalEnum(Map<String, Integer> enumValues, String value) {
        this.enumValues = enumValues;
        if (!enumValues.containsKey(value)) {
            throw new IllegalArgumentException("Invalid enumeration value: " + value);
        }
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public int getOrdinal() {
        return enumValues.get(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int compareTo(PascalEnum o) {
        return Integer.compare(this.getOrdinal(), o.getOrdinal());
    }
}
