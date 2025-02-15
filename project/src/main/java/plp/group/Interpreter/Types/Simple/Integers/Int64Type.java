package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigInteger;

public class Int64Type extends GeneralInteger {
    private final BigInteger lowerBound = new BigInteger("-9223372036854775808");
    private final BigInteger upperBound = new BigInteger("9223372036854775807");

    public Int64Type(BigInteger value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigInteger value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for IntegerType: " + value);
        }
    }
}
