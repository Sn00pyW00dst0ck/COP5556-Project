package plp.group.Interpreter;

import java.util.List;
import java.util.function.Function;

/**
 * The information we have about a symbol in the table.
 * 
 * A symbol is an identifier that represents a value/variable or a function.
 */
public class SymbolInfo {
    public enum SymbolType {
        VARIABLE, FUNCTION
    };

    public SymbolType symbolType;
    public Object value = null; // Holds actual value for variables
    public Function<List<Object>, Object> function = null; // For functions
    public List<Class<?>> parameterTypes = null; // For functions
    public Class<?> returnType = null; // For functions and variables, the type of the value (return or variable
                                       // value).

    /**
     * Create a SymbolInfo for a variable with a value.
     * 
     * @param _value the value of the variable.
     */
    public SymbolInfo(Object _value) {
        symbolType = SymbolType.VARIABLE;
        value = _value;
    }

    /**
     * Create a SymbolInfo for a variable with a type but not a value.
     * 
     * @param _value the value of the variable.
     */
    public SymbolInfo(Object _value, Class<?> _type) {
        symbolType = SymbolType.VARIABLE;
        value = _value;
        returnType = _type;
    }

    /**
     * Create a SymbolInfo for a function.
     * 
     * @param _function       the code of the function to execute when it is called.
     * @param _parameterTypes the paramter types for the arguments
     * @param _returnType     the return type for the function
     */
    public SymbolInfo(Function<List<Object>, Object> _function, List<Class<?>> _parameterTypes, Class<?> _returnType) {
        symbolType = SymbolType.FUNCTION;
        function = _function;
        parameterTypes = _parameterTypes;
        returnType = _returnType;
    }
};
