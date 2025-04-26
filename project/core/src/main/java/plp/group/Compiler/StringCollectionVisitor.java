package plp.group.Compiler;

import plp.group.AST.ASTBaseVisitor;
import java.util.HashSet;
import java.util.Set;

public class StringCollectionVisitor extends ASTBaseVisitor<Void> {
    /**
     * The context in which the visitor is operating.
     */
    private final CompilerContext context;
    private final Set<String> seenStrings = new HashSet<>();

    public StringCollectionVisitor(CompilerContext context) {
        this.context = context;
    }

    @Override
    public Void visitExpressionLiteralString(plp.group.AST.AST.Expression.Literal.String node) {
        String value = node.value();

        if (!seenStrings.contains(value)) {
            seenStrings.add(value);
            context.createGlobalString(value);
        }
        return null;
    }
    
}

