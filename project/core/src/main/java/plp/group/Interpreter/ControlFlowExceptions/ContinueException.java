package plp.group.Interpreter.ControlFlowExceptions;

public class ContinueException extends ControlFlowException {
    public ContinueException() {
        super("Encountered Continue Statement");
    }
}
