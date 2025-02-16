package plp.group.Interpreter;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.GeneralTypeFactory;
import plp.group.Interpreter.Types.Procedural.FunctionImplementation;
import plp.group.Interpreter.Types.Procedural.ProcedureImplementation;
import plp.group.Interpreter.Types.Simple.BooleanType;
import plp.group.Interpreter.Types.Simple.CharType;
import plp.group.Interpreter.Types.Simple.EnumType;
import plp.group.Interpreter.Types.Simple.StringType;
import plp.group.Interpreter.Types.Simple.Integers.GeneralInteger;
import plp.group.Interpreter.Types.Simple.Reals.GeneralReal;
import plp.group.Interpreter.Types.Simple.Reals.RealType;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;
import plp.group.project.delphiParser.AdditiveoperatorContext;
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
    // private GeneralTypeRegistry knownTypes = new GeneralTypeRegistry();

    public Interpreter() {
        scope.enterScope();
        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html

        // #region Built-In IO Functions

        Scanner scanner = new Scanner(System.in);

        scope.insert("write", new SymbolInfo(
                "write",
                GeneralTypeFactory.createProcedure((arguments -> {
                    for (var arg : arguments) {
                        System.out.print(arg);
                    }
                }))));

        scope.insert("writeln", new SymbolInfo(
                "writeln",
                GeneralTypeFactory.createProcedure((arguments -> {
                    for (var arg : arguments) {
                        System.out.print(arg);
                    }
                    System.out.println();
                }))));

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

    @Override
    public Void visitBlock(delphiParser.BlockContext ctx) {
        scope.enterScope();
        visitChildren(ctx);
        scope.exitScope();
        return null;
    }

    // #region Declarations

    @Override
    public Void visitTypeDefinition(delphiParser.TypeDefinitionContext ctx) {
        var typeIdentifier = (String) visit(ctx.getChild(0));
        var typeDefinition = (GeneralType) visit(ctx.getChild(2));
        GeneralTypeFactory.registerType(typeIdentifier.toLowerCase(), typeDefinition);
        return null;
    }

    @Override
    public Void visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        GeneralType instance;
        try {
            instance = GeneralTypeFactory.constructType(ctx.getChild(ctx.getChildCount() - 1).getText().toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        @SuppressWarnings("unchecked")
        var identifiers = (ArrayList<String>) visit(ctx.getChild(0));
        for (String identifier : identifiers) {
            scope.insert(identifier, new SymbolInfo(identifier, instance));
        }
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {
        var identifier = (String) visit(ctx.getChild(1));

        @SuppressWarnings("unchecked")
        var parameters = (List<SymbolInfo>) visit(ctx.getChild(2));

        var body = GeneralTypeFactory.createProcedure(arguments -> {
            scope.enterScope();
            // If we have expected parameters then add them here...
            if (parameters != null) {
                for (var i = 0; i < parameters.size(); i++) {
                    scope.insert(parameters.get(i).name,
                            new SymbolInfo(parameters.get(i).name, (GeneralType) arguments[i]));
                }
            }
            visit(ctx.getChild(ctx.getChildCount() - 1));
            scope.exitScope();
        });

        scope.insert(identifier, new SymbolInfo(identifier, body));
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SymbolInfo> visitFormalParameterList(delphiParser.FormalParameterListContext ctx) {
        var parameters = new ArrayList<SymbolInfo>();
        for (var i = 1; i < ctx.getChildCount(); i += 2) {
            parameters.addAll((List<SymbolInfo>) visit(ctx.getChild(i)));
        }
        return parameters;
    }

    // How to handle formal parameter section??

    @Override
    public List<SymbolInfo> visitParameterGroup(delphiParser.ParameterGroupContext ctx) {
        @SuppressWarnings("unchecked")
        var identifiers = (List<String>) visit(ctx.getChild(0));
        var typeIdentifier = (String) visit(ctx.getChild(ctx.getChildCount() - 1));

        var parameters = new ArrayList<SymbolInfo>();
        for (var identifier : identifiers) {
            try {
                parameters.add(new SymbolInfo(identifier, GeneralTypeFactory.constructType(typeIdentifier)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parameters;
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

    // #region Types

    @Override
    public GeneralType visitScalarType(delphiParser.ScalarTypeContext ctx) {
        @SuppressWarnings("unchecked")
        var identifiers = (List<String>) visit(ctx.getChild(1));

        // No overriding of the underlying values...
        var temp = new HashMap<String, Integer>();
        for (var i = 0; i < identifiers.size(); i++) {
            temp.put(identifiers.get(i), i);
        }

        // We can now add all the enum names into scope...
        for (var i = 0; i < identifiers.size(); i++) {
            scope.insert(identifiers.get(i),
                    new SymbolInfo(identifiers.get(i), GeneralTypeFactory.createEnum(temp, identifiers.get(i))));
        }

        return GeneralTypeFactory.createEnum(temp);
    }

    @Override
    public GeneralType visitSubrangeType(delphiParser.SubrangeTypeContext ctx) {
        var lhs = visit(ctx.getChild(0));
        var rhs = visit(ctx.getChild(2));

        if (lhs instanceof String) {
            lhs = scope.lookup((String) lhs).value;
            rhs = scope.lookup((String) rhs).value;
        }

        if (lhs instanceof GeneralInteger) {
            return GeneralTypeFactory.createSubrange((GeneralInteger) lhs, (GeneralInteger) rhs);
        }

        if (lhs instanceof GeneralReal) {
            return GeneralTypeFactory.createSubrange((GeneralInteger) lhs, (GeneralInteger) rhs);
        }

        if (lhs instanceof CharType) {
            return GeneralTypeFactory.createSubrange((CharType) lhs, (CharType) rhs);
        }

        if (lhs instanceof StringType) {
            return GeneralTypeFactory.createSubrange((StringType) lhs, (StringType) rhs);
        }

        if (lhs instanceof BooleanType) {
            return GeneralTypeFactory.createSubrange((StringType) lhs, (StringType) rhs);
        }

        if (lhs instanceof EnumType) {
            return GeneralTypeFactory.createSubrange((EnumType) lhs, (EnumType) rhs);
        }

        return null;
    }

    @Override
    public String visitTypeIdentifier(delphiParser.TypeIdentifierContext ctx) {
        return (String) ctx.getText();
    }

    @Override
    public Void visitStringtype(delphiParser.StringtypeContext ctx) {
        return null;
    }

    // #endregion Types

    // #region Statements

    @Override
    public Void visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        var identifier = (SymbolInfo) visit(ctx.getChild(0));
        var value = (GeneralType) visit(ctx.getChild(2));

        scope.update(identifier.name, new SymbolInfo(identifier.name, value));
        return null;
    }

    @Override
    public Void visitIfStatement(delphiParser.IfStatementContext ctx) {
        var expr = (BooleanType) visit(ctx.getChild(1));

        if (((Boolean) expr.getValue()).equals(Boolean.TRUE)) {
            visit(ctx.getChild(3));
        } else {
            visit(ctx.getChild(5));
        }

        return null;
    }

    @Override
    public Void visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        var procedureDetails = (SymbolInfo) scope.lookup(((String) visit(ctx.getChild(0))));

        @SuppressWarnings("unchecked")
        var parameters = (List<GeneralType>) visit(ctx.getChild(2));

        // Below craziness passes the parameters one at a time...
        ((ProcedureImplementation) procedureDetails.value.getValue()).execute(parameters.toArray(new Object[0]));
        return null;
    }

    @Override
    public List<GeneralType> visitParameterList(delphiParser.ParameterListContext ctx) {
        var parameters = new ArrayList<GeneralType>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) {
            parameters.add((GeneralType) visit(ctx.getChild(i)));
        }
        return parameters;
    }

    // #endregion Statements

    // #region Expressions

    /**
     * Visits the expression, returns whatever the value of the expression is.
     * 
     * The value is returned within a GeneralType.
     */
    @Override
    public GeneralType visitExpression(delphiParser.ExpressionContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.relationaloperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.relationaloperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator, rhs);
    }

    /**
     * Get the string representation (text) of the relational operator.
     */
    @Override
    public String visitRelationaloperator(RelationaloperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.additiveoperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator, rhs);
    }

    /**
     * Get the string representation (text) of the additive operator.
     */
    @Override
    public String visitAdditiveoperator(AdditiveoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitTerm(delphiParser.TermContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.multiplicativeoperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator, rhs);
    }

    /**
     * Get the string representation (text) of the multiplicative operator.
     */
    @Override
    public String visitMultiplicativeoperator(MultiplicativeoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitSignedFactor(delphiParser.SignedFactorContext ctx) {
        var factor = (GeneralType) visit(ctx.factor());
        // NOTE: the ctx.PLUS() does nothing...
        if (ctx.MINUS() != null) {
            factor = factor.applyOperation("NEGATE", null);
        }
        return factor;
    }

    /**
     * Returns a GeneralType with the information regarding this thing.
     */
    @Override
    public GeneralType visitFactor(delphiParser.FactorContext ctx) {
        int childIndex = (ctx.LPAREN() != null || ctx.NOT() != null) ? 1 : 0;
        var result = visit(ctx.getChild(childIndex));

        // If its a symbol info, grab the GeneralType from it.
        if (result instanceof SymbolInfo) {
            result = ((SymbolInfo) result).value;
        }

        // If a NOT is present, perform that operation.
        if (ctx.NOT() != null) {
            result = ((GeneralType) result).applyOperation("NOT", null);
        }

        return (GeneralType) result;

        // TODO: actually do all the below in their own visit functions (_set)...
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
    public GeneralType visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return GeneralTypeFactory.createString(literal.substring(1, literal.length() - 1));
    }

    @Override
    public GeneralType visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
        return GeneralTypeFactory.createInteger(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public GeneralType visitUnsignedReal(delphiParser.UnsignedRealContext ctx) {
        return GeneralTypeFactory.createReal(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public GeneralType visitConstantChr(delphiParser.ConstantChrContext ctx) {
        var charCode = (BigInteger) ((GeneralType) visit(ctx.getChild(2))).getValue();
        return GeneralTypeFactory.createChar(Character.valueOf((char) charCode.intValue()));
    }

    @Override
    public GeneralType visitBool_(delphiParser.Bool_Context ctx) {
        return GeneralTypeFactory.createBoolean(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    @Override
    public GeneralType visitFunctionDesignator(delphiParser.FunctionDesignatorContext ctx) {
        var function = (FunctionImplementation) (scope.lookup((String) visit(ctx.getChild(0))).value).getValue();

        @SuppressWarnings("unchecked")
        var parameters = (List<GeneralType>) visit(ctx.getChild(2));

        return function.execute(parameters.toArray(new Object[0]));
    }

    @Override
    public GeneralType visitSet_(delphiParser.Set_Context ctx) {
        @SuppressWarnings("unchecked")
        var elements = (List<GeneralType>) visit(ctx.getChild(1));
        return GeneralTypeFactory.createSet(new HashSet<GeneralType>(elements));
    }

    @Override
    public List<GeneralType> visitElementList(delphiParser.ElementListContext ctx) {
        var result = new ArrayList<GeneralType>();
        for (var i = 0; i < ctx.getChildCount() - 1; i++) {
            result.add(((GeneralType) visit(ctx.getChild(i))));
        }
        return result;
    }

    @Override
    public GeneralType visitElement(delphiParser.ElementContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.DOTDOT() == null) {
            return lhs;
        }

        return lhs.applyOperation("..", (GeneralType) visit(ctx.getChild(2)));
    }

    // #endregion Literals

}
