package plp.group.PascalTypes;

import java.math.BigInteger;

public class PascalLongint extends PascalInteger {
    public PascalLongint(BigInteger value) {
        super(checkOverflow(value, new BigInteger("-2147483648"), new BigInteger("2147483647")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for ShortInt: " + value);
        }
        return value;
    }
}
