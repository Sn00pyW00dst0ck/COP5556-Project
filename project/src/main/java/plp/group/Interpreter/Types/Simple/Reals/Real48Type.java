package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;

public class Real48Type extends GeneralReal {
    private final BigDecimal lowerBound = new BigDecimal("2.94e-39");
    private final BigDecimal upperBound = new BigDecimal("1.70e+38");

    public Real48Type() {
        super();
    }

    public Real48Type(BigDecimal value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigDecimal value) {
        if (value.abs().compareTo(lowerBound) < 0 || value.abs().compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for Real48Type: " + value);
        }
    }
}
