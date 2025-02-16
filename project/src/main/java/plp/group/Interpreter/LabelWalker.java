package plp.group.Interpreter;

import java.util.HashMap;

import org.antlr.v4.runtime.tree.ParseTree;

import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;

/**
 * Labelwalker will walk over the part of the parse tree it is given and get
 * all information about the labels...
 */
public class LabelWalker extends delphiBaseVisitor<Object> {
    private HashMap<String, delphiParser.StatementContext> labels = new HashMap<String, delphiParser.StatementContext>();

    public LabelWalker(ParseTree ctx) {
        visit(ctx);
    }

    public HashMap<String, delphiParser.StatementContext> getLabels() {
        return labels;
    }

    @Override
    public Void visitStatement(delphiParser.StatementContext ctx) {
        // If the statement is labelled, add it to the list of knowns
        if (ctx.COLON() != null) {
            var labelName = (String) visit(ctx.getChild(0));
            if (labels.containsKey(labelName)) {
                labels.replace(labelName, ctx);
            }
            // maybe throw error if not found previously...
        }
        // Visit the statement so that nothing breaks
        visit(ctx.getChild(ctx.getChildCount() - 1));
        return null;
    }

    @Override
    public Void visitLabelDeclarationPart(delphiParser.LabelDeclarationPartContext ctx) {
        // LABEL label (COMMA label)* SEMI
        for (var i = 1; i < ctx.getChildCount() - 1; i += 2) {
            var label = (String) visit(ctx.getChild(i));
            labels.put(label, null);
        }
        return null;
    }

    @Override
    public String visitLabel(delphiParser.LabelContext ctx) {
        return ctx.getText();
    }
}
