package plp.group.Compiler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import plp.group.AST.AST;
import plp.group.AST.AST.Type.Method.ParameterGroup.GroupType;
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
    private int labelCounter = 1; // Labels have to start at 1 I guess...
    private int stringCounter = 0;

    public String getNextTmp() { return "%tmp" + (tempCounter++); }
    public String getNextLabel() { return "label_internal_" + (labelCounter++); }
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
        this.registerBuiltInFunctions();
        this.symbolTable = this.symbolTable.pushScope();

        // Collect all the strings, add all their declarations to the IR.
        (new StringCollectionVisitor(this)).visit(source);
        Map<String, LLVMValue> strings = this.symbolTable.getEntriesOfType(LLVMValue.String.class, false);
        for (var string : strings.entrySet().stream().sorted((Map.Entry<String,LLVMValue> a, Map.Entry<String,LLVMValue> b) -> {
            return ((LLVMValue.String) a.getValue()).name().compareTo(((LLVMValue.String) b.getValue()).name());
        }).toList()) {
            ir.appendStringConstant(((LLVMValue.String) string.getValue()).getGlobalDefinition());
        }

        // TODO: GET ALL THE TYPE DEFS

        // Collect all the user defined functions, add all their declarations to the IR. 
        (new FunctionCollectionVisitor(this)).visit(source);
        Map<String, LLVMValue> functions = this.symbolTable.getEntriesOfType(LLVMValue.LLVMFunction.UserFunction.class, false);
        for (var f : functions.values()) {
            var function = (LLVMValue.LLVMFunction.UserFunction) f;
            if (function.body().isEmpty()) {
                ir.appendDeclaration(function.getDeclare());
            }
        }

        // Write all the function definitions to the IR.
        for (var f : functions.entrySet()) {
            var function = (LLVMValue.LLVMFunction.UserFunction) f.getValue();
            function.body().ifPresent((body) -> {
                ir.functionDefs.append(function.getDefineHeader() + "\n");
                this.symbolTable = this.symbolTable.pushScope();
                for (int i = 0; i < function.paramNames().size(); i++) {
                    this.symbolTable.define(function.paramNames().get(i), new LLVMValue.Register("%" + function.paramNames().get(i), function.paramTypes().get(i)));
                }

                if (!function.returnType().equals("void")) {
                    this.ir.functionDefs.append("%result = alloca " + function.returnType() + "\n");
                    this.symbolTable.define("result", new LLVMValue.Pointer("%result", new LLVMValue.Register("%result", function.returnType())));
                }

                (new StatementIRGenVisitor(this, ir.functionDefs)).visit(body);
                if (function.returnType().equals("void")) {
                    ir.functionDefs.append("ret void\n");
                } else {
                    String tmp = this.getNextTmp();
                    ir.functionDefs.append(tmp + " = load " + function.returnType() + ", ptr %result\n");
                    ir.functionDefs.append("ret " + function.returnType() + " " + tmp + "\n");
                }
                this.symbolTable = this.symbolTable.popScope().get();
                ir.functionDefs.append("}\n");
            });
        }

        // Write the main function
        this.symbolTable = this.symbolTable.pushScope();
        (new StatementIRGenVisitor(this, ir.mainFunction)).visit(source.block());
        this.symbolTable = this.symbolTable.popScope().get();

        return ir.build();
    }

    //#region Helpers

    /**
     * Registers all built in functions to the symbol table.
     */
    private void registerBuiltInFunctions() {
        // Define all symbols in the symbol table.
        this.symbolTable.define("write", new LLVMValue.LLVMFunction.WriteFunction());
        this.symbolTable.define("writeln", new LLVMValue.LLVMFunction.WritelnFunction());
        this.symbolTable.define("arctan", new LLVMValue.LLVMFunction.ArctanFunction());
        this.symbolTable.define("cos", new LLVMValue.LLVMFunction.CosFunction());
        this.symbolTable.define("exp", new LLVMValue.LLVMFunction.ExpFunction());
        this.symbolTable.define("ln", new LLVMValue.LLVMFunction.LogFunction());
        this.symbolTable.define("sin", new LLVMValue.LLVMFunction.SinFunction());
        this.symbolTable.define("sqrt", new LLVMValue.LLVMFunction.SqrtFunction());
        this.symbolTable.define("trunc", new LLVMValue.LLVMFunction.TruncFunction());
        this.symbolTable.define("odd", new LLVMValue.LLVMFunction.OddFunction());
        this.symbolTable.define("round", new LLVMValue.LLVMFunction.RoundFunction());

        // Dependencies of built in functions must be declared...
        this.ir.appendDeclaration("declare i32 @printf(ptr noundef, ...)");
        this.ir.appendDeclaration("declare double @atan(double)");
        this.ir.appendDeclaration("declare double @cos(double)");
        this.ir.appendDeclaration("declare double @exp(double)");
        this.ir.appendDeclaration("declare double @log(double)");
        this.ir.appendDeclaration("declare double @sin(double)");
        this.ir.appendDeclaration("declare double @sqrt(double)");
        this.ir.appendDeclaration("declare double @trunc(double)");
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
