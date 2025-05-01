package plp.group.Compiler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

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
    public final IRBuilder ir = new IRBuilder();

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

    
    //#region Loop Labels 
    private final Stack<String> breakLabelStack = new Stack<>();
    private final Stack<String> continueLabelStack = new Stack<>();

    public void pushLoopLabels(String breakLabel, String continueLabel) {
        breakLabelStack.push(breakLabel);
        continueLabelStack.push(continueLabel);
    }
    
    public void popLoopLabels() {
        breakLabelStack.pop();
        continueLabelStack.pop();
    }
    
    public String getBreakLabel() {
        if (breakLabelStack.isEmpty()) {
            throw new IllegalStateException("No break label in scope.");
        }
        return breakLabelStack.peek();
    }
    
    public String getContinueLabel() {
        if (continueLabelStack.isEmpty()) {
            throw new IllegalStateException("No continue label in scope.");
        }
        return continueLabelStack.peek();
    }

    //#endregion Loop Labels

    /**
     * 
     * @param source
     * @return
     */
    public String compileToLLVMIR(AST.Program source) {
        // Collect all the strings, add all their declarations to the IR.
        (new StringCollectionVisitor(this)).visit(source);
        Map<String, LLVMValue> strings = this.symbolTable.getEntriesOfType(LLVMValue.String.class, false);
        for (var string : strings.entrySet().stream().sorted((Map.Entry<String,LLVMValue> a, Map.Entry<String,LLVMValue> b) -> {
            return ((LLVMValue.String) a.getValue()).name().compareTo(((LLVMValue.String) b.getValue()).name());
        }).toList()) {
            ir.appendStringConstant(((LLVMValue.String) string.getValue()).getGlobalDefinition());
        }

        // TODO: GET ALL THE TYPE DEFS

        this.registerBuiltInFunctions();

        // Collect all the user defined functions, add all their declarations to the IR. 
        (new FunctionCollectionVisitor(this)).visit(source);
        Map<String, LLVMValue> functions = this.symbolTable.getEntriesOfType(LLVMValue.LLVMFunction.UserFunction.class, false);
        for (var function : functions.values()) {
            ir.appendDeclaration(((LLVMValue.LLVMFunction.UserFunction) function).getDeclare());
        }

        // Write all the function definitions to the IR.
        for (var function : functions.entrySet()) {
            // TODO: handle everything in the function body...
            ((LLVMValue.LLVMFunction.UserFunction) function.getValue()).body().ifPresent((body) -> {
                ir.functionDefs.append(((LLVMValue.LLVMFunction.UserFunction) function.getValue()).getDefineHeader() + "\n");
                (new StatementIRGenVisitor(this, ir.functionDefs)).visit(body);
                ir.functionDefs.append("|\n");
            });
        }
        // TODO: Write all the built in function definitions to the IR.

        // Write the main function
        (new StatementIRGenVisitor(this, ir.mainFunction)).visit(source.block());

        return ir.build();
    }

    //#region Helpers

    /**
     * Registers all built in functions to the symbol table.
     */
    private void registerBuiltInFunctions() {
        this.symbolTable.define("write", new LLVMValue.LLVMFunction.WriteFunction());
        this.symbolTable.define("writeln", new LLVMValue.LLVMFunction.WritelnFunction());
        // TODO: others...

        // Dependencies of built in functions must be declared...
        this.ir.appendDeclaration("declare i32 @printf(ptr noundef, ...)");
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
                    case "String" -> "ptr"; // String = Array of Characters : may need to change this though...
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
