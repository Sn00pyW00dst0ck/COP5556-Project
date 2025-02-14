package plp.group.PascalTypes.Scalars.Standard;

import plp.group.PascalTypes.PascalType;

public class PascalString extends PascalType {
    private final String value;

    public PascalString(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
