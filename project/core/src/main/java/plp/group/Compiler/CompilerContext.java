package plp.group.Compiler;

import java.util.Optional;

import plp.group.AST.AST;

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


    /**
     * Get the LLVM type name for a given AST.Type
     * 
     * @param type the AST.Type to get the LLVM type of...
     * @return
     */
    String getLLVMType(AST.Type type) {
        return switch (type) {
            // Convert named types
            case AST.Type.Simple.Named named -> 
                switch (named.name()) {
                    case "Integer" -> "i32";
                    case "Real" -> "double";
                    case "Boolean" -> "i1";
                    case "Character" -> "i8";
                    case "String" -> "i8*"; // String = Array of Characters : may need to change this though...
                    default -> {
                        // Lookup in scope? 
                        throw new RuntimeException("Unsupported Type: " + type.toString());
                    }
                };
            
            // TODO: handle other Type nodes...

            default -> throw new RuntimeException("Unsupported Type: " + type.toString());
        };
    }
}


