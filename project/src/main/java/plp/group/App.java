package plp.group;

import javax.swing.JFrame;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import plp.group.project.pascalLexer;
import plp.group.project.pascalParser;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // Use a simple hello world program to test
        var program = """
                program Hello;
                begin
                  writeln ('Hello, world.');
                end.
                """;

        try {
            var lexer = new pascalLexer(CharStreams.fromString(program)); // CharStreams.fromFileName(args[0])
            var tokens = new CommonTokenStream(lexer);
            var parser = new pascalParser(tokens);

            var tree = parser.program();

            // Open a GUI window with the parse tree
            JFrame frame = Trees.inspect(tree, parser).get();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
