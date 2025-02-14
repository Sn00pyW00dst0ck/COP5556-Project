package plp.group.PascalTypes;

public abstract class PascalType {
    protected boolean isInitialized = false;

    public abstract Object getValue();

    public Boolean isInitialized() {
        return isInitialized;
    };
}
