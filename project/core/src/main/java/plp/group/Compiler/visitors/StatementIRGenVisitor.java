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

    //#region Variable Defs

    @Override
    public Object visitDeclarationVariable(AST.Declaration.Variable dec) {
        dec.variables().forEach((variable) -> {
            LLVMValue.Register tmp = new LLVMValue.Register("%" + variable.name(), context.getLLVMType(dec.type()));
            context.symbolTable.define(variable.name(), tmp);
            context.ir.append(tmp.getRef() + " = alloca " + tmp.getType() + "\n");
        });
        return null;
    }

    //#endregion Variable Defs

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
    public LLVMValue visitExpressionBinary(AST.Expression.Binary expr) {
        // Visit the LHS and RHS, generating the IR to compute them and getting their 'tmp' result.
        LLVMValue lhsTemp = (LLVMValue) this.visit(expr.lhs());
        LLVMValue rhsTemp = (LLVMValue) this.visit(expr.rhs());

        // TODO: figure out type calculation...
        String type = switch (lhsTemp.getType()) {
            case "double" -> "double";
            case "i32" -> {
                yield rhsTemp.getType().equals("double") ? "double" : "i32";
            }
            case "i8" -> "i8";
            case "i1" -> "i1";
            default -> "UNKNOWN"; // Placeholder for unknown types... they need to be handled too
        };

        // Generate an instruction based on LHS, RHS, and operand, then put result in a tmp...
        LLVMValue tmp = new LLVMValue.Register(context.getNextTmp(), type);
        context.symbolTable.define(tmp.getRef(), tmp);

        context.ir.append(tmp.getRef() + " = ");
        context.ir.append(
            switch (expr.operator()) {
                // I'm assuming integers for now, TODO: figure out types i vs f instructions...
                case "+" -> (tmp.getType() == "double" ? "f" : "") + "add " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "-" -> (tmp.getType() == "double" ? "f" : "") + "sub " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "*" -> (tmp.getType() == "double" ? "f" : "") + "mul " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "DIV", "/" -> (tmp.getType() == "double" ? "f" : "s") + "div " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "MOD" -> (tmp.getType() == "double" ? "f" : "s") + "rem " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();

                case "=" -> "icmp eq " + tmp.getRef() + ", " + rhsTemp.getRef();
                case "<>" -> "icmp ne " + tmp.getRef() + ", " + rhsTemp.getRef();
                case "<" -> "icmp slt " + tmp.getRef() + ", " + rhsTemp.getRef();
                case "<=" -> "icmp sle " + tmp.getRef() + ", " + rhsTemp.getRef();
                case ">" -> "icmp sgt " + tmp.getRef() + ", " + rhsTemp.getRef();
                case ">=" -> "icmp sge " + tmp.getRef() + ", " + rhsTemp.getRef();

                case "AND" -> "and i1 " + tmp.getRef() + ", " + rhsTemp.getRef();
                case "OR" -> "or i1 " + tmp.getRef() + ", " + rhsTemp.getRef();

                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        context.ir.append("\n");

        // Return the tmp so that other expressions can use it...
        return tmp;
    }

    @Override
    public LLVMValue visitExpressionUnary(AST.Expression.Unary expr) {
        // Visit the value first, then generate an instruction based on value and operand.
        LLVMValue exprTemp = (LLVMValue) this.visit(expr.expression());

        // Generate an instruction based on LHS, RHS, and operand, then put result in a tmp...
        LLVMValue tmp = new LLVMValue.Register(context.getNextTmp(), exprTemp.getType());
        context.symbolTable.define(tmp.getRef(), tmp);

        context.ir.append(tmp.getRef() + " = ");
        context.ir.append(
            switch (expr.operator()) {
                // I'm assuming integers for now, TODO: figure out types...
                case "+" -> (tmp.getType() == "double" ? "f" : "") + "add " + tmp.getType() + " 0, " + exprTemp.getRef();
                case "-" -> (tmp.getType() == "double" ? "f" : "") + "sub " + tmp.getType() + " 0, " + exprTemp.getRef();

                case "NOT" -> "xor i1 " + exprTemp.getRef() + ", 1"; // XOR with a 1 always flips the bit
                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        context.ir.append("\n");

        return tmp;
    }

    @Override
    public LLVMValue visitExpressionGroup(AST.Expression.Group expr) {
        return (LLVMValue) this.visit(expr.expression());
    }

    @Override
    public LLVMValue visitExpressionVariable(AST.Expression.Variable expr) {
        return switch (expr.variable()) {
            case AST.Variable.Simple simple -> {
                LLVMValue variable = context.symbolTable.lookup(simple.name(), false).get();
                LLVMValue tmp = new LLVMValue.Register(context.getNextTmp(), variable.getType());

                context.symbolTable.define(tmp.getRef(), tmp);
                context.ir.append(tmp.getRef() + " = load " + tmp.getType() + ", ptr " + variable.getRef() + "\n");
                yield tmp;
            }
            // NEED TO HANDLE ALL OTHER CASES HERE!
            // For something with a postFix, need to generate more complicated IR...
            default -> throw new RuntimeException("Unexpected variable type in expression!");
        };
    }

    //#endregion Expressions

    //#region Literals - returns their literal value as a string...

    @Override
    public LLVMValue.Immediate visitExpressionLiteralInteger(AST.Expression.Literal.Integer lit) {
        return new LLVMValue.Immediate(lit.value().toString(10), "i32");
    }

    @Override
    public LLVMValue.Immediate visitExpressionLiteralReal(AST.Expression.Literal.Real lit) {
        return new LLVMValue.Immediate(lit.value().toString(), "double");
    }

    @Override
    public LLVMValue.Immediate visitExpressionLiteralString(AST.Expression.Literal.String lit) {
        LLVMValue value = context.symbolTable.lookup("'" + lit.value() + "'", false).get();
        return new LLVMValue.Immediate(value.getRef(), value.getType());
    }

    @Override
    public LLVMValue.Immediate visitExpressionLiteralCharacter(AST.Expression.Literal.Character lit) {
        return new LLVMValue.Immediate("" + (int) lit.value().charValue(), "i8");
    }

    @Override
    public LLVMValue.Immediate visitExpressionLiteralBoolean(AST.Expression.Literal.Boolean lit) {
        return new LLVMValue.Immediate("" + (lit.value() ? 1 : 0), "i1");
    }

    @Override
    public LLVMValue.Immediate visitExpressionLiteralNil(AST.Expression.Literal.Nil lit) {
        return new LLVMValue.Immediate("null", null);
    }

    //#endregion Literals

}
