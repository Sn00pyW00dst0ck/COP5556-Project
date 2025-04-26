package plp.group.Compiler;

import java.util.Optional;

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
    private int stringCounter = 0;

    public String getNextTmp() { return "%tmp" + (tempCounter++); }
    public String getNextLabel() { return "" + (labelCounter++); }
    public String getNextString() { return "@llvm.str." + (stringCounter++); }
}


