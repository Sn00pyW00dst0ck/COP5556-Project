package plp.group.Interpreter;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import plp.group.PascalTypes.PascalType;
import plp.group.PascalTypes.Callables.PascalProcedureType;
import plp.group.PascalTypes.Scalars.Standard.PascalBoolean;
import plp.group.PascalTypes.Scalars.Standard.PascalChar;
import plp.group.PascalTypes.Scalars.Standard.PascalInteger;
import plp.group.PascalTypes.Scalars.Standard.PascalLongint;
import plp.group.PascalTypes.Scalars.Standard.PascalReal;
import plp.group.PascalTypes.Scalars.Standard.PascalShortint;
import plp.group.PascalTypes.Scalars.Standard.PascalSmallint;
import plp.group.PascalTypes.Scalars.Standard.PascalString;
import plp.group.PascalTypes.Utils.PascalOperationHandler;
import plp.group.PascalTypes.Utils.PascalTypeRegistry;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;
import plp.group.project.delphiParser.AdditiveoperatorContext;
import plp.group.project.delphiParser.Bool_Context;
import plp.group.project.delphiParser.MultiplicativeoperatorContext;
import plp.group.project.delphiParser.RelationaloperatorContext;

/**
 * Interpret valid delphi code according to our grammar.
 * 
 * We override the delphiBaseVisitor with type <Object> so that
 * we can return any type from each function (useful for dealing)
 * with many types of literals.
 * 
 * Below interpreter is a minimum required to interpret the hello_world.pas
 * file.
 * It needs the creation/deletion of scopes in a proper way in order to be 100%
 * correct though,
 * but this can write to the screen the 'Hello World' string properly.
 */
public class Interpreter extends delphiBaseVisitor<Object> {

    private SymbolTable scope = new SymbolTable();
    private PascalTypeRegistry knownTypes = new PascalTypeRegistry();

    public Interpreter() {
        scope.enterScope();
        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html

        // #region Built-In IO Functions

        Scanner scanner = new Scanner(System.in);

        scope.insert("write", new SymbolInfo(
                "write",
                new PascalProcedureType(
                        List.of(),
                        arguments -> {
                            for (var arg : arguments) {
                                System.out.print(arg);
                            }
                            return null;
                        })));

        scope.insert("writeln", new SymbolInfo(
                "write",
                new PascalProcedureType(
                        List.of(),
                        arguments -> {
                            for (var arg : arguments) {
                                System.out.print(arg);
                            }
                            System.out.println();
                            return null;
                        })));

        // #endregion Built-In IO Functions
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        // The program block introduces the global scope
        scope.enterScope();
        visitChildren(ctx);
        scope.exitScope();
        return null;
    }

    // #region Declarations

    @Override
    public Void visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        Class<? extends PascalType> type = knownTypes.getType(ctx.getChild(ctx.getChildCount() - 1).getText());

        // For each identifier, insert it to the current scope with the type set.
        var identifiers = (ArrayList<String>) visit(ctx.getChild(0));
        for (String identifier : identifiers) {
            try {
                scope.insert(identifier, new SymbolInfo(identifier, type.getDeclaredConstructor().newInstance()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // #endregion Declarations

    // #region Identifiers

    /**
     * Returns the SymbolInfo for the variable.
     */
    @Override
    public SymbolInfo visitVariable(delphiParser.VariableContext ctx) {
        var name = (String) visit(ctx.getChild(0));
        return scope.lookup(name);
    }

    /**
     * Returns the result of visiting all the identifiers in a list.
     */
    @Override
    public List<String> visitIdentifierList(delphiParser.IdentifierListContext ctx) {
        List<String> identifiers = new ArrayList<String>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) { // Iterate by 2s to skip each COMMA
            identifiers.add((String) visit(ctx.getChild(i)));
        }
        return identifiers;
    }

    /**
     * Returns a string of the identifier name.
     */
    @Override
    public String visitIdentifier(delphiParser.IdentifierContext ctx) {
        return ctx.getText();
    }

    // #endregion Identifiers

    // #region Statements

    @Override
    public Void visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        var identifier = (SymbolInfo) visit(ctx.getChild(0));
        var value = (PascalType) visit(ctx.getChild(2));

        scope.update(identifier.name, new SymbolInfo(identifier.name, value));
        return null;
    }

    @Override
    public Void visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        var procedureDetails = (SymbolInfo) scope.lookup(((String) visit(ctx.getChild(0))));

        var parameters = new ArrayList<Object>(); // Do we want to pass all parameters as PascalType??
        for (var i = 2; i < ctx.getChildCount() - 1; i++) {
            var parameter = visit(ctx.getChild(i));
            if (parameter instanceof SymbolInfo) {
                parameter = ((SymbolInfo) parameter).value; // If SymbolInfo, grab the value from it.
            }
            parameters.add(((PascalType) parameter));
        }

        // Below craziness passes the parameters one at a time...
        ((PascalProcedureType) (procedureDetails.value)).invoke(parameters.toArray(new Object[0]));
        return null;
    }

    // #endregion Statements

    // #region Expressions

    /**
     * Visits the expression, returns whatever the value of the expression is.
     * 
     * The value is returned within a PascalType.
     */
    @Override
    public PascalType visitExpression(delphiParser.ExpressionContext ctx) {
        var lhs = (PascalType) visit(ctx.getChild(0));

        if (ctx.relationaloperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.relationaloperator());
        var rhs = (PascalType) visit(ctx.getChild(2));
        return PascalOperationHandler.performBinaryOperation(lhs, rhs, operator);
    }

    /**
     * Get the string representation (text) of the relational operator.
     */
    @Override
    public String visitRelationaloperator(RelationaloperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public PascalType visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
        var lhs = (PascalType) visit(ctx.getChild(0));

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.additiveoperator());
        var rhs = (PascalType) visit(ctx.getChild(2));
        return PascalOperationHandler.performBinaryOperation(lhs, rhs, operator);
    }

    /**
     * Get the string representation (text) of the additive operator.
     */
    @Override
    public String visitAdditiveoperator(AdditiveoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public PascalType visitTerm(delphiParser.TermContext ctx) {
        var lhs = (PascalType) visit(ctx.getChild(0));

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.multiplicativeoperator());
        var rhs = (PascalType) visit(ctx.getChild(2));
        return PascalOperationHandler.performBinaryOperation(lhs, rhs, operator);
    }

    /**
     * Get the string representation (text) of the multiplicative operator.
     */
    @Override
    public String visitMultiplicativeoperator(MultiplicativeoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public PascalType visitSignedFactor(delphiParser.SignedFactorContext ctx) {
        var factor = (PascalType) visit(ctx.factor());
        // NOTE: the ctx.PLUS() does nothing...
        if (ctx.MINUS() != null) {
            factor = PascalOperationHandler.performUnaryOperation(factor, "-");
        }
        return factor;
    }

    /**
     * Returns a PascalType with the information regarding this thing.
     */
    @Override
    public PascalType visitFactor(delphiParser.FactorContext ctx) {
        int childIndex = (ctx.LPAREN() != null || ctx.NOT() != null) ? 1 : 0;
        var result = visit(ctx.getChild(childIndex));

        // If its a symbol info, grab the PascalType from it.
        if (result instanceof SymbolInfo) {
            result = ((SymbolInfo) result).value;
        }

        // If a NOT is present, perform that operation.
        if (ctx.NOT() != null) {
            result = PascalOperationHandler.performUnaryOperation((PascalType) result, "NOT");
        }

        return (PascalType) result;

        // TODO: actually do all the below in their own visit functions (_set and
        // functionDesignator)...
        /*
         * factor
         * : variable
         * | LPAREN expression RPAREN
         * | functionDesignator
         * | unsignedConstant
         * | set_
         * | NOT factor
         * | bool_
         * ;
         */
    }

    // #endregion Expressions

    // #region Literals

    @Override
    public PascalString visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return new PascalString(literal.substring(1, literal.length() - 1));
    }

    @Override
    public PascalInteger visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
        return new PascalInteger(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public PascalReal visitUnsignedReal(delphiParser.UnsignedRealContext ctx) {
        return new PascalReal(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public PascalChar visitConstantChr(delphiParser.ConstantChrContext ctx) {
        var charCode = (PascalInteger) visit(ctx.getChild(2));
        return new PascalChar(charCode.getValue());
    }

    @Override
    public PascalBoolean visitBool_(Bool_Context ctx) {
        return new PascalBoolean(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    // #endregion Literals

}
