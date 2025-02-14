package plp.group.PascalTypes.Scalars.Standard.Integers;

import java.math.BigInteger;

public class PascalShortint extends PascalInteger {

    public PascalShortint() {
        super();
    }

    public PascalShortint(BigInteger value) {
        super(checkOverflow(value, new BigInteger("-128"), new BigInteger("127")));
    }

    private static BigInteger checkOverflow(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Overflow for ShortInt: " + value);
        }
        return value;
    }

    @Override
    public BigInteger getMinValue() {
        return new BigInteger("-128");
    }

    @Override
    public BigInteger getMaxValue() {
        return new BigInteger("127");
    }
}
