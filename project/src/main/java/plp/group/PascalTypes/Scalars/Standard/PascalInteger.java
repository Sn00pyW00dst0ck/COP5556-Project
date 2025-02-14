package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigInteger;

import plp.group.PascalTypes.PascalType;

public class PascalInteger extends PascalType {
    private BigInteger value;

    public PascalInteger() {
        this.isInitialized = false;
    }

    public PascalInteger(BigInteger value) {
        this.value = value;
        this.isInitialized = true;
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
