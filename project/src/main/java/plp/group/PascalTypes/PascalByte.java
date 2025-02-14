package plp.group.PascalTypes;

import java.math.BigInteger;

public class PascalByte extends PascalInteger {
    public PascalByte(BigInteger value) {
        super(checkOverflow(value, new BigInteger("0"), new BigInteger("255")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for ShortInt: " + value);
        }
        return value;
    }
}
