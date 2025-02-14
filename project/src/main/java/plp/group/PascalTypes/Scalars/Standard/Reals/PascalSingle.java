package plp.group.PascalTypes.Scalars.Standard.Reals;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PascalSingle extends PascalReal {
    private DecimalFormat singlePrecision = new DecimalFormat(" 0.000000000E00;-0.000000000E00");

    public PascalSingle() {
        super();
    }

    public PascalSingle(BigDecimal value) {
        super(checkOverflow(value));
    }

    private static BigDecimal checkOverflow(BigDecimal value) {
        if (value.abs().compareTo(new BigDecimal("1.5E-45")) < 0
                || value.abs().compareTo(new BigDecimal("3.4E38")) > 0) {
            throw new ArithmeticException("Overflow for PascalSingle: " + value);
        }
        return value;
    }

    @Override
    public String toString() {
        var result = singlePrecision.format(value);
        if (!result.contains("E-")) {
            result = result.replace("E", "E+");
        }
        return result;
    }

    @Override
    public BigDecimal getMinValue() {
        return new BigDecimal("1.5E-45");
    }

    @Override
    public BigDecimal getMaxValue() {
        return new BigDecimal("3.4E38");
    }
}
