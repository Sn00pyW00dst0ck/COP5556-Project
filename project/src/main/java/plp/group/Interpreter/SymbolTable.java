package plp.group.Interpreter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

/**
 * Represents a 'Scope' which has a parent Scope and a mapping of identifiers to
 * values.
 */
public class SymbolTable {

    private Deque<HashMap<String, SymbolInfo>> scopes = new ArrayDeque<HashMap<String, SymbolInfo>>();

    /**
     * Insert a new symbol into the scope.
     * 
     * @param name the name of the symbol to insert.
     * @param info the info about the symbol to insert.
     */
    public void insert(String name, SymbolInfo info) {
        scopes.peek().put(name, info);
    }

    /**
     * Update a symbol within the scope. If not found in current scope, will search
     * the parent scopes to update.
     * 
     * @param name the name to update in the scope.
     * @param info the new info about the symbol to use.
     */
    public void update(String name, SymbolInfo info) {
        for (HashMap<String, SymbolInfo> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.replace(name, info);
                return;
            }
        }
    }

    /**
     * Delete a symbol from the scope. If not found in current scope, will search
     * parent scopes.
     * 
     * @param name the name to remove from the scope.
     */
    public void delete(String name) {
        for (HashMap<String, SymbolInfo> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.remove(name);
                return;
            }
        }
    }

    /**
     * Look up a symbol in the scopes. Will return the first instance of the name in
     * the table.
     * 
     * @param name the name to look up in the scopes.
     * @return
     */
    public SymbolInfo lookup(String name) {
        for (HashMap<String, SymbolInfo> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    /**
     * Tells if the name is defined in the current scope.
     * 
     * @param name the name to look up in the current scope.
     * @return true if there, false if not.
     */
    public boolean isInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }

    /**
     * Creates and enters a new (empty) scope.
     */
    public void enterScope() {
        scopes.push(new HashMap<String, SymbolInfo>());
    }

    /**
     * Exits (and deletes) the current scope, returning to the parent scope.
     */
    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }
}
