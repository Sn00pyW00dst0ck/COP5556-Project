package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi_lexer;
import plp.group.project.delphi;

public class InterpreterUnitTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @ParameterizedTest
    @MethodSource
    void testInterpreter(String testName, String programFile, String outputFile) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        Interpreter interpreter = new Interpreter(null);
        interpreter.visit(parser.program());

        InputStream expectedOutputStream = getClass().getClassLoader().getResourceAsStream("outputs/" + outputFile);
        String expectedOutput = new String(expectedOutputStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expectedOutput, outContent.toString());
    }

    //loops
    @Test
    public void testLoopExecution() throws Exception {
        String programName = "loop_test";
        String expectedOutput = readFile("src/main/resources/outputs/" + programName + ".out");
        String actualOutput = runProgram("src/main/resources/programs/" + programName + ".pas");

        assertEquals(expectedOutput.trim(), actualOutput.trim(), "Loop execution did not produce expected output.");
    }

    private String readFile(String filePath) throws java.io.IOException {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
    }

    private String runProgram(String filePath) throws Exception {
        // Depending on your setup, this might call Interpreter.main or similar execution utility...
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));
        plp.group.Main.main(new String[]{filePath});
        return outContent.toString();
    }

    // for loop
    @Test
    public void testForLoopExecution() throws Exception {
        String program = "for_loop_test";
        String expected = readFile("src/main/resources/outputs/" + program + ".out");
        String actual = runProgram("src/main/resources/programs/" + program + ".pas");

        assertEquals(expected.trim(), actual.trim(), "FOR loop did not execute as expected.");
    }

    //while loop
    @Test
    public void testWhileLoopExecution() throws Exception {
        String program = "while_loop_test";
        String expected = readFile("src/main/resources/outputs/" + program + ".out");
        String actual = runProgram("src/main/resources/programs/" + program + ".pas");

        assertEquals(expected.trim(), actual.trim(), "WHILE loop did not execute as expected.");
    }

    public static Stream<Arguments> testInterpreter() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", "arithmetic_operators.out"),
            Arguments.of("Boolean Operators", "boolean_operators.pas", "boolean_operators.out"),
            Arguments.of("Break Continue", "break_continue.pas", "break_continue.out"),
            Arguments.of("Case Statement", "case_statement.pas", "case_statement.out"),
            Arguments.of("Comparison Operators", "comparison_operators.pas", "comparison_operators.out"),
            Arguments.of("Enumerations", "enumerations.pas", "enumerations.out"),
            Arguments.of("For Loop", "for_loop_test.pas", "for_loop_test.out"),
            Arguments.of("Function Definition", "function_definition.pas", "function_definition.out"),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", "goto_statement_simple.out"),
            //Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", "goto_statement_complex.out"),
            Arguments.of("Hello World", "hello_world.pas", "hello_world.out"),
            Arguments.of("If Statement", "if_statement.pas", "if_statement.out"),
            Arguments.of("Loop Test", "loop_test.pas", "loop_test.out"), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", "nested_calculations.out"), 
            Arguments.of("Nested Classes", "nested_classes.pas", "nested_classes.out"), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", "procedure_definition.out"),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", "repetetive_statements.out"),
            Arguments.of("Return", "return.pas", "return.out"),
            Arguments.of("Simple Class", "simple_class.pas", "simple_class.out"), 
            Arguments.of("Simple Math", "simple_math.pas", "simple_math.out"),
            Arguments.of("While Loop", "while_loop_test.pas", "while_loop_test.out")
        );
    }
}
