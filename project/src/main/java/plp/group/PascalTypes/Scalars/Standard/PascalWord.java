package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigInteger;

public class PascalWord extends PascalInteger {
    public PascalWord(BigInteger value) {
        super(checkOverflow(value, new BigInteger("0"), new BigInteger("65535")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for Word: " + value);
        }
        return value;
    }
}
