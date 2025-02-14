package plp.group.PascalTypes.Scalars.Standard;

import java.math.BigDecimal;

import plp.group.PascalTypes.PascalType;

// TODO: might need to extend this to handle different precisions
public class PascalReal extends PascalType {
    private final BigDecimal value;

    public PascalReal(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
