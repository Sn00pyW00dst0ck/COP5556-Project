package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigInteger;

public class ShortintType extends GeneralInteger {
    private final BigInteger lowerBound = new BigInteger("-127");
    private final BigInteger upperBound = new BigInteger("128");

    public ShortintType(BigInteger value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigInteger value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for IntegerType: " + value);
        }
    }
}
