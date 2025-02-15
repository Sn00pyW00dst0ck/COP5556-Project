package plp.group.Interpreter.Types.Simple;

import plp.group.Interpreter.Types.GeneralType;

public class StringType extends GeneralType implements Comparable<StringType> {

    public StringType(String value) {
        super(value);
        defineOperations();
    }

    private void defineOperations() {
        defineOperation("+", this::append);
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("<", this::lessThan);
        defineOperation("<=", this::lessThanOrEqualTo);
        defineOperation(">", this::greaterThan);
        defineOperation(">=", this::greaterThanOrEqualTo);
    }

    private GeneralType append(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof CharType) {
            return new StringType((String) (lhs.getValue()) + (Character) (rhs.getValue()));
        } else if (rhs instanceof StringType) {
            return new StringType((String) (lhs.getValue()) + (String) (rhs.getValue()));
        }
        throw new UnsupportedOperationException("Cannot append " + rhs.getType() + " to a StringType.");
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result == 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result == 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result != 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result != 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result < 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result < 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result <= 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result > 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result > 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((String) lhs.getValue()).compareTo((String) rhs.getValue());
            return new BooleanType(result >= 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((String) lhs.getValue()).compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.getValue().equals(((StringType) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    // compareTo is needed in order to support making subranges of StringType.
    @Override
    public int compareTo(StringType o) {
        return ((String) this.getValue()).compareTo((String) o.getValue());
    }

    @Override
    public String toString() {
        return ((String) this.getValue()).toString();
    }
}
