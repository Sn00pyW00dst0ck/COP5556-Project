package plp.group.Interpreter;

/**
 * ControlFlowException are RuntimeExceptions used to control the flow of the interpreter. 
 * IE: things like continue, break, return, etc which cause non-linear execution. 
 */
public class ControlFlowException extends RuntimeException {
    
    /**
     * An exception which signifies a Return (Exit) statement.
     */
    public class Return extends ControlFlowException {
        final RuntimeValue value;

        Return(RuntimeValue value) {
            this.value = value;
        }
    }

}
