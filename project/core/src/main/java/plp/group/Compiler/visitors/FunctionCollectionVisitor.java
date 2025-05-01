package plp.group.Compiler.visitors;

import java.util.ArrayList;

import plp.group.AST.AST;
import plp.group.AST.ASTBaseVisitor;
import plp.group.Compiler.CompilerContext;
import plp.group.Compiler.LLVMValue;

/**
 * Walks the AST to get all function definitions so that we can forward declare them...
 */
public class FunctionCollectionVisitor extends ASTBaseVisitor<Void> {
    private final CompilerContext context;

    public FunctionCollectionVisitor(CompilerContext context) {
        this.context = context;
    }

    @Override
    public Void visitDeclarationCallable(AST.Declaration.Callable dec) {
        // Grab result type
        String llvmReturnType = "void";
        if (dec.method().resultType().isPresent()) {
            llvmReturnType = this.context.getLLVMType(dec.method().resultType().get());
        }

        // Grab parameter types TODO: add class 'this'
        ArrayList<String> llvmParameterTypes = new ArrayList<>();
        for (var parameter : dec.method().parameters()) {
            // TODO: handle by value vs by reference / etc....
            for (var _ : parameter.parameters()) {
                llvmParameterTypes.add(this.context.getLLVMType(parameter.parameterType()));
            }
        }

        // Define in context
        this.context.symbolTable.define(
            dec.name(),
            new LLVMValue.LLVMFunction.UserFunction(
                dec.name(),
                llvmReturnType,
                llvmParameterTypes,
                dec.body()
            )
        );
        return null;
    }
}
