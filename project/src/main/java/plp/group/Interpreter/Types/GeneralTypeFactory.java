package plp.group.Interpreter.Types;

import java.util.HashSet;

import plp.group.Interpreter.Types.Procedural.FunctionImplementation;
import plp.group.Interpreter.Types.Procedural.FunctionType;
import plp.group.Interpreter.Types.Procedural.ProcedureImplementation;
import plp.group.Interpreter.Types.Procedural.ProcedureType;
import plp.group.Interpreter.Types.Simple.BooleanType;
import plp.group.Interpreter.Types.Simple.CharType;
import plp.group.Interpreter.Types.Simple.StringType;
import plp.group.Interpreter.Types.Structured.SetType;

public class GeneralTypeFactory {

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
