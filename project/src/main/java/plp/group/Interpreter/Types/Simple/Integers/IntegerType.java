package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigInteger;

public class IntegerType extends GeneralInteger {
    private final BigInteger lowerBound = new BigInteger("-2147483648");
    private final BigInteger upperBound = new BigInteger("2147483647");

    public IntegerType() {
        super();
    }

    public IntegerType(BigInteger value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigInteger value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for IntegerType: " + value);
        }
    }
}
