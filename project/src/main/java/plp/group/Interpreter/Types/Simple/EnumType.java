package plp.group.Interpreter.Types.Simple;

import java.util.Map;

import plp.group.Interpreter.Types.GeneralType;

public class EnumType extends GeneralType implements Comparable<EnumType> {
    private final Map<String, Integer> enumValues;

    public EnumType(Map<String, Integer> enumValues, String value) {
        super(value);
        this.enumValues = enumValues;
        defineOperations();
    }

    @Override
    public Object getValue() {
        return enumValues.get((String) super.getValue());
    }

    private void defineOperations() {
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("<", this::lessThan);
        defineOperation("<=", this::lessThanOrEqualTo);
        defineOperation(">", this::greaterThan);
        defineOperation(">=", this::greaterThanOrEqualTo);
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result == 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result != 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result < 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result > 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof EnumType) {
            var result = ((Integer) ((EnumType) lhs).getValue()).compareTo(((Integer) ((EnumType) rhs).getValue()));
            return new BooleanType(result >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to an EnumType.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.getValue().equals(((EnumType) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    // compareTo is needed to support subranges of EnumType
    @Override
    public int compareTo(EnumType o) {
        return ((Integer) this.getValue()).compareTo(((Integer) o.getValue()));
    }

}
