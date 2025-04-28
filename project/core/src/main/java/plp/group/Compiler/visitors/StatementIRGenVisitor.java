package plp.group.Compiler.visitors;

import plp.group.AST.AST;
import plp.group.AST.ASTBaseVisitor;
import plp.group.Compiler.CompilerContext;
import plp.group.Compiler.LLVMValue;

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
        this.visit(stmt.value());
        return null;
    }

    // TODO: OTHER TYPES OF STSTEMENTS HERE!

    //#endregion Statements

    //#region Expressions - returns the 'temp' they create as a string...

    @Override
    public String visitExpressionBinary(AST.Expression.Binary expr) {
        // Visit the LHS and RHS, generating the IR to compute them and getting their 'tmp' result.
        String lhsTemp = (String) this.visit(expr.lhs());
        String rhsTemp = (String) this.visit(expr.rhs());

        // Generate an instruction based on LHS, RHS, and operand, then put result in a tmp...
        String tmp = context.getNextTmp();

        context.ir.append(tmp + " = ");
        context.ir.append(
            switch (expr.operator()) {
                // I'm assuming integers for now, TODO: figure out types...
                case "+" -> "add i32 " + lhsTemp + ", " + rhsTemp;
                case "-" -> "sub i32 " + lhsTemp + ", " + rhsTemp;
                case "*" -> "mul i32 " + lhsTemp + ", " + rhsTemp;
                case "/" -> "div i32 " + lhsTemp + ", " + rhsTemp;
                case "DIV" -> "sdiv i32 " + lhsTemp + ", " + rhsTemp;
                case "MOD" -> "srem i32 " + lhsTemp + ", " + rhsTemp;

                case "=" -> "icmp eq " + lhsTemp + ", " + rhsTemp;
                case "<>" -> "icmp ne " + lhsTemp + ", " + rhsTemp;
                case "<" -> "icmp slt " + lhsTemp + ", " + rhsTemp;
                case "<=" -> "icmp sle " + lhsTemp + ", " + rhsTemp;
                case ">" -> "icmp sgt " + lhsTemp + ", " + rhsTemp;
                case ">=" -> "icmp sge " + lhsTemp + ", " + rhsTemp;

                case "AND" -> "and i1 " + lhsTemp + ", " + rhsTemp;
                case "OR" -> "or i1 " + lhsTemp + ", " + rhsTemp ;

                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        context.ir.append("\n");

        // Return the tmp so that other expressions can use it...
        return tmp;
    }

    @Override
    public String visitExpressionUnary(AST.Expression.Unary expr) {
        // Visit the value first, then generate an instruction based on value and operand.
        String exprTemp = (String) this.visit(expr.expression());

        // Generate an instruction based on LHS, RHS, and operand, then put result in a tmp...
        String tmp = context.getNextTmp();

        context.ir.append(tmp + " = ");
        context.ir.append(
            switch (expr.operator()) {
                // I'm assuming integers for now, TODO: figure out types...
                case "+" -> "";
                case "-" -> "";

                case "NOT" -> "xor i1 " + exprTemp + ", 1"; // XOR with a 1 always flips the bit
                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        context.ir.append("\n");

        return tmp;
    }

    @Override
    public String visitExpressionGroup(AST.Expression.Group expr) {
        return (String) this.visit(expr.expression());
    }

    @Override
    public String visitExpressionVariable(AST.Expression.Variable expr) {
        // COMPLICATED AS HELL!
        // For something with no postFix, do a simple lookup in scope (aka a load I think)...
        // For something with a postFix, need to generate more complicated IR...
        System.out.println(expr);
        return "";
    }

    //#endregion Expressions

    //#region Literals - returns their literal value as a string...

    @Override
    public String visitExpressionLiteralInteger(AST.Expression.Literal.Integer lit) {
        return lit.value().toString(10);
    }

    @Override
    public String visitExpressionLiteralReal(AST.Expression.Literal.Real lit) {
        return lit.value().toString();
    }

    @Override
    public String visitExpressionLiteralString(AST.Expression.Literal.String lit) {
        LLVMValue value = context.symbolTable.lookup("'" + lit.value() + "'", false).get();
        return value.getRef();
    }

    @Override
    public String visitExpressionLiteralCharacter(AST.Expression.Literal.Character lit) {
        return "" + (int) lit.value().charValue();
    }

    @Override
    public String visitExpressionLiteralBoolean(AST.Expression.Literal.Boolean lit) {
        return "" + (lit.value() ? 1 : 0);
    }

    @Override
    public String visitExpressionLiteralNil(AST.Expression.Literal.Nil lit) {
        return "null";
    }

    //#endregion Literals

}
