package plp.group.Interpreter;

/**
 * The information we have about a symbol in the table.
 * 
 * A symbol is an identifier that represents a value/variable or a function.
 */
public class SymbolInfo {
    public String name = null; // The name of the variable/function
    public Object value = null; // Holds actual value for variables
    public String accessModifier = null; // public/private

    /**
     * Create a SymbolInfo for a variable with a value.
     * 
     * @param name  The name of the symbol.
     * @param value The value of the symbol.
     */
    public SymbolInfo(String name, Object value, String accessModifier) {
        if (name.equals("Self")) {
            throw new RuntimeException("Error: `Self` cannot be directly reassigned.");
        }
        this.name = name;
        this.value = value;
        this.accessModifier = accessModifier;
    }

    /**
     * Update the value of this symbol.
     * Prevents modification of `Self`.
     *
     * @param newValue The new value to assign.
     */
    public void setValue(Object newValue) {
        if (this.name.equals("Self")) {
            throw new RuntimeException("Error: `Self` cannot be reassigned.");
        }
        this.value = newValue;
    }

    /**
     * Returns a string representation of the symbol.
     */
    @Override
    public String toString() {
        return "SymbolInfo{name='" + name + "', value=" + value + "}";
    }
};
