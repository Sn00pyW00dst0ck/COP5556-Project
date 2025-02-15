package plp.group.Interpreter.Types.Simple.Reals;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class SingleType extends GeneralReal {
    private DecimalFormat singlePrecision = new DecimalFormat(" 0.000000000E00;-0.000000000E00");

    private final BigDecimal lowerBound = new BigDecimal("1.18e-38");
    private final BigDecimal upperBound = new BigDecimal("3.40e+38");

    public SingleType() {
        super();
    }

    public SingleType(BigDecimal value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigDecimal value) {
        if (value.abs().compareTo(lowerBound) < 0 || value.abs().compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for SingleType: " + value);
        }
    }

    @Override
    public String toString() {
        var result = singlePrecision.format(((BigDecimal) this.getValue()));
        if (!result.contains("E-")) {
            result = result.replace("E", "E+");
        }
        return result;
    }
}
