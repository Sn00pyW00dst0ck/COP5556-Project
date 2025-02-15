package plp.group.Interpreter.Types;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class GeneralType {
    private Object value;
    private Class<?> type;
    private final Map<String, BiFunction<GeneralType, GeneralType, GeneralType>> operations = new HashMap<>();

    public GeneralType(Class<?> type) {
        this.value = null;
        this.type = type;
    }

    public GeneralType(Object value) {
        this.value = value;
        this.type = value != null ? value.getClass() : Object.class;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void defineOperation(String op, BiFunction<GeneralType, GeneralType, GeneralType> function) {
        operations.put(op, function);
    }

    public GeneralType applyOperation(String op, GeneralType other) {
        if (!operations.containsKey(op)) {
            throw new UnsupportedOperationException("Operation " + op + " is not defined for " + type.getSimpleName());
        }
        return operations.get(op).apply(this, other);
    }

    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> expectedType) {
        if (!expectedType.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "Cannot cast " + type.getSimpleName() + " to " + expectedType.getSimpleName());
        }
        return (T) value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return Objects.equals(this.getValue(), ((GeneralType) o).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

}
