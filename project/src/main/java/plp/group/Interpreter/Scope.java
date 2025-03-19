package plp.group.Interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class representing a scope.
 * 
 * A scope has a collection of strings mapped to runtime values. 
 * A scope also has a parent scope, which 
 * 
 * The scope class can be used to represent a scope within the running program (like a global or function scope),
 * or it can be used to represent a scope of things available to a class.
 */
public class Scope {
    private final Map<String, RuntimeValue> symbols = new HashMap<>();
    private final Optional<Scope> parent;

    /**
     * 
     * @param parent
     */
    public Scope(Optional<Scope> parent) {
        this.parent = parent;
    }

    /**
     * Define a new symbol within the current scope.
     * 
     * @param name The name of the new symbol in the current scope.
     * @param value The value of the new symbol in the current scope.
     * 
     * @throws IllegalArgumentException when attempting to redefine a name already in the current scope.
     */
    public void define(String name, RuntimeValue value) {
        symbols.merge(name, value, (_, _) -> {
            // If we are re-defining the name, then throw an error
            throw new IllegalArgumentException("Attempting to redefine name '" + name + "' in scope!");
        });
    }

    /**
     * Assign a new value to a pre-defined variable in the current scope or any parent scope. 
     * 
     * @param name The name of the symbol to assign a new value to.
     * @param value The new value to assign to the symbol.
     * 
     * @throws IllegalArgumentException when attempting to assign a value to a name that has not already been defined in the current scope.
     */
    public void assign(String name, RuntimeValue value) {
        if (symbols.containsKey(name)) {
            symbols.put(name, value);
        } else {
            parent.map(p -> {
                p.assign(name, value); 
                return true; // Successful assignment in parent
            }).orElseThrow(() ->
                new IllegalArgumentException("Attempting to assign a value to name '" + name + "' not in scope!")
            );
        }
    }

    /**
     * Look up the value associated with a name in the current scope or any parent scope. 
     * 
     * @param name the name of the symbol to get the value of. 
     * @return the RuntimeValue associated with the name, or Optional.empty() if not found.
     */
    public Optional<RuntimeValue> lookup(String name) {
        // A funky one liner, if the symbol exists in current scope get that, or we get it from the parent scope recursively
        return Optional.ofNullable(symbols.get(name)).or(() -> parent.flatMap(p -> p.lookup(name)));
    }
}
