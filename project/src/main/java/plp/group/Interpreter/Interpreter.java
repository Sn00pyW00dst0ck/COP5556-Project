package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;

/**
 * The interpreter that walks the tree and does the actual calculations/running of the program.
 */
public class Interpreter extends delphiBaseVisitor<Object> {
    
    /**
     * The scope of the interpreter as it is running. 
     */
    private Scope scope = new Scope(Optional.empty());

    /*
     * TODO: 
     *  1. Built in functions (write, writeln, readln).
     *  2. Finish visitFactor & visitConstant & visitVariable.
     *  3. Statements (broad / biggest thing)
     *      3.5. Break/continue keywords.
     *  4. Function/Procedure Definitions.
     *  5. Class Definitions / Usage -> Pair program? 
     *  6. Other types we had in P1 (range, enum, etc).
     * 
     *  * Write unit tests!
     */

    //#region Statements

    //#endregion Statements

    //#region Expressions

    @Override
    public RuntimeValue visitExpression(delphi.ExpressionContext ctx) {
        RuntimeValue lhs = visitSimpleExpression(ctx.simpleExpression());

        if (ctx.relationaloperator() == null) {
            return lhs;
        }
        
        return switch (ctx.relationaloperator().getChild(0)) {
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.EQUAL ||
                t.getSymbol().getType() == delphi.NOT_EQUAL ||
                t.getSymbol().getType() == delphi.LT ||
                t.getSymbol().getType() == delphi.LE ||
                t.getSymbol().getType() == delphi.GT ||
                t.getSymbol().getType() == delphi.GE
            ) -> {
                RuntimeValue rhs = visitExpression(ctx.expression());

                // If either one is not a comparable there is an issue
                var lhsValue = RuntimeValue.requireType(lhs, Comparable.class);
                var rhsValue = RuntimeValue.requireType(rhs, lhsValue.getClass());

                @SuppressWarnings("unchecked")
                var result = lhsValue.compareTo(rhsValue);

                yield switch (t.getSymbol().getType()) {
                    case delphi.EQUAL -> new RuntimeValue.Primitive(result == 0);
                    case delphi.NOT_EQUAL -> new RuntimeValue.Primitive(result != 0);
                    case delphi.LT -> new RuntimeValue.Primitive(result < 0);
                    case delphi.LE -> new RuntimeValue.Primitive(result <= 0);
                    case delphi.GT -> new RuntimeValue.Primitive(result > 0);
                    case delphi.GE -> new RuntimeValue.Primitive(result >= 0);
                    default -> throw new RuntimeException("Unexpected item '" + ctx.relationaloperator().getText() + "' when attempting to evaluate 'expression'.");
                };
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.relationaloperator().getText() + "' when attempting to evaluate 'expression'.");
        };
    }

    @Override
    public RuntimeValue visitSimpleExpression(delphi.SimpleExpressionContext ctx) {
        RuntimeValue lhs = visitTerm(ctx.term());

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        return switch (ctx.additiveoperator().getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.OR -> {
                Boolean result = RuntimeValue.requireType(lhs, Boolean.class) || RuntimeValue.requireType(visitSimpleExpression(ctx.simpleExpression()), Boolean.class);
                yield new RuntimeValue.Primitive(result);
            }
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.PLUS ||
                t.getSymbol().getType() == delphi.MINUS
            ) -> {
                RuntimeValue rhs = visitSimpleExpression(ctx.simpleExpression());

                // If either one is not a number there is an issue
                var lhsValue = RuntimeValue.requireType(lhs, Number.class);
                var rhsValue = RuntimeValue.requireType(rhs, Number.class);

                if (lhsValue instanceof BigDecimal || rhsValue instanceof BigDecimal) {
                    yield switch (t.getSymbol().getType()) {
                        case delphi.PLUS -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).add(new BigDecimal(rhsValue.toString())));
                        case delphi.MINUS -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).subtract(new BigDecimal(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'simple expression'."); // Should never happen
                    };
                } else {
                    yield switch (t.getSymbol().getType()) {
                        case delphi.PLUS -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).add(new BigInteger(rhsValue.toString())));
                        case delphi.MINUS -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).subtract(new BigInteger(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'simple expression'."); // Should never happen
                    };
                }
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.additiveoperator().getText() + "' when attempting to evaluate 'simple expression'.");
        };
    }

    @Override
    public RuntimeValue visitTerm(delphi.TermContext ctx) {
        RuntimeValue lhs = visitSignedFactor(ctx.signedFactor());

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        return switch (ctx.multiplicativeoperator().getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.AND -> {
                Boolean result = RuntimeValue.requireType(lhs, Boolean.class) && RuntimeValue.requireType(visitTerm(ctx.term()), Boolean.class);
                yield new RuntimeValue.Primitive(result);
            }
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.STAR ||
                t.getSymbol().getType() == delphi.SLASH ||
                t.getSymbol().getType() == delphi.DIV ||
                t.getSymbol().getType() == delphi.MOD
            ) -> {
                RuntimeValue rhs = visitTerm(ctx.term());

                // If either one is not a number there is an issue
                var lhsValue = RuntimeValue.requireType(lhs, Number.class);
                var rhsValue = RuntimeValue.requireType(rhs, Number.class);

                if (lhsValue instanceof BigDecimal || rhsValue instanceof BigDecimal) {
                    // We know the result type is BigDecimal
                    yield switch (t.getSymbol().getType()) {
                        case delphi.STAR -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).multiply(new BigDecimal(rhsValue.toString())));
                        case delphi.DIV -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).divide(new BigDecimal(rhsValue.toString())));
                        case delphi.SLASH -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).divide(new BigDecimal(rhsValue.toString())));
                        case delphi.MOD -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).remainder(new BigDecimal(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'term'."); // Should never happen
                    };
                } else {
                    // We know the result type is BigInteger
                    yield switch (t.getSymbol().getType()) {
                        case delphi.STAR -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).multiply(new BigInteger(rhsValue.toString())));
                        case delphi.DIV -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).divide(new BigInteger(rhsValue.toString())));
                        case delphi.SLASH -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).divide(new BigInteger(rhsValue.toString())));
                        case delphi.MOD -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).remainder(new BigInteger(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'term'."); // Should never happen
                    };    
                }
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.multiplicativeoperator().getText() + "' when attempting to evaluate 'term'.");
        };
    }

    @Override
    public RuntimeValue visitSignedFactor(delphi.SignedFactorContext ctx) {
        return switch (ctx.getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.PLUS -> visitFactor(ctx.factor());
            case TerminalNode t when t.getSymbol().getType() == delphi.MINUS -> {
                Number n = RuntimeValue.requireType(visitFactor(ctx.factor()), Number.class);
                yield switch (n) {
                    case BigInteger bigInteger -> new RuntimeValue.Primitive(bigInteger.negate());
                    case BigDecimal bigDecimal -> new RuntimeValue.Primitive(bigDecimal.negate());
                    default -> throw new RuntimeException("Expected BigInteger or BigDecimal when attempting to negate value when attempting to evaluate 'signed factor'.");
                };
            }
            case delphi.FactorContext factorCtx -> visitFactor(factorCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'signed factor'.");
        };
    }

    @Override
    public RuntimeValue visitFactor(delphi.FactorContext ctx) {
        // We can tell what we are parsing based on the first child, so use a switch expression to return the proper thing.
        return switch (ctx.getChild(0)) {
            // case delphi.VariableContext variableCtx -> null;
            // case TerminalNode t when t.getSymbol().getType() == delphi.LPAREN -> null;
            // case delphi.FunctionDesignatorContext functionDesignatorCtx -> null;
            case delphi.UnsignedConstantContext unsignedConstantCtx -> visitUnsignedConstant(unsignedConstantCtx);
            // case delphi.Set_Context setContext -> null;
            case TerminalNode t when t.getSymbol().getType() == delphi.NOT -> new RuntimeValue.Primitive(!RuntimeValue.requireType(visitFactor(ctx.factor()), Boolean.class));
            case delphi.Bool_Context boolCtx -> visitBool_(boolCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'factor'.");
        };
    }

    //#endregion Expressions

    //#region Constants

    @Override
    public RuntimeValue visitConstant(delphi.ConstantContext ctx) {
        return switch (ctx.getChild(0)) {
            // case delphi.IdentifierContext identifierCtx -> null;
            case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
            case delphi.SignContext signCtx -> {
                RuntimeValue value = switch (ctx.getChild(1)) {
                    // case delphi.IdentifierContext identifierCtx -> null;
                    case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
                    default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(1).getText() + "' when attempting to evaluate 'constant'.");
                };

                if (signCtx.MINUS() != null) {
                    Number n = RuntimeValue.requireType(value, Number.class);
                    value = switch (n) {
                        case BigInteger bigInteger -> new RuntimeValue.Primitive(bigInteger.negate());
                        case BigDecimal bigDecimal -> new RuntimeValue.Primitive(bigDecimal.negate());
                        default -> throw new RuntimeException("Expected BigInteger or BigDecimal when attempting to negate value when attempting to evaluate 'constant'.");
                    };
                }

                yield value;
            }
            case delphi.StringContext stringCtx -> visitString(stringCtx);
            case delphi.ConstantChrContext constantChrCtx -> visitConstantChr(constantChrCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'constant'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedConstant(delphi.UnsignedConstantContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
            case delphi.ConstantChrContext constantChrCtx -> visitConstantChr(constantChrCtx);
            case delphi.StringContext stringContext -> visitString(stringContext);
            case TerminalNode t when t.getSymbol().getType() == delphi.NIL -> new RuntimeValue.Primitive(null);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'UnsignedConstant'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedNumber(delphi.UnsignedNumberContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.UnsignedIntegerContext unsignedIntegerCtx -> visitUnsignedInteger(unsignedIntegerCtx);
            case delphi.UnsignedRealContext unsignedRealCtx -> visitUnsignedReal(unsignedRealCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'UnsignedNumber'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedInteger(delphi.UnsignedIntegerContext ctx) {
        return new RuntimeValue.Primitive(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public RuntimeValue visitUnsignedReal(delphi.UnsignedRealContext ctx) {
        return new RuntimeValue.Primitive(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public RuntimeValue visitBool_(delphi.Bool_Context ctx) {
        return new RuntimeValue.Primitive(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    @Override
    public RuntimeValue visitString(delphi.StringContext ctx) {
        String literal = ctx.STRING_LITERAL().getText();
        return new RuntimeValue.Primitive(literal.substring(1, literal.length() - 1));
    }

    @Override
    public RuntimeValue visitConstantChr(delphi.ConstantChrContext ctx) {
        int charCode = Integer.parseInt(ctx.unsignedInteger().NUM_INT().getText());
        return new RuntimeValue.Primitive((char) charCode);
    }

    //#endregion Constants

}
