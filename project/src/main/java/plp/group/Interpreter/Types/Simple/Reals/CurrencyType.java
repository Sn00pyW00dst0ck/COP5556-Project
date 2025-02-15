package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;

public class CurrencyType extends GeneralReal {
    private final BigDecimal lowerBound = new BigDecimal("-922337203685477.5808");
    private final BigDecimal upperBound = new BigDecimal("922337203685477.5807");

    public CurrencyType(BigDecimal value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigDecimal value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for CurrencyType: " + value);
        }
    }
}
