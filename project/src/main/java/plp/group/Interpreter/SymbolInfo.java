package plp.group.Interpreter;

import plp.group.Interpreter.Types.GeneralType;

/**
 * The information we have about a symbol in the table.
 * 
 * A symbol is an identifier that represents a value/variable or a function.
 */
public class SymbolInfo {
    public String name = null; // The name of the variable/function.
    public GeneralType value = null; // Holds actual value for variables

    /**
     * Create a SymbolInfo for a variable with a value.
     * 
     * @param _value the value of the variable.
     */
    public SymbolInfo(String name, GeneralType value) {
        this.name = name;
        this.value = value;
    }
};
