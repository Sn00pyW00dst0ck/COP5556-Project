package plp.group.PascalTypes;

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
