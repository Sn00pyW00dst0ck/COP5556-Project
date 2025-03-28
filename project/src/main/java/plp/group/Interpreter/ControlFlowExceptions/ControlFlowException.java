package plp.group.Interpreter.ControlFlowExceptions;

/**
 * ControlFlowException are RuntimeExceptions used to control the flow of the interpreter. 
 * IE: things like continue, break, return, etc which cause non-linear execution. 
 */
public class ControlFlowException extends RuntimeException {
    public ControlFlowException(String message) {
        super(message);
    }
}

