package plp.group.PascalTypes.Scalars.Standard;

import plp.group.PascalTypes.PascalType;

public class PascalBoolean extends PascalType {
    private boolean value;

    public PascalBoolean() {
        this.isInitialized = false;
    }

    public PascalBoolean(boolean value) {
        this.value = value;
        this.isInitialized = true;
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
