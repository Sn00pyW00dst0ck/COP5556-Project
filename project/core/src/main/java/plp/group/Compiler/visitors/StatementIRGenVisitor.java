package plp.group.Compiler.visitors;

import java.util.List;
import java.util.stream.Collectors;

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
    private final StringBuilder irBuilder;

    public StatementIRGenVisitor(CompilerContext context, StringBuilder irBuilder) {
        this.context = context;
        this.irBuilder = irBuilder;
    }

    //#region Variable Defs

    @Override
    public Object visitDeclarationVariable(AST.Declaration.Variable dec) {
        dec.variables().forEach((variable) -> {
            LLVMValue.Register tmp = new LLVMValue.Register("%" + variable.name(), context.getLLVMType(dec.type()));
            context.symbolTable.define(variable.name(), tmp);
            irBuilder.append(tmp.getRef() + " = alloca " + tmp.getType() + "\n");
        });
        return null;
    }

    //#endregion Variable Defs

    //#region Statements

    @Override
    public Object visitStatementLabeled(AST.Statement.Labeled stmt) {
        irBuilder.append("br label %label_" + stmt.label() + "\n");
        irBuilder.append("label_" + stmt.label() + ":\n");
        this.visit(stmt.statement());
        return null;
    }

    @Override
    public Object visitStatementAssignment(AST.Statement.Assignment stmt) {
        // Evaluate RHS first
        Object value = this.visit(stmt.value());
        LLVMValue rhs = (LLVMValue) value;

        // Evaluate LHS as pointer (should be an lvalue)
        EvaluatedVariable ev = evaluateVariable(stmt.variable().variable());

        if (!ev.isPointer()) {
            throw new RuntimeException("Cannot assign to a non-pointer variable.");
        }

        // Store value into pointer
        irBuilder.append("store " + rhs.getType() + " " + rhs.getRef() + ", " + ev.value().getType() + "* " + ev.value().getRef() + "\n");
        return null;
    }

    @Override
    public Object visitStatementVariable(AST.Statement.Variable stmt) {
        EvaluatedVariable ev = evaluateVariable(stmt.variable());

        if (ev.isPointer()) {
            // Do a final load
            String tmp = context.getNextTmp();
            irBuilder.append(tmp + " = load " + ev.type() + ", " + ev.value().getType() + " " + ev.value().getRef() + "\n");
            return new LLVMValue.Register(tmp, ev.type());
        } else {
            return ev.value(); // already a loaded value
        }
    }

    // Visit the block of statements and generate IR for each one
    @Override
    public Object visitStatementCompound(AST.Statement.Compound stmt) {
        stmt.statements().forEach(this::visit);
        return null;
    }
    
    @Override
    public Object visitStatementIf(AST.Statement.If stmt) {
        String condTmp = (String) ((LLVMValue) this.visit(stmt.condition())).getRef();
    
        String thenLabel = context.getNextLabel();
        String elseLabel = context.getNextLabel();
        String endLabel = context.getNextLabel();
    
        // Branch based on condition
        irBuilder.append("br i1 " + condTmp + ", label %" + thenLabel + ", label %" + elseLabel + "\n");
    
        // Then block
        irBuilder.append(thenLabel + ":\n");
        this.visit(stmt.thenCase());
        irBuilder.append("br label %" + endLabel + "\n");
    
        // Else block
        irBuilder.append(elseLabel + ":\n");
        if (stmt.elseCase() != null) {
            this.visit(stmt.elseCase());
        }
        irBuilder.append("br label %" + endLabel + "\n");
    
        // End label
        irBuilder.append(endLabel + ":\n");
        return null;
    }
    
    @Override 
    public Object visitStatementWhile(AST.Statement.While stmt) {
        String condLabel = context.getNextLabel();
        String bodyLabel = context.getNextLabel();
        String endLabel = context.getNextLabel();
    
        // Branch to condition check
        irBuilder.append("br label %" + condLabel + "\n");
    
        // Condition block
        irBuilder.append(condLabel + ":\n");
        LLVMValue condVal = (LLVMValue) this.visit(stmt.condition());
        irBuilder.append("br i1 " + condVal.getRef() + ", label %" + bodyLabel + ", label %" + endLabel + "\n");
    
        // Push loop labels for break/continue handling
        context.pushLoopLabels(endLabel, condLabel);
    
        // Loop body block
        irBuilder.append(bodyLabel + ":\n");
        this.visit(stmt.body());
        irBuilder.append("br label %" + condLabel + "\n");
    
        // Pop loop labels after loop body
        context.popLoopLabels();
    
        // End of loop
        irBuilder.append(endLabel + ":\n");
        return null;
    }

    @Override
    public Object visitStatementGoto(AST.Statement.Goto stmt) {
        irBuilder.append("br label %label_" + stmt.label() + "\n");
        return null;
    }

    @Override
    public Object visitStatementCase(AST.Statement.Case stmt) {
        LLVMValue cond = (LLVMValue) this.visit(stmt.expr());
        String endLabel = context.getNextLabel();

        // Generate labels for each case branch
        List<String> caseLabels = stmt.branches().stream()
            .map(_ -> context.getNextLabel())
            .collect(Collectors.toList());

        // Generate a label for the else branch if it exists
        for (int i = 0; i < stmt.branches().size(); i++) {
            AST.Statement.Case.CaseElement caseElement = stmt.branches().get(i);
            for (AST.Expression val : caseElement.values()) {
                LLVMValue matchVal = (LLVMValue) this.visit(val);
                String caseLabel = caseLabels.get(i);
                String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = icmp eq i32 " + cond.getRef() + ", " + matchVal.getRef() + "\n");
                irBuilder.append("br i1 " + tmp + ", label %" + caseLabel + ", label %" + endLabel + "\n");
            }
        }

        for (int i = 0; i < stmt.branches().size(); i++) {
            String label = caseLabels.get(i);
            AST.Statement.Case.CaseElement caseElement = stmt.branches().get(i);
            irBuilder.append(label + ":\n");
            this.visit(caseElement.body());
            irBuilder.append("br label %" + endLabel + "\n");
        }

        stmt.elseBranch().ifPresent(elseBranch -> {
            this.visit(elseBranch);
            irBuilder.append("br label %" + endLabel + "\n");
        });

        irBuilder.append(endLabel + ":\n");
        return null;
    }

    @Override
    public Object visitStatementRepeat(AST.Statement.Repeat stmt) {
        String bodyLabel = context.getNextLabel();
        String endLabel = context.getNextLabel();

        // Push loop labels for break/continue handling
        irBuilder.append(bodyLabel + ":\n");
        this.visit(stmt.body());

        // Pop loop labels after loop body
        LLVMValue condVal = (LLVMValue) this.visit(stmt.condition());
        irBuilder.append("br i1 " + condVal.getRef() + ", label %" + endLabel + ", label %" + bodyLabel + "\n");

        // End of loop 
        irBuilder.append(endLabel + ":\n");
        return null;
    }

    @Override
    public Object visitStatementFor(AST.Statement.For stmt) {
        String loopVar = stmt.variable().toString();
        String loopStart = ((LLVMValue) this.visit(stmt.initialValue())).getRef();
        String loopEnd = ((LLVMValue) this.visit(stmt.finalValue())).getRef();

        String varTmp = context.getNextTmp();
        LLVMValue.Register loopReg = new LLVMValue.Register(varTmp, "i32");
        context.symbolTable.define(loopVar, loopReg);
        irBuilder.append(varTmp + " = alloca i32\n");
        irBuilder.append("store i32 " + loopStart + ", i32* " + varTmp + "\n");

        String loopCondLabel = context.getNextLabel();
        String loopBodyLabel = context.getNextLabel();
        String loopEndLabel = context.getNextLabel();

        irBuilder.append("br label %" + loopCondLabel + "\n");

        irBuilder.append(loopCondLabel + ":\n");
        String currentVal = context.getNextTmp();
        irBuilder.append(currentVal + " = load i32, i32* " + varTmp + "\n");

        String cmpTmp = context.getNextTmp();
        String cmpOp = stmt.type() == AST.Statement.For.LoopType.TO ? "icmp sle" : "icmp sge";
        irBuilder.append(cmpTmp + " = " + cmpOp + " i32 " + currentVal + ", " + loopEnd + "\n");
        irBuilder.append("br i1 " + cmpTmp + ", label %" + loopBodyLabel + ", label %" + loopEndLabel + "\n");

        irBuilder.append(loopBodyLabel + ":\n");
        this.visit(stmt.body());
        String nextVal = context.getNextTmp();
        String op = stmt.type() == AST.Statement.For.LoopType.TO ? "add" : "sub";
        irBuilder.append(nextVal + " = " + op + " i32 " + currentVal + ", 1\n");
        irBuilder.append("store i32 " + nextVal + ", i32* " + varTmp + "\n");
        irBuilder.append("br label %" + loopCondLabel + "\n");

        irBuilder.append(loopEndLabel + ":\n");
        return null;
    }

    @Override
    public Object visitStatementWith(AST.Statement.With stmt) {
        for (AST.Variable var : stmt.variables()) {
            this.evaluateVariable(var); // Potential for context changes
        }
        return this.visit(stmt.statement());
    }

    //#endregion Statements

    //#region Expressions - returns the 'temp' they create as a string...

    @Override
    public LLVMValue visitExpressionBinary(AST.Expression.Binary expr) {
        // Visit the LHS and RHS, generating the IR to compute them and getting their 'tmp' result.
        LLVMValue lhsTemp = (LLVMValue) this.visit(expr.lhs());
        LLVMValue rhsTemp = (LLVMValue) this.visit(expr.rhs());

        // Calculate the type of the result...
        String type = switch (lhsTemp.getType()) {
            case "double" -> "double";
            case "i32" -> {
                yield rhsTemp.getType().equals("double") ? "double" : "i32";
            }
            case "i8" -> "i8";
            case "i1" -> "i1";
            default -> lhsTemp.getType(); // Placeholder for unknown types is to assume both sides are the same...
        };
        // The comparison operators all result in a boolean
        type = switch (expr.operator()) {
            case "=", "<>", "<", "<=", ">", ">=" -> "i1";
            default -> type;
        };

        // Promote operands to result type...


        // Generate an instruction based on LHS, RHS, and operand, then put result in a tmp...
        LLVMValue tmp = new LLVMValue.Register(context.getNextTmp(), type);
        context.symbolTable.define(tmp.getRef(), tmp);

        irBuilder.append(tmp.getRef() + " = ");
        irBuilder.append(
            switch (expr.operator().toUpperCase()) {
                case "+" -> (tmp.getType() == "double" ? "f" : "") + "add " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "-" -> (tmp.getType() == "double" ? "f" : "") + "sub " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "*" -> (tmp.getType() == "double" ? "f" : "") + "mul " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "DIV", "/" -> (tmp.getType() == "double" ? "f" : "s") + "div " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "MOD" -> (tmp.getType() == "double" ? "f" : "s") + "rem " + tmp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();

                case "=" -> (tmp.getType().equals("double") ? "fcmp oeq " : "icmp eq ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "<>" -> (tmp.getType().equals("double") ? "fcmp one " : "icmp ne ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "<" -> (tmp.getType().equals("double") ? "fcmp olt " : "icmp slt ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "<=" -> (tmp.getType().equals("double") ? "fcmp ole " : "icmp sle ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case ">" -> (tmp.getType().equals("double") ? "fcmp ogt " : "icmp sgt ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case ">=" -> (tmp.getType().equals("double") ? "fcmp oge " : "icmp sge ") + lhsTemp.getType() + " " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                
                case "AND" -> "and i1 " + lhsTemp.getRef() + ", " + rhsTemp.getRef();
                case "OR" -> "or i1 " + lhsTemp.getRef() + ", " + rhsTemp.getRef();

                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        irBuilder.append("\n");

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

        irBuilder.append(tmp.getRef() + " = ");
        irBuilder.append(
            switch (expr.operator()) {
                case "+" -> (tmp.getType() == "double" ? "f" : "") + "add " + tmp.getType() + " 0, " + exprTemp.getRef();
                case "-" -> (tmp.getType() == "double" ? "f" : "") + "sub " + tmp.getType() + " 0, " + exprTemp.getRef();

                case "NOT" -> "xor i1 " + exprTemp.getRef() + ", 1"; // XOR with a 1 always flips the bit
                // TODO: OTHER CASES IF APPLICABLE
                default -> throw new RuntimeException("Unexpected operator: " + expr.operator());
            }
        );
        irBuilder.append("\n");

        return tmp;
    }

    @Override
    public LLVMValue visitExpressionGroup(AST.Expression.Group expr) {
        return (LLVMValue) this.visit(expr.expression());
    }

    @Override
    public LLVMValue visitExpressionVariable(AST.Expression.Variable expr) {
        EvaluatedVariable ev = evaluateVariable(expr.variable());

        if (ev.isPointer()) {
            // Do a final load
            String tmp = context.getNextTmp();
            irBuilder.append(tmp + " = load " + ev.type() + ", " + ev.value().getType() + "* " + ev.value().getRef() + "\n");
            return new LLVMValue.Register(tmp, ev.type());
        } else {
            return ev.value(); // already a loaded value
        }
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

    //#region Variable Helper

    /**
     * A small helper to make handling variables a bit easier to manage...
     */
    private record EvaluatedVariable(
        LLVMValue value,
        String type,     // LLVM type
        boolean isPointer // true if value should be loaded in expression
    ) {}

    /**
     * Evaluate a variable so that we can read or write it...
     * 
     * @param variable
     * @return
     */
    private EvaluatedVariable evaluateVariable(AST.Variable variable) {
        return switch (variable) {
            case AST.Variable.Simple simple -> {
                LLVMValue variableValue = context.symbolTable.lookup(simple.name(), false).get();
                yield new EvaluatedVariable(variableValue, variableValue.getType(), true);
            }
            case AST.Variable.Address address -> {
                // Address-of operator, don't load the pointer? 
                EvaluatedVariable base = evaluateVariable(address.variable());
                yield new EvaluatedVariable(base.value(), base.type(), false); // It's already a pointer...
            }
            case AST.Variable.PostFixVariable postFixVar -> {
                EvaluatedVariable current = evaluateVariable(postFixVar.base());

                for (AST.Variable.PostFix postfix : postFixVar.postFixes()) {
                    current = switch (postfix) {
                        case AST.Variable.PostFix.FieldAccess fieldAccess -> {
                            yield null;
                        }
                        case AST.Variable.PostFix.MethodCall methodCall -> {
                            // Assume simple normal function calls for now...
                            if (!(current.value() instanceof LLVMValue.LLVMFunction function)) {
                                throw new RuntimeException("Attempted to call a non-function: " + current.value());
                            }

                            // Evaluate the args...
                            List<LLVMValue> argValues = new java.util.ArrayList<>();
                            for (var arg : methodCall.args()) {
                                LLVMValue val = (LLVMValue) this.visit(arg.expr());
                                argValues.add(val);
                            }

                            // Generate the call instruction and return evaluated result of call
                            yield new EvaluatedVariable(
                                function.emitCall(argValues, context, irBuilder).orElse(null), // if we don't emit a value then set the LLVMValue to null...?
                                function.returnType(),
                                false
                            );
                        }
                        case AST.Variable.PostFix.ArrayAccess arrayAccess -> {
                            yield null;
                        }
                        case AST.Variable.PostFix.PointerDereference _ -> {
                            String tmp = context.getNextTmp();
                            irBuilder.append(tmp + " = load " + current.type() + ", " + current.value().getType() + " " + current.value().getRef() + "\n");
    
                            LLVMValue loaded = new LLVMValue.Register(tmp, current.type());
                            yield new EvaluatedVariable(loaded, current.type(), false);
                        }
                        default -> throw new RuntimeException("Unknown postfix!");
                    };
                }

                yield current;
            }
            default -> throw new RuntimeException("Unexpected variable type!");
        };
    }

    //#endregion

}
