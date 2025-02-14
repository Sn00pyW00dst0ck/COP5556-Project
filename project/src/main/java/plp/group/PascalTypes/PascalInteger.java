package plp.group.PascalTypes;

import java.math.BigInteger;

public class PascalInteger extends PascalType {
    private final BigInteger value;

    public PascalInteger(BigInteger value) {
        this.value = value; // You can apply overflow rules here
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
