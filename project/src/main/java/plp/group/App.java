package plp.group;

import java.util.Scanner;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import plp.group.Interpreter.Interpreter;
import plp.group.Optimizer.Optimizer;
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

                String[] input = scanner.nextLine().split(" ");
                String command = input[0];
                String argument = (input.length > 2 && "-o".equals(input[1])) ? input[2] : (input.length > 1 ? input[1] : null);

                switch (command) {
                    case "exit":
                        return;
                    case "eval":
                        interpretProgram(argument, "-o".equals(input[1]));
                        break;
                    case "tree":
                        displayParseTree(argument, "-o".equals(input[1]));
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

    private static void interpretProgram(String programFileName, boolean optimize) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream("programs/" + programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Apply optimization pass if necessary.
            if (optimize) {
                String optimized = (new Optimizer()).visit(tree);
                tree = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized)))).program();
            }

            var interpreter = new Interpreter();
            interpreter.visit(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayParseTree(String programFileName, boolean optimize) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(App.class.getClassLoader().getResourceAsStream("programs/" + programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Apply optimization pass if necessary.
            if (optimize) {
                String optimized = (new Optimizer()).visit(tree);
                tree = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized)))).program();
                System.out.println(optimized);
            }

            // Open a GUI window with the parse tree.
            var frame = Trees.inspect(tree, parser);
            frame.get().setSize(600, 800);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the application's help menu.
     */
    private static void displayHelpMenu() {
        System.out.println("Help Menu:");
        System.out.println("--------------------");
        System.out.println("exit");
        System.out.println("\tQuits the program.\n");
        System.out.println("eval <program_file>");
        System.out.println("\tInterpret the program_file.\n");
        System.out.println("eval -o <program_file>");
        System.out.println("\tInterpret the program_file after optimizations are applied.\n");
        System.out.println("help");
        System.out.println("\tDisplays this help menu.\n");
        System.out.println("tree <program_file>");
        System.out.println("\tDisplays the parsed tree for the program_file in a GUI window.\n");
        System.out.println("tree -o <program_file>");
        System.out.println("\tDisplays the parsed tree for the program_file after optimizations are applied in a GUI window.");
        System.out.println("--------------------");
    }

}
