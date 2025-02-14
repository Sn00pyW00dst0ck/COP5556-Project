package plp.group.PascalTypes.Scalars.Standard.Reals;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import plp.group.PascalTypes.PascalType;

public abstract class PascalReal extends PascalType {
    protected BigDecimal value;

    public PascalReal() {
        this.isInitialized = false;
    }

    public PascalReal(BigDecimal value) {
        this.value = value;
        this.isInitialized = true;
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    public abstract BigDecimal getMinValue();

    public abstract BigDecimal getMaxValue();

    /**
     * If given a BigDecimal, will return a PascalReal with the smallest range.
     * So if the value is in range of PascalSingle that type is created.
     * 
     * @param value the value to instantiate.
     * @return the created minimum range Real, as a PascalReal.
     */
    public static PascalReal createBestFit(BigDecimal value) {
        for (Class<? extends PascalReal> type : List.of(PascalSingle.class, PascalDouble.class)) {
            try {
                return type.getDeclaredConstructor(BigDecimal.class).newInstance(value);
            } catch (InvocationTargetException e) {
                continue;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate " + type.getSimpleName(), e);
            }
        }
        throw new RuntimeException("No suitable Real type found for value: " + value);
    }
}
