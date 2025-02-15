package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class RealType extends GeneralReal {
    private DecimalFormat doublePrecision = new DecimalFormat(" 0.0000000000000000E000;-0.0000000000000000E000");

    private final BigDecimal lowerBound = new BigDecimal("2.23e-308");
    private final BigDecimal upperBound = new BigDecimal("1.79e+308");

    public RealType(BigDecimal value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigDecimal value) {
        if (value.abs().compareTo(lowerBound) < 0 || value.abs().compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for RealType: " + value);
        }
    }

    @Override
    public String toString() {
        var result = doublePrecision.format(((BigDecimal) this.getValue()));
        if (!result.contains("E-")) {
            result = result.replace("E", "E+");
        }
        return result;
    }
}
