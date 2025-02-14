package plp.group.PascalTypes.Structured;

import java.util.HashSet;

import plp.group.PascalTypes.PascalType;

public class PascalSet<T extends PascalType> extends PascalType {

    private HashSet<T> value = new HashSet<T>();

    public PascalSet() {
        isInitialized = false;
    }

    public PascalSet(@SuppressWarnings("unchecked") T... values) {
        for (T val : values) {
            this.value.add(val);
        }
    }

    @Override
    public HashSet<T> getValue() {
        return value;
    }
}
