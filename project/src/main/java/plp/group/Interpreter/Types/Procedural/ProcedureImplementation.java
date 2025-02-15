package plp.group.Interpreter.Types.Procedural;

@FunctionalInterface
public interface ProcedureImplementation {
    void execute(Object... args);
}