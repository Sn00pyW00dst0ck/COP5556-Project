package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;
import java.math.BigInteger;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.GeneralTypeFactory;
import plp.group.Interpreter.Types.Simple.Integers.GeneralInteger;

public class GeneralReal extends GeneralType {

    public GeneralReal(BigDecimal value) {
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
        defineOperation("DIV", this::division); // No int division for real
        defineOperation("NEGATE", this::negate);
    }

    private GeneralType equalTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) == 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) == 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Real types.");
    }

    private GeneralType notEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) != 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) != 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Real types.");
    }

    private GeneralType lessThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) < 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) < 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Real types.");
    }

    private GeneralType lessThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) <= 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Real types.");
    }

    private GeneralType greaterThan(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) > 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) > 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Real types.");
    }

    private GeneralType greaterThanOrEqualTo(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createBoolean(
                            ((BigDecimal) lhs.getValue()).compareTo(new BigDecimal((BigInteger) rhs.getValue())) >= 0);
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory
                    .createBoolean((((BigDecimal) lhs.getValue())).compareTo(((BigDecimal) rhs.getValue())) >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare non-numeric type to Integer types.");
    }

    private GeneralType add(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createReal(((BigDecimal) lhs.getValue()).add(new BigDecimal(((BigInteger) rhs.getValue()))));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory.createReal(((BigDecimal) lhs.getValue()).add(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot add non-real types.");
    }

    private GeneralType subtract(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createReal(((BigDecimal) lhs.getValue()).subtract(new BigDecimal(((BigInteger) rhs.getValue()))));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory.createReal(((BigDecimal) lhs.getValue()).subtract(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot subtract non-real types.");
    }

    private GeneralType multiply(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createReal(((BigDecimal) lhs.getValue()).multiply(new BigDecimal(((BigInteger) rhs.getValue()))));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory.createReal(((BigDecimal) lhs.getValue()).multiply(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot multiply non-real types.");
    }

    private GeneralType division(GeneralType lhs, GeneralType rhs) {
        if (rhs instanceof GeneralInteger) {
            return GeneralTypeFactory
                    .createReal(((BigDecimal) lhs.getValue()).divide(new BigDecimal(((BigInteger) rhs.getValue()))));
        }
        if (rhs instanceof GeneralReal) {
            return GeneralTypeFactory.createReal(((BigDecimal) lhs.getValue()).divide(((BigDecimal) rhs.getValue())));
        }
        throw new UnsupportedOperationException("Cannot divide non-real types.");
    }

    private GeneralType negate(GeneralType lhs, GeneralType rhs) {
        if (lhs instanceof GeneralReal) {
            return GeneralTypeFactory.createReal(((BigDecimal) lhs.getValue()).multiply(new BigDecimal("-1")));
        }
        throw new UnsupportedOperationException("Cannot negate non-real type.");
    }

}
