package plp.group.Compiler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import plp.group.AST.AST;
import plp.group.Compiler.visitors.FunctionCollectionVisitor;
import plp.group.Compiler.visitors.StatementIRGenVisitor;
import plp.group.Compiler.visitors.StringCollectionVisitor;

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
    public SymbolTable symbolTable = new SymbolTable(Optional.empty());

    private int tempCounter = 0;
    private int labelCounter = 0;
    private int stringCounter = 0;

    public String getNextTmp() { return "%tmp" + (tempCounter++); }
    public String getNextLabel() { return "" + (labelCounter++); }
    public String getNextString() { return "@llvm.str." + (stringCounter++); }


    /**
     * 
     * @param source
     * @return
     */
    public String compileToLLVMIR(AST.Program source) {
        // Collect all the strings, add all their declarations to the IR.
        (new StringCollectionVisitor(this)).visit(source);
        var strings = this.symbolTable.getEntriesOfType(LLVMValue.String.class, false);
        for (var string : strings.entrySet().stream().sorted((Map.Entry<String,LLVMValue> a, Map.Entry<String,LLVMValue> b) -> {
            return ((LLVMValue.String) a.getValue()).name().compareTo(((LLVMValue.String) b.getValue()).name());
        }).toList()) {
            ir.append(((LLVMValue.String) string.getValue()).getGlobalDefinition() + "\n");
        }
        ir.append("\n");

        // GET ALL THE TYPE DEFS

        this.registerBuiltInFunctions();

        // Collect all the functions, add all their declarations to the IR. 
        (new FunctionCollectionVisitor(this)).visit(source);
        var functions = this.symbolTable.getEntriesOfType(LLVMValue.Function.class, false);
        for (var function : functions.entrySet()) {
            ir.append(((LLVMValue.Function) function.getValue()).getDeclare() + "\n");
        }
        ir.append("\n");

        // Write all the function definitions to the IR.
        for (var function : functions.entrySet()) {
            // TODO: handle everything in the function body...
            ((LLVMValue.Function) function.getValue()).body().ifPresent((body) -> {
                ir.append(((LLVMValue.Function) function.getValue()).getDefineHeader() + "\n");
                (new StatementIRGenVisitor(this)).visit(body);
                ir.append("}\n");
            });
        }
        // TODO: Write all the built in function definitions to the IR.
        ir.append("\n");

        // Write the main function
        ir.append("define i32 @main() {\n");
        // TODO: ensure everything is handled with function body...
        (new StatementIRGenVisitor(this)).visit(source.block());
        ir.append("\tret i32 0\n");
        ir.append("}\n");

        // Anything else

        return ir.toString();
    }

    //#region Helpers

    /**
     * Registers all built in functions to the symbol table.
     */
    private void registerBuiltInFunctions() {
        this.symbolTable.define("write", new LLVMValue.Function(
            "write",
            "void",
            List.of("i8*"),
            Optional.empty()
        ));

        this.symbolTable.define("writeln", new LLVMValue.Function(
            "writeln",
            "void",
            List.of("i8*"),
            Optional.empty()
        ));

        // TODO: others...
    }

    /**
     * Add all the built in function definitions to the IR.
     */
    private void writeBuiltInFunctionDefinitions()  {

    }



    /**
     * Get the LLVM type name for a given AST.Type
     * 
     * @param type the AST.Type to get the LLVM type of...
     * @return
     */
    public String getLLVMType(AST.Type type) {
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
            
            // TODO: handle other Type nodes here...

            default -> throw new RuntimeException("Unsupported Type: " + type.toString());
        };
    }

    //#endregion Helpers
}
