package plp.group.Compiler;

import plp.group.AST.ASTBaseVisitor;

/**
 * 
 */
public class StringCollectionVisitor extends ASTBaseVisitor<Void> {
    private final CompilerContext context;

    public StringCollectionVisitor(CompilerContext context) {
        this.context = context;
    }

    // TODO: override various things so we grab all string literals... put them into compiler context as LLVM values....
    // TODO: test by checking against programs and counting number of literals found...
    // Be careful that duplicate string literals aren't defined...
}
