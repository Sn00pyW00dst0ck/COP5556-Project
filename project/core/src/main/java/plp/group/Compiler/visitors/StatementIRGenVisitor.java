package plp.group.Compiler.visitors;

import plp.group.AST.AST;
import plp.group.AST.ASTBaseVisitor;
import plp.group.Compiler.CompilerContext;

/**
 * Generates the IR for all statements / expressions.
 * 
 * Use for generating all function bodies.
 */
public class StatementIRGenVisitor extends ASTBaseVisitor<Object> {
    private final CompilerContext context;

    public StatementIRGenVisitor(CompilerContext context) {
        this.context = context;
    }

    //#region Statements

    @Override
    public Object visitStatementAssignment(AST.Statement.Assignment stmt) {
        // visit the expression value, put IR for all expression value eval first...
        // generate a 'store' instruction into the IR...
        return null;
    }

    // TODO: OTHER TYPES OF STSTEMENTS HERE!

    //#endregion Statements

    //#region Expressions

    @Override
    public Object visitExpressionBinary(AST.Expression.Binary expr) {
        // visit the LHS first, generate all IR for it. 
        // visit the RHS second, generate all IR for it.
        // Generate an instruction based on LHS, RHS, and operatnd.
        System.out.println(expr);
        return null;
    }

    @Override
    public Object visitExpressionUnary(AST.Expression.Unary expr) {
        // Visit the value first, then generate an instruction based on value and operand.
        System.out.println(expr);
        return null;
    }

    @Override
    public Object visitExpressionGroup(AST.Expression.Group expr) {
        // Visit the value inside.
        this.visit(expr.expression());
        return null;
    }

    @Override
    public Object visitExpressionVariable(AST.Expression.Variable expr) {
        // COMPLICATED AS HELL!
        // For something with no postFix, do a simple lookup in scope (aka a load I think)...
        // For something with a postFix, need to generate more complicated IR...
        System.out.println(expr);
        return null;
    }

    //#endregion Expressions

    //#region Literals

    //#endregion Literals

}
