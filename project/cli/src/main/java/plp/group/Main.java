package plp.group;

import java.io.IOException;

import org.jline.reader.*;
import org.jline.terminal.*;
import org.jline.utils.InfoCmp.Capability;
import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.Compiler.CompilerContext;
import plp.group.Interpreter.Interpreter;
import plp.group.Optimizer.Optimizer;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

/**
 * Entry point for the CLI application.
 */
public class Main {

    private static Terminal terminal;
    private static LineReader reader;
    
    public static void main(String[] args) throws IOException {
        terminal = TerminalBuilder.terminal();
        reader = LineReaderBuilder.builder().terminal(terminal).build();

        String prompt = "cmd> ";
        String[] line;

        displayHelpMenu();
        while (true) {
            try {
                line = reader.readLine(prompt).split(" ");
                String programFile = (line.length > 2 && "-o".equals(line[1])) ? line[2] : (line.length > 1 ? line[1] : null);

                switch (line[0]) {
                    case "clear":
                        terminal.puts(Capability.clear_screen);
                        terminal.flush();
                        break;                    
                    case "exit":
                        return;
                    case "eval":
                        interpretProgram(programFile, "-o".equals(line[1]));
                        break;
                    case "tree":
                        displayParseTree(programFile, "-o".equals(line[1]));
                        break;
                    case "compile":
                        compile(programFile, "-o".equals(line[1]));
                        break;
                    case "help":
                        displayHelpMenu();
                        break;
                    default:
                        System.out.println("Bad command, try again. ");
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    private static void interpretProgram(String programFileName, boolean optimize) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(Main.class.getClassLoader().getResourceAsStream("programs/" + programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Apply optimization pass if necessary.
            if (optimize) {
                String optimized = (new Optimizer()).visit(tree);
                tree = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized)))).program();
            }

            var interpreter = new Interpreter(reader);
            interpreter.visit(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayParseTree(String programFileName, boolean optimize) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(Main.class.getClassLoader().getResourceAsStream("programs/" + programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Apply optimization pass if necessary.
            if (optimize) {
                String optimized = (new Optimizer()).visit(tree);
                tree = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized)))).program();
            }

            // Open a GUI window with the parse tree.
            var frame = Trees.inspect(tree, parser);
            frame.get().setSize(600, 800);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void compile(String programFileName, boolean optimize) {
        try {
            // Get the parse tree for the file we enter in command line.
            var lexer = new delphi_lexer(
                    CharStreams.fromStream(Main.class.getClassLoader().getResourceAsStream("programs/" + programFileName)));
            var tokens = new CommonTokenStream(lexer);
            var parser = new delphi(tokens);
            var tree = parser.program();

            // Apply optimization pass if necessary.
            if (optimize) {
                String optimized = (new Optimizer()).visit(tree);
                tree = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized)))).program();
            }

            ASTBuilder builder = new ASTBuilder();
            AST.Program AST = (AST.Program) builder.visit(tree);
            System.out.println((new CompilerContext()).compileToLLVMIR(AST));
            // TODO: logic to compile everything...
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
        System.out.println("clear");
        System.out.println("\tClears the screen.\n");
        System.out.println("exit");
        System.out.println("\tQuits the program.\n");
        System.out.println("compile <program_file>");
        System.out.println("\tCompile the program_file to LLVM IR.\n");
        System.out.println("compile -o <program_file>");
        System.out.println("\ttCompile the program_file to LLVM IR after optimizations are applied.\n");
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
