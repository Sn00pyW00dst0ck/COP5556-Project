package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigDecimal;
import java.math.BigInteger;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.GeneralTypeFactory;
import plp.group.Interpreter.Types.Simple.Reals.GeneralReal;

public abstract class GeneralInteger extends GeneralType implements Comparable<GeneralInteger> {

    public GeneralInteger() {
        super(BigInteger.class);
        defineOperations();
    }

    public GeneralInteger(BigInteger value) {
        super(value);
        defineOperations();
    }

    protected void defineOperations() {
        defineOperation("=", this::equalTo);
        defineOperation("<>", this::notEqualTo);
        defineOperation("<", this::lessThan);
        defineOperation("<=", this::lessThanOrEqualTo);
        defineOperation(">", this::greaterThan);
        defineOperation(">=", this::greaterThanOrEqualTo);

        defineOperation("+", this::add);
        defineOperation("-", this::subtract);
        defineOperation("*", this::multiply);
        defineOperation("/", this::division);
        defineOperation("DIV", this::intDivision);
        defineOperation("MOD", this::modulus);
        defineOperation("NEGATE", this::negate);

        defineOperation("..", this::range);
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) == 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue()))
                                    .compareTo(((BigDecimal) rhs.getValue())) == 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) != 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue()))
                                    .compareTo(((BigDecimal) rhs.getValue())) != 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) < 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) < 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) <= 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue()))
                                    .compareTo(((BigDecimal) rhs.getValue())) <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) > 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue()))
                                    .compareTo(((BigDecimal) rhs.getValue())) > 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(((BigInteger) lhs.getValue()).compareTo(((BigInteger) rhs.getValue())) >= 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean(
                            (new BigDecimal((BigInteger) lhs.getValue()))
                                    .compareTo(((BigDecimal) rhs.getValue())) >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType add(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).add(((BigInteger) rhs.getValue())));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createReal((new BigDecimal((BigInteger) lhs.getValue())).add(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot add non-integer types.");
    }

    private GeneralType subtract(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).subtract(((BigInteger) rhs.getValue())));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createReal((new BigDecimal((BigInteger) lhs.getValue())).subtract(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot subtract non-integer types.");
    }

    private GeneralType multiply(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).multiply(((BigInteger) rhs.getValue())));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createReal((new BigDecimal((BigInteger) lhs.getValue())).multiply(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot multiply non-integer types.");
    }

    private GeneralType division(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).divide(((BigInteger) rhs.getValue())));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createReal((new BigDecimal((BigInteger) lhs.getValue())).divide(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot divide non-integer types.");
    }

    private GeneralType intDivision(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).divide(((BigInteger) rhs.getValue())));
        }
        // TODO: this case is hard
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createReal((new BigDecimal((BigInteger) lhs.getValue())).divide(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot integer divide non-integer types.");
    }

    private GeneralType modulus(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createInteger(((BigInteger) lhs.getValue()).mod(((BigInteger) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot modulus non-integer types.");
    }

    private GeneralType negate(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof GeneralInteger) {
            return GeneralTypeFactory.createInteger(((BigInteger) lhs.getValue()).multiply(new BigInteger("-1")));
        }
        throw new UnsupportedOperationException("Cannot negate non-integer type.");
    }

    private GeneralType range(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof GeneralInteger && rhs instanceof GeneralInteger) {
            return GeneralTypeFactory.createSubrange((GeneralInteger) lhs, (GeneralInteger) rhs);
        }
        throw new UnsupportedOperationException("Cannot .. non-integer type.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.getValue().equals(((GeneralInteger) o).getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    // compareTo is needed in order to support making subranges of GeneralInteger.
    @Override
    public int compareTo(GeneralInteger o) {
        return ((BigInteger) this.getValue()).compareTo((BigInteger) o.getValue());
    }

    @Override
    public String toString() {
        return ((BigInteger) this.getValue()).toString();
    }
}
