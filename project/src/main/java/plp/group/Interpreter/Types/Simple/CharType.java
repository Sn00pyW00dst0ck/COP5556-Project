package plp.group.Interpreter.Types.Simple;

import plp.group.Interpreter.Types.GeneralType;

public class CharType extends GeneralType implements Comparable<CharType> {

    public CharType(Character value) {
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
            return new StringType("" + (Character) (lhs.getValue()) + (Character) (rhs.getValue()));
        } else if (rhs instanceof StringType) {
            return new StringType((Character) (lhs.getValue()) + (String) (rhs.getValue()));
        }
        throw new UnsupportedOperationException("Cannot append " + rhs.getType() + " to a CharType.");
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result == 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result == 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result != 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result != 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result < 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result < 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result <= 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result > 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
            return new BooleanType(result > 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a StringType.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof String) {
            var result = ((Character) lhs.getValue()).toString().compareTo((String) rhs.getValue());
            return new BooleanType(result >= 0);
        } else if (rhs.getValue() instanceof Character) {
            var result = ((Character) lhs.getValue()).toString().compareTo(((Character) rhs.getValue()).toString());
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
        return this.getValue().equals(((CharType) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    // compareTo is needed in order to support making subranges of CharType.
    @Override
    public int compareTo(CharType o) {
        return ((Character) this.getValue()).compareTo((Character) o.getValue());
    }

    @Override
    public String toString() {
        return ((Character) this.getValue()).toString();
    }

}
