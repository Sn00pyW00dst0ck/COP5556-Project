package plp.group.Interpreter.Types.Procedural;

@FunctionalInterface
public interface FunctionImplementation {
    Object execute(Object... args);
}