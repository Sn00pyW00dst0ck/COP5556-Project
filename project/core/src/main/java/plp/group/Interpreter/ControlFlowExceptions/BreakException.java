package plp.group.Interpreter.ControlFlowExceptions;

public class BreakException extends ControlFlowException {
    public BreakException() {
        super("Encountered Break Statement");
    }
}
