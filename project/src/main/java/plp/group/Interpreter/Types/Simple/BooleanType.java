package plp.group.Interpreter.Types.Simple;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.GeneralTypeFactory;

public class BooleanType extends GeneralType implements Comparable<BooleanType> {

    public BooleanType() {
        super(Boolean.class);
        defineOperations();
    }

    public BooleanType(Boolean value) {
        super(value);
        defineOperations();
    }

    private void defineOperations() {
        defineOperation("AND", this::AND);
        defineOperation("OR", this::OR);
        defineOperation("NOT", this::NOT);
        defineOperation("XOR", this::XOR);
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("<", this::lessThan);
        defineOperation("<=", this::lessThanOrEqualTo);
        defineOperation(">", this::greaterThan);
        defineOperation(">=", this::greaterThanOrEqualTo);
        defineOperation("..", this::range);
    }

    private GeneralType AND(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            return new BooleanType((Boolean) lhs.getValue() && (Boolean) rhs.getValue());
        }
        throw new UnsupportedOperationException("Cannot AND " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType OR(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            return new BooleanType((Boolean) lhs.getValue() || (Boolean) rhs.getValue());
        }
        throw new UnsupportedOperationException("Cannot OR " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType NOT(GeneralType lhs, GeneralType rhs) {
        return new BooleanType(!((Boolean) lhs.getValue()));
    }

    private GeneralType XOR(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            return new BooleanType((Boolean) lhs.getValue() ^ (Boolean) rhs.getValue());
        }
        throw new UnsupportedOperationException("Cannot XOR " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result == 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result != 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result < 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result > 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs.getValue() instanceof Boolean) {
            var result = ((Boolean) lhs.getValue()).compareTo((Boolean) rhs.getValue());
            return new BooleanType(result >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare " + rhs.getType() + " to a BooleanType.");
    }

    private GeneralType range(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof BooleanType && rhs instanceof BooleanType) {
            return GeneralTypeFactory.createSubrange((BooleanType) lhs, (BooleanType) rhs);
        }
        throw new UnsupportedOperationException("Cannot .. " + rhs.getType() + " to a BooleanType.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.getValue().equals(((BooleanType) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    // compareTo is needed to support making subranges of BooleanType
    @Override
    public int compareTo(BooleanType o) {
        return ((Boolean) this.getValue()).compareTo(((Boolean) o.getValue()));
    }

    @Override
    public String toString() {
        return ((Boolean) this.getValue()).toString().toUpperCase();
    }

}
