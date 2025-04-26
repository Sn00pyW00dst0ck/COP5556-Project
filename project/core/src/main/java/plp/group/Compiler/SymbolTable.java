package plp.group.Compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A symbol table, analagous to a scope, but usable in more contexts...
 */
public class SymbolTable {
    private final Optional<SymbolTable> parent;
    private final HashMap<String, LLVMValue> symbols = new HashMap<String, LLVMValue>();

    public SymbolTable(Optional<SymbolTable> parent) {
        this.parent = parent;
    }

    /**
     * Define a new symbol to the symbol table.
     * 
     * @param name the name to define
     * @param value the value to define the name as
     */
    public void define(String name, LLVMValue value) {
        symbols.put(name, value);
    }

    /**
     * Look up a LLVMValue 'symbol' by name within the symbol table.
     * 
     * @param name the name to look up.
     * @param current set true if looking in only the current scope!
     * @return the LLVMValue within an optional.
     */
    public Optional<LLVMValue> lookup(String name, boolean current) {
        if (symbols.containsKey(name)) {
            return Optional.of(symbols.get(name));
        }
        if (parent.isPresent() && !current) {
            return parent.get().lookup(name, false);
        }
        return Optional.empty();
    }

    /**
     * Get all entries of a specific LLVMValue type...
     * 
     * @param typeClass the class to get the type of (ex: LLVMValue.String.class)
     * @param current set true to use only the current scope, set false to include parent scope.
     * @return all entries in the table of given typeClass
     */
    public Map<String, LLVMValue> getEntriesOfType(Class<? extends LLVMValue> typeClass, boolean current) {
        Map<String, LLVMValue> result = symbols.entrySet().stream()
            .filter(e -> typeClass.isInstance(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!current) {
            parent.ifPresent(p -> result.putAll(p.getEntriesOfType(typeClass, false)));
        }
        return result;
    }

    /**
     * Get a new scope (an empty symbol table whose parent is set to the instance you called pushScope on).
     * 
     * @return the new scope (symbol table)
     */
    public SymbolTable pushScope() {
        return new SymbolTable(Optional.of(this));
    }

    /**
     * Return to the parent scope by getting its reference.
     * 
     * @return the parent scope (symbol table)
     */
    public Optional<SymbolTable> popScope() {
        return parent;
    }
}
