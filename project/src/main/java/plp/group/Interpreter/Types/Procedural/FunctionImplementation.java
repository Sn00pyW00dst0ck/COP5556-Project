package plp.group.Interpreter.Types.Procedural;

import plp.group.Interpreter.Types.GeneralType;

@FunctionalInterface
public interface FunctionImplementation {
    GeneralType execute(Object... args);
}