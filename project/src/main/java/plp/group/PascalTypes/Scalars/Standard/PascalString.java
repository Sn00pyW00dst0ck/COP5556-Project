package plp.group.PascalTypes.Scalars.Standard;

import plp.group.PascalTypes.PascalType;

public class PascalString extends PascalType {
    private String value;

    public PascalString() {
        this.isInitialized = false;
    }

    public PascalString(String value) {
        this.value = value;
        this.isInitialized = true;
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
