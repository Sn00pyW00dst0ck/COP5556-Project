package plp.group;

import java.util.Scanner;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import plp.group.Interpreter.Interpreter;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

/**
 * Right now this invokes ANTLR4 tooling to view the AST in a GUI window, in
 * future it will call our interpreter.
 *
 */
public class App {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            displayHelpMenu();
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
                    case "help":
                        displayHelpMenu();
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
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream(programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();
            var interpreter = new Interpreter();
            interpreter.visit(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayParseTree(String programFileName) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream(programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Open a GUI window with the parse tree.
            var frame = Trees.inspect(tree, parser);
            frame.get().setSize(600, 800);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayHelpMenu() {
        System.out.println("Help Menu:");
        System.out.println("--------------------");
        System.out.println("exit");
        System.out.println("\tQuits the program.\n");
        System.out.println("eval <program_file>");
        System.out.println("\tInterpret the program_file.\n");
        System.out.println("help");
        System.out.println("\tDisplays this help menu.\n");
        System.out.println("tree <program_file>");
        System.out.println("\tDisplays the parsed tree for the program_file in a GUI window.");
        System.out.println("--------------------");
    }

}
