package plp.group.Interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;

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

    public Interpreter() {
        scope.enterScope();
        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html

        // IO Functions (TODO: Read and ReadLn)

        scope.insert("write", new SymbolInfo(
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("writeln", new SymbolInfo(
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    System.out.println("");
                    return null;
                },
                List.of(Object[].class),
                Void.class));
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
    public Object visitLabelDeclarationPart(delphiParser.LabelDeclarationPartContext ctx) {
        // TODO: figure out how to store labels and what they are used for.
        return visitChildren(ctx);
    }

    /**
     * Inserts each of the identifiers within the variable declaration to the
     * current scope with no value but specified type.
     * 
     * Returns null.
     */
    @Override
    public Object visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        var type = getJavaType(ctx.getChild(ctx.getChildCount() - 1).getText());

        // For each identifier, insert it to the current scope with the type set.
        var identifiers = (ArrayList<String>) visit(ctx.getChild(0));
        for (String ident : identifiers) {
            scope.insert(ident, new SymbolInfo(null, type));
        }

        return null;
    }

    @Override
    public Object visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {
        var procedureName = (String) visit(ctx.getChild(1));

        var paramsList = visit(ctx.getChild(2)); // TODO: implement visitFormalParameterList

        var body = visit(ctx.getChild(3)); // TODO: implement visitBlock

        return visitChildren(ctx);
    }

    @Override
    public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    // #endregion Declarations

    // #region Identifiers

    /**
     * Returns the text of all the identifiers in a list.
     */
    @Override
    public Object visitIdentifierList(delphiParser.IdentifierListContext ctx) {
        List<String> idents = new ArrayList<String>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) { // Iterate by 2s to skip each COMMA
            var ident = (String) visit(ctx.getChild(i));
            idents.add(ident);
        }
        return idents;
    }

    /**
     * Returns the text of the identifier as a string.
     */
    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        return ctx.getText();
    }

    // #endregion Identifiers

    // Related to procedures

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        // Get the procedure name/details
        SymbolInfo procedureDetails = (SymbolInfo) scope.lookup((String) visit(ctx.getChild(0)));

        // Visit parameters to get values to add to the parameters list
        var parameters = new ArrayList<Object>();
        for (var i = 2; i < ctx.getChildCount() - 1; i++) {
            parameters.add(visit(ctx.getChild(i)));
        }

        // Call the function and return the result.
        return procedureDetails.function.apply(parameters);
    }

    // Related to constants

    @Override
    public Object visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return literal.substring(1, literal.length() - 1);
    }

    // #region Helper Functions

    private Class<?> getJavaType(String type) {
        return switch (type.toLowerCase()) {
            // Numeric types
            case "integer" -> int.class;
            case "cardinal" -> long.class;
            case "shortint" -> byte.class;
            case "smallint" -> short.class;
            case "longint" -> long.class;
            case "int64" -> long.class;
            case "byte" -> short.class;
            case "word" -> char.class;
            case "longword" -> long.class;

            // Other basic types
            case "real" -> BigDecimal.class;
            case "char" -> char.class;
            case "boolean" -> boolean.class;

            // Handle strings, arrays, etc
            case "string" -> String.class;

            // Handle user defined types...

            default -> throw new IllegalStateException("Invalid type: " + type);
        };
    }

    // #endregion Helper Functions

}
