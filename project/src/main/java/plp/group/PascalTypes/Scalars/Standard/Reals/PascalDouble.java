package plp.group.PascalTypes.Scalars.Standard.Reals;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PascalDouble extends PascalReal {
    private DecimalFormat doublePrecision = new DecimalFormat(" 0.0000000000000000E000;-0.0000000000000000E000");

    public PascalDouble() {
        super();
    }

    public PascalDouble(BigDecimal value) {
        super(checkOverflow(value, new BigDecimal("5.0E-324"), new BigDecimal("1.7E308")));
    }

    private static BigDecimal checkOverflow(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for PascalSingle: " + value);
        }
        return value;
    }

    @Override
    public String toString() {
        var result = doublePrecision.format(value);
        if (!result.contains("E-")) {
            result = result.replace("E", "E+");
        }
        return result;
    }

    @Override
    public BigDecimal getMinValue() {
        return new BigDecimal("5.0E-324");
    }

    @Override
    public BigDecimal getMaxValue() {
        return new BigDecimal("1.7E308");
    }
}
