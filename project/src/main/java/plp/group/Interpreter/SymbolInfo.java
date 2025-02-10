package plp.group.Interpreter;

/**
 * The information we have about a symbol in the table.
 * Includes name, value, type.
 */
public class SymbolInfo {
    // Type t; // Todo: figure out how a type is represented.

    public Object value;

    public SymbolInfo(Object _value) {
        value = _value;
    }
};