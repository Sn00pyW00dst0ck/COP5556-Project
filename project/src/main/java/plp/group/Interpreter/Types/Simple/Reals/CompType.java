package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;

public class CompType extends GeneralReal {
    private final BigDecimal lowerBound = new BigDecimal("-9223372036854775808");
    private final BigDecimal upperBound = new BigDecimal("9223372036854775807");

    public CompType() {
        super();
    }

    public CompType(BigDecimal value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigDecimal value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for CompType: " + value);
        }
    }
}
