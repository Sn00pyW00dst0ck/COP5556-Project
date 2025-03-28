package plp.group.Interpreter.ControlFlowExceptions;

/**
 * An exception which signifies a Return (Exit) statement.
 */
public class Return extends ControlFlowException {
    public Return() {
        super("Encountered Return Statement");
    }
}