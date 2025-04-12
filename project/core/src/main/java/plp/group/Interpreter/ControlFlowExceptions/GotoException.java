package plp.group.Interpreter.ControlFlowExceptions;

/*
 * GotoException is a specific type of ControlFlowException used to handle 'goto' statements in the interpreter.
 * It carries a label to which the execution should jump.
 */

public class GotoException extends RuntimeException {
    public final String label;

    public GotoException(String label) {
        this.label = label;
    }
}
