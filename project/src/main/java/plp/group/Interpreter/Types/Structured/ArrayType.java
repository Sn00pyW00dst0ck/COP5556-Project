package plp.group.Interpreter.Types.Structured;

import java.util.HashMap;

import plp.group.Interpreter.Types.GeneralType;

public class ArrayType<T> extends GeneralType {
    // TODO: figure out operations to support
    // TODO: figure out how to make this work with dynamic arrays

    private final Object lowerBound;
    private final Object upperBound;

    public ArrayType(HashMap<Object, T> value, Object lowerBound, Object upperBound) {
        super(value);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public void set() {

    }

    @SuppressWarnings("unchecked")
    public T get(Object index) {
        return ((HashMap<Object, T>) this.getValue()).get(index);
    }

    public Object getLowerBound() {
        return lowerBound;
    }

    public Object getUpperBound() {
        return upperBound;
    }

}
