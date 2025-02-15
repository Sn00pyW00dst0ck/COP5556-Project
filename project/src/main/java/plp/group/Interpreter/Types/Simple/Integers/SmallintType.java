package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigInteger;

public class SmallintType extends GeneralInteger {
    private final BigInteger lowerBound = new BigInteger("-32768");
    private final BigInteger upperBound = new BigInteger("32767");

    public SmallintType() {
        super();
    }

    public SmallintType(BigInteger value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigInteger value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for IntegerType: " + value);
        }
    }
}
