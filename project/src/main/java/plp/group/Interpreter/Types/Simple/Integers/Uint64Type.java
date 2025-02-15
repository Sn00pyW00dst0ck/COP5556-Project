package plp.group.Interpreter.Types.Simple.Integers;

import java.math.BigInteger;

public class Uint64Type extends GeneralInteger {
    private final BigInteger lowerBound = new BigInteger("0");
    private final BigInteger upperBound = new BigInteger("18446744073709551615");

    public Uint64Type() {
        super();
    }

    public Uint64Type(BigInteger value) {
        super(value);
        checkOverflow(value);
    }

    protected void checkOverflow(BigInteger value) {
        if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
            throw new ArithmeticException("Overflow for IntegerType: " + value);
        }
    }
}
