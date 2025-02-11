package plp.group.Interpreter;

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
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        // Get the procedure name/details
        SymbolInfo procedureDetails = (SymbolInfo) visit(ctx.getChild(0));

        // Visit parameters to get values to add to the parameters list
        var parameters = new ArrayList<Object>();
        for (var i = 2; i < ctx.getChildCount() - 1; i++) {
            parameters.add(visit(ctx.getChild(i)));
        }

        // Call the function and return the result.
        return procedureDetails.function.apply(parameters);
    }

    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        return scope.lookup(ctx.getText());
    }

    @Override
    public Object visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return literal.substring(1, literal.length() - 1);
    }
}
