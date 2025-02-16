package plp.group;

import java.util.Scanner;

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
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter your command to perform.");
                System.out.print("> ");
                var input = scanner.nextLine().split(" ");

                switch (input[0]) {
                    case "exit":
                        return;
                    case "eval":
                        interpretProgram(input[1]);
                        break;
                    case "tree":
                        displayParseTree(input[1]);
                        break;
                    default:
                        System.out.println("Bad command, try again. ");
                        break;
                }
            }
        }
    }

    private static void interpretProgram(String programFileName) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphiLexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream(programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphiParser(tokens);
            var tree = parser.program();
            var interpreter = new Interpreter();
            interpreter.visit(tree);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    private static void displayParseTree(String programFileName) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphiLexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream(programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphiParser(tokens);
            var tree = parser.program();

            // Open a GUI window with the parse tree.
            Trees.inspect(tree, parser);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
