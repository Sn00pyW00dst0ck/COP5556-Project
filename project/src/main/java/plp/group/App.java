package plp.group;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import plp.group.Interpreter.Interpreter;
import plp.group.project.delphiLexer;
import plp.group.project.delphiParser;

/**
 * Right now this invokes ANTLR4 tooling to view the AST in a GUI window, in
 * future it will call our interpreter.
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphiLexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream(args[0])));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphiParser(tokens);
            var tree = parser.program();

            // Open a GUI window with the parse tree & print it to command line.
            System.out.println(tree.toStringTree(parser));
            Trees.inspect(tree, parser);

            var interpreter = new Interpreter();
            System.out.println(interpreter.visit(tree));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
