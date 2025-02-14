package plp.group.PascalTypes.Scalars.Standard.Integers;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.List;

import plp.group.PascalTypes.PascalType;

public abstract class PascalInteger extends PascalType {
    private BigInteger value;

    public PascalInteger() {
        this.isInitialized = false;
    }

    public PascalInteger(BigInteger value) {
        this.value = value;
        this.isInitialized = true;
    }

    public abstract BigInteger getMinValue();

    public abstract BigInteger getMaxValue();

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * If given a BigInteger, will return a PascalInteger with the smallest range.
     * So if the value is in range of PascalSingle that type is created.
     * 
     * @param value the value to instantiate.
     * @return the created minimum range Integer, as a PascalInteger.
     */
    public static PascalInteger createBestFit(BigInteger value) {
        for (Class<? extends PascalInteger> type : List.of(
                PascalByte.class,
                PascalShortint.class,
                PascalSmallint.class,
                PascalWord.class,
                PascalLongint.class,
                PascalLongword.class,
                PascalInt64.class)) {
            try {
                return type.getDeclaredConstructor(BigInteger.class).newInstance(value);
            } catch (InvocationTargetException e) {
                continue;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate " + type.getSimpleName(), e);
            }
        }
        throw new RuntimeException("No suitable Integer type found for value: " + value);
    }
}
