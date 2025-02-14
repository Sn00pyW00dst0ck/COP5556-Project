package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigInteger;

import plp.group.PascalTypes.PascalType;

public class PascalInteger extends PascalType {
    private final BigInteger value;

    public PascalInteger(BigInteger value) {
        this.value = value;
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
