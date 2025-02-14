package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigInteger;

import plp.group.PascalTypes.PascalType;

public class PascalChar extends PascalType {
    private BigInteger value;

    public PascalChar() {
        this.isInitialized = false;
    }

    public PascalChar(BigInteger value) {
        this.value = value;
        this.isInitialized = true;
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf((char) value.intValue());
    }
}
