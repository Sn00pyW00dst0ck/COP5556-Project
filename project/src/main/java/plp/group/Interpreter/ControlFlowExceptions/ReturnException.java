package plp.group.Interpreter.ControlFlowExceptions;

/**
 * An exception which signifies a Return (Exit) statement.
 */
public class ReturnException extends ControlFlowException {
    public ReturnException() {
        super("Encountered Return Statement");
    }
}