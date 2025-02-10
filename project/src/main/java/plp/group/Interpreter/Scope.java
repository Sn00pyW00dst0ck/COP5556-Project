package plp.group.Interpreter;

import java.util.HashMap;
import java.util.Optional;

/**
 * Represents a 'Scope' which has a parent Scope and a mapping of identifiers to
 * values.
 */
public class Scope {
    private HashMap<String, Object> values = new HashMap<>();
    private Optional<Scope> parent = Optional.empty();

    /**
     * Create a new scope with specified parent (if there is no parent scope
     * required set to null).
     * 
     * @param _parent The parent scope to use, or null if there is no parent scope.
     */
    public Scope(Scope _parent) {
        if (_parent != null) {
            parent = Optional.of(_parent);
        }
    }

    /**
     * Add a new thing into the scope.
     * 
     * @param name  the name of the thing to add.
     * @param value the value of the thing to add.
     */
    public void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Get a thing from the scope. If it doesn't exist in this scope, we check all
     * parent scopes.
     * If not found, throws a RuntimeException.
     * 
     * @param name the name of the thing to get.
     * @return the value of the thing, or a RuntimeException if not found.
     */
    public Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent.isPresent()) {
            return parent.get().get(name);
        }
        throw new RuntimeException("'" + name + "' was not found in scope!");
    }

    /**
     * Assign a new value to a thing in the scope. If it doesn't exist in this
     * scope, attempt to update the value in parent scopes.
     * If not found, throws a RuntimeException.
     * 
     * @param name  the name of the thing to assign to.
     * @param value the new value to assign to the thing.
     */
    public void assign(String name, Object value) {
        if (values.replace(name, value) != null) {
            return;
        }
        if (parent.isPresent()) {
            parent.get().assign(name, value);
            return;
        }
        throw new RuntimeException("'" + name + "' was not found in scope!");
    }
}
