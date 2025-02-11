package plp.group.Interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.ibm.icu.impl.Pair;

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

        // #region Built-In IO Functions

        Scanner scanner = new Scanner(System.in);

        scope.insert("read", new SymbolInfo(
                "read",
                (List<Object> arguments) -> {
                    // FIGURE OUT THIS

                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("readln", new SymbolInfo(
                "readln",
                (List<Object> arguments) -> {
                    // FIGURE OUT THIS
                    scanner.nextLine();
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("write", new SymbolInfo(
                "write",
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("writeln", new SymbolInfo(
                "writeln",
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    System.out.println("");
                    return null;
                },
                List.of(Object[].class),
                Void.class));

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
        var identifiers = (ArrayList<SymbolInfo>) visit(ctx.getChild(0));
        for (SymbolInfo ident : identifiers) {
            scope.insert(ident.name, new SymbolInfo(ident.name, null, type));
        }

        return null;
    }

    /**
     * Inserts the procedure into the current scope. Does NOT execute the procedure.
     * 
     * Returns null.
     */
    @Override
    public Object visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {
        var procedureName = (SymbolInfo) visit(ctx.getChild(1));

        var paramsList = visit(ctx.getChild(2)); // TODO: implement visitFormalParameterList

        var body = ctx.getChild(4);

        SymbolTable capturedScope = scope;
        scope.insert(procedureName.name, new SymbolInfo(
                procedureName.name,
                (List<Object> arguments) -> {
                    // Create the new function scope (store current scope to restore later)
                    SymbolTable currentScope = scope;
                    scope = capturedScope;
                    scope.enterScope();

                    try {
                        // Add the arguments
                        for (int i = 0; i < arguments.size(); i++) {
                            scope.insert(null, new SymbolInfo(null, arguments.get(i), null)); // TODO: add type info
                        }

                        // Visit the body
                        visit(body);
                    } finally { // TODO: Error handling here??
                        // Restore scope no matter what
                        scope.exitScope();
                        scope = currentScope;
                    }

                    return null;
                },
                List.of(Object[].class), // TODO: update the list of arguments here to be correct...
                Void.class));

        return null;
    }

    @Override
    public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    // #endregion Declarations

    @Override
    public Object visitFormalParameterList(delphiParser.FormalParameterListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFormalParameterSection(delphiParser.FormalParameterSectionContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * Returns a list of SymbolInfo. The last element is the type (as it appears in
     * the
     * Pascal program).
     */
    @Override
    public Object visitParameterGroup(delphiParser.ParameterGroupContext ctx) {
        var identifiers = (ArrayList<SymbolInfo>) visit(ctx.getChild(0));
        var type = (SymbolInfo) visit(ctx.getChild(2));
        return identifiers.add(type);
    }

    // #region Identifiers

    /**
     * Returns the result of visiting all the identifiers in a list.
     */
    @Override
    public Object visitIdentifierList(delphiParser.IdentifierListContext ctx) {
        List<SymbolInfo> idents = new ArrayList<SymbolInfo>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) { // Iterate by 2s to skip each COMMA
            var ident = (SymbolInfo) visit(ctx.getChild(i));
            idents.add(ident);
        }
        return idents;
    }

    /**
     * Returns a SymbolInfo struct.
     * 
     * If in the scope, it will be filled out.
     * If not, it will have null fields but the name is there.
     */
    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        var info = scope.lookup(ctx.getText());
        if (info == null) {
            info = new SymbolInfo(ctx.getText(), null);
        }
        return info;
    }

    // #endregion Identifiers

    // Related to procedures

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        // Get the procedure name/details
        SymbolInfo procedureDetails = (SymbolInfo) scope.lookup(((SymbolInfo) visit(ctx.getChild(0))).name);

        System.out.println(((SymbolInfo) visit(ctx.getChild(0))).name);

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
