package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;

/**
 * The interpreter that walks the tree and does the actual calculations/running of the program.
 */
public class Interpreter extends delphiBaseVisitor<Object> {
    

    //#region Expressions

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
