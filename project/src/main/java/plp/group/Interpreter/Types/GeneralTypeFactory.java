package plp.group.Interpreter.Types;

import java.math.BigInteger;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import plp.group.Interpreter.Types.Procedural.*;
import plp.group.Interpreter.Types.Simple.*;
import plp.group.Interpreter.Types.Simple.Integers.*;
import plp.group.Interpreter.Types.Simple.Reals.*;
import plp.group.Interpreter.Types.Structured.*;

/**
 * Provides an easy to use interface for constructing any value.
 */
public class GeneralTypeFactory {

    // #region Arbitrary Type Creation

    private static HashMap<String, Class<? extends GeneralType>> knownTypes = new HashMap<>();
    static {
        knownTypes.put("procedure", ProcedureType.class);
        knownTypes.put("function", FunctionType.class);

        knownTypes.put("shortint", ShortintType.class);
        knownTypes.put("byte", ByteType.class);
        knownTypes.put("smallint", SmallintType.class);
        knownTypes.put("word", WordType.class);
        knownTypes.put("fixedint", FixedintType.class);
        knownTypes.put("integer", IntegerType.class);
        knownTypes.put("fixeduint", FixeduintType.class);
        knownTypes.put("cardinal", CardinalType.class);
        knownTypes.put("int64", Int64Type.class);
        knownTypes.put("uint64", Uint64Type.class);

        knownTypes.put("real", RealType.class);
        knownTypes.put("real48", Real48Type.class);
        knownTypes.put("double", DoubleType.class);
        knownTypes.put("single", SingleType.class);
        knownTypes.put("Comp", CompType.class);
        knownTypes.put("Currency", CurrencyType.class);

        knownTypes.put("bool", BooleanType.class);
        knownTypes.put("char", CharType.class);
        knownTypes.put("string", StringType.class);
        knownTypes.put("enum", EnumType.class);
        knownTypes.put("subrange", SubrangeType.class);

        knownTypes.put("set", SetType.class);
        knownTypes.put("array", ArrayType.class);
        knownTypes.put("file", FileType.class);
        knownTypes.put("record", RecordType.class);
    }

    public static void registerType(String name, Class<? extends GeneralType> type) {
        knownTypes.put(name, type);
    }

    public static Class<? extends GeneralType> getType(String name) {
        return knownTypes.get(name);
    }

    /**
     * An easy way to make a type given a string name and args for the constructor.
     * Throws error if failed to make new type for any reason.
     * 
     * @param name
     * @param args
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static GeneralType constructType(String name, Object... args)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass(); // Extract runtime class of each argument
        }

        return knownTypes.get(name.toLowerCase()).getDeclaredConstructor(argTypes).newInstance(args);
    }

    // #endregion Arbitrary Type Creation

    // #region Simple

    public static GeneralType createInteger(BigInteger value) {
        // Create the smallest size valid integer type...
        for (Class<? extends Object> type : List.of(
                ShortintType.class,
                ByteType.class,
                SmallintType.class,
                WordType.class,
                FixedintType.class,
                Integer.class,
                FixeduintType.class,
                CardinalType.class,
                Int64Type.class,
                Uint64Type.class)) {
            // Try constructing the type, if fail move to next. If success return it.
            try {
                return (GeneralType) type.getDeclaredConstructor(BigInteger.class).newInstance(value);
            } catch (Exception e) {
                continue;
            }
        }

        throw new ArithmeticException("Value '" + value.toString(0) + "' out of range for any integer type.");
    }

    public static GeneralType createReal(BigDecimal value) {
        // Create the smallest range size real that fits the value.
        for (Class<? extends Object> type : List.of(
                SingleType.class,
                Real48Type.class,
                RealType.class,
                DoubleType.class,
                CompType.class,
                CurrencyType.class)) {
            // Try constructing the type, if fail move to next. If success return it.
            try {
                return (GeneralType) type.getDeclaredConstructor(BigDecimal.class).newInstance(value);
            } catch (Exception e) {
                continue;
            }
        }

        throw new ArithmeticException("Value '" + value.toString() + "' out of range for any real type.");
    }

    public static GeneralType createBoolean(Boolean value) {
        return new BooleanType(value);
    }

    public static GeneralType createChar(Character value) {
        return new CharType(value);
    }

    public static GeneralType createString(String value) {
        return new StringType(value);
    }

    public static GeneralType createEnum(Map<String, Integer> enumValues, String value) {
        return new EnumType(enumValues, value);
    }

    public static <T extends Comparable<T>> GeneralType createSubrange(T lowerBound, T upperBound, T value) {
        return new SubrangeType<T>(lowerBound, upperBound, value);
    }

    // #endregion Simple

    // #region Structured

    public static <T> GeneralType createEmptySet(Class<T> value) {
        return new SetType<T>(value);
    }

    public static <T> GeneralType createSet(HashSet<T> value) {
        return new SetType<T>(value);
    }

    // #endregion Structured

    // #region Procedural

    public static GeneralType createProcedure(ProcedureImplementation value) {
        return new ProcedureType(value);
    }

    public static GeneralType createFunction(FunctionImplementation value) {
        return new FunctionType(value);
    }

    // #endregion Procedural

}
