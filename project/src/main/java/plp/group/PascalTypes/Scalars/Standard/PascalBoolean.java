package plp.group.PascalTypes.Scalars.Standard;

import plp.group.PascalTypes.PascalType;

public class PascalBoolean extends PascalType {
    private final boolean value;

    public PascalBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value ? "TRUE" : "FALSE";
    }
}
