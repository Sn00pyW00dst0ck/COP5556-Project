package plp.group.Interpreter;

/**
 * The information we have about a symbol in the table.
 * 
 * A symbol is an identifier that represents a value/variable or a function.
 */
public class SymbolInfo {
    public String name = null; // The name of the variable/function.
    public Object value = null; // Holds actual value for variables

    /**
     * Create a SymbolInfo for a variable with a value.
     * 
     * @param _value the value of the variable.
     */
    public SymbolInfo(String name, Object value) {
        this.name = name;
        this.value = value;
    }
};
