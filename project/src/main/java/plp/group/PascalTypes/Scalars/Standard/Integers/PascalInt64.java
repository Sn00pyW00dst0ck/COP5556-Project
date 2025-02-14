package plp.group.PascalTypes.Scalars.Standard.Integers;

import java.math.BigInteger;

public class PascalInt64 extends PascalInteger {

    public PascalInt64() {
        super();
    }

    public PascalInt64(BigInteger value) {
        super(checkOverflow(value, new BigInteger("-2147483648"), new BigInteger("2147483647")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for ShortInt: " + value);
        }
        return value;
    }

    @Override
    public BigInteger getMinValue() {
        return new BigInteger("-2147483648");
    }

    @Override
    public BigInteger getMaxValue() {
        return new BigInteger("2147483647");
    }
}
