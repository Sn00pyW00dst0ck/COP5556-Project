package plp.group.Interpreter;

/**
 * GoToException is used to help escape the ANTLR4 depth first visiting...
 */
public class GoToException extends RuntimeException {

    public final String label;

    public GoToException(String label) {
        this.label = label;
    }

}
