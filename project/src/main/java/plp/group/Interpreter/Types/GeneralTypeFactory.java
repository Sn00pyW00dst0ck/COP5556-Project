package plp.group.Interpreter.Types;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import com.sun.jdi.ByteType;
import com.sun.jdi.DoubleType;

import plp.group.Interpreter.Types.Procedural.FunctionImplementation;
import plp.group.Interpreter.Types.Procedural.FunctionType;
import plp.group.Interpreter.Types.Procedural.ProcedureImplementation;
import plp.group.Interpreter.Types.Procedural.ProcedureType;
import plp.group.Interpreter.Types.Simple.BooleanType;
import plp.group.Interpreter.Types.Simple.CharType;
import plp.group.Interpreter.Types.Simple.StringType;
import plp.group.Interpreter.Types.Simple.Integers.Cardinal;
import plp.group.Interpreter.Types.Simple.Integers.Fixedint;
import plp.group.Interpreter.Types.Simple.Integers.Fixeduint;
import plp.group.Interpreter.Types.Simple.Integers.Int64Type;
import plp.group.Interpreter.Types.Simple.Integers.ShortintType;
import plp.group.Interpreter.Types.Simple.Integers.SmallintType;
import plp.group.Interpreter.Types.Simple.Integers.Uint64Type;
import plp.group.Interpreter.Types.Simple.Integers.WordType;
import plp.group.Interpreter.Types.Simple.Reals.CompType;
import plp.group.Interpreter.Types.Simple.Reals.CurrencyType;
import plp.group.Interpreter.Types.Simple.Reals.Real48Type;
import plp.group.Interpreter.Types.Simple.Reals.RealType;
import plp.group.Interpreter.Types.Simple.Reals.SingleType;
import plp.group.Interpreter.Types.Structured.SetType;

public class GeneralTypeFactory {

    public static GeneralType createInteger(BigInteger value) {
        // Create the smallest size valid integer type...
        for (Class<? extends Object> type : List.of(
                ShortintType.class,
                ByteType.class,
                SmallintType.class,
                WordType.class,
                Fixedint.class,
                Integer.class,
                Fixeduint.class,
                Cardinal.class,
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

    //

    public static <T> GeneralType createEmptySet(Class<T> value) {
        return new SetType<T>(value);
    }

    public static <T> GeneralType createSet(HashSet<T> value) {
        return new SetType<T>(value);
    }

    public static GeneralType createProcedure(ProcedureImplementation value) {
        return new ProcedureType(value);
    }

    public static GeneralType createFunction(FunctionImplementation value) {
        return new FunctionType(value);
    }

}
