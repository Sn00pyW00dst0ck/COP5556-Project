package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigInteger;

public class PascalSmallint extends PascalInteger {

    public PascalSmallint() {
        super();
    }

    public PascalSmallint(BigInteger value) {
        super(checkOverflow(value, new BigInteger("-32768"), new BigInteger("32767")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for ShortInt: " + value);
        }
        return value;
    }
}
