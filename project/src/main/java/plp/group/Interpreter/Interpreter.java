package plp.group.Interpreter;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphiParser.*;
import plp.group.project.delphiVisitor;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;

public class Interpreter extends delphiBaseVisitor<Integer> {

    private Scope scope = new Scope(null);

    // @Override
    // public Integer visitIdentifier(delphiParser.IdentifierContext ctx) {
    // System.out.println(ctx.IDENT().getText());
    // return visitChildren(ctx);
    // }

    // @Override
    // public Integer visitStatement(delphiParser.StatementContext ctx) {
    // System.out.println(ctx.getText());
    // return visitChildren(ctx);
    // }

    @Override
    public Integer visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        System.out.println(ctx.getText());

        System.out.println(ctx.getChild(0).getText());
        System.out.println(ctx.getChild(1).getText());
        System.out.println(ctx.getChild(2).getText());
        System.out.println(ctx.getChild(3).getText());

        return visitChildren(ctx);
    }
}
