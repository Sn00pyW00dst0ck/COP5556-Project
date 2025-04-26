package plp.group.Compiler;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * An object that should be passed around the compiler passes to serve as a collection of all the compiler state...
 */
public class CompilerContext {
    /**
     * The internal represetntation string builder where IR is written
     */
    public final StringBuilder ir = new StringBuilder();

    /**
     * The symbols in use...
     */
    public final SymbolTable symbolTable = new SymbolTable(Optional.empty());

    private int tempCounter = 0;
    private int labelCounter = 0;

    public String getNextTmp() { return "%tmp" + (tempCounter++); }
    public String getNextLabel() { return "" + (labelCounter++); }

    private final Map<String, String> globalStrings = new HashMap<>();
    private int stringCounter = 0;

    /**
     * Creates a global LLVM string for the given literal
     */
    public String createGlobalString(String value) {
        if (globalStrings.containsKey(value)) {
            return globalStrings.get(value);
        }
        String name = "@.str" + stringCounter++;
        globalStrings.put(value, name);
        return name;
    }

    /**
     * Returns all the collected global strings
     */
    public Map<String, String> getGlobalStrings() {
        return globalStrings;
    }

    /**
     * Emits LLVM IR definitions for all collected global strings
     */
    public String generateGlobalStringLLVM() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : globalStrings.entrySet()) {
            String value = entry.getKey();
            String name = entry.getValue();
            int length = value.length() + 1; // +1 for null terminator
            sb.append(name)
              .append(" = private unnamed_addr constant [")
              .append(length)
              .append(" x i8] c\"")
              .append(escapeString(value))
              .append("\\00\"\n");
        }
        return sb.toString();
    }

    /**
     * Escapes special characters for LLVM IR string constants
     */
    private String escapeString(String s) {
        return s.replace("\\", "\\5C")
                .replace("\n", "\\0A")
                .replace("\t", "\\09")
                .replace("\"", "\\22");
    }
}


