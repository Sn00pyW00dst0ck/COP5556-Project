package plp.group.Optimizer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.Interpreter.Interpreter;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;
import plp.group.project.delphi.ProgramContext;

/**
 * A simple test suite for the Optimizer. 
 * 
 * Ensures the optimization pass does not break any of the program's parse trees, and that optimizations are properly applied to various operators.
 */
public class OptimizerTest {
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
    @MethodSource("testOptimizedInterpreter")
    void testOptimizedInterpreter(String testName, String programFile, String outputFile) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("interpreter_tests/programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        String optimized = (new Optimizer()).visit(parser.program());
        parser = new delphi(new CommonTokenStream(new delphi_lexer(CharStreams.fromString(optimized))));

        Interpreter interpreter = new Interpreter(null);
        interpreter.visit(parser.program());

        InputStream expectedOutputStream = getClass().getClassLoader().getResourceAsStream("interpreter_tests/outputs/" + outputFile);
        String expectedOutput = new String(expectedOutputStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expectedOutput, outContent.toString());
    }

    public static Stream<Arguments> testOptimizedInterpreter() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", "arithmetic_operators.out"),
            Arguments.of("Boolean Operators", "boolean_operators.pas", "boolean_operators.out"),
            Arguments.of("Break Continue", "break_continue.pas", "break_continue.out"),
            Arguments.of("Case Statement", "case_statement.pas", "case_statement.out"),
            Arguments.of("Comparison Operators", "comparison_operators.pas", "comparison_operators.out"),
            Arguments.of("Enumerations", "enumerations.pas", "enumerations.out"),
            Arguments.of("Function Definition", "function_definition.pas", "function_definition.out"),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", "goto_statement_simple.out"),
            // Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", "goto_statement_complex.out"),
            Arguments.of("Hello World", "hello_world.pas", "hello_world.out"),
            Arguments.of("If Statement", "if_statement.pas", "if_statement.out"),
            Arguments.of("Loop Test", "loop_test.pas", "loop_test.out"), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", "nested_calculations.out"), 
            Arguments.of("Nested Classes", "nested_classes.pas", "nested_classes.out"), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", "procedure_definition.out"),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", "repetetive_statements.out"),
            Arguments.of("Return", "return.pas", "return.out"),
            Arguments.of("Simple Class", "simple_class.pas", "simple_class.out"), 
            Arguments.of("Simple Math", "simple_math.pas", "simple_math.out")
        );
    }

    /**
    * This test optimizes each of the sample programs, and ensures they are still valid programs after optimizations.
     * 
     * It runs against every one of the `.pas` file programs within the `resources/programs` directory.
     */
    @ParameterizedTest
    @MethodSource("testOptimizationOutputIsValidParseTree")
    void testOptimizationOutputIsValidParseTree(String programFileName) {
        assertDoesNotThrow(() -> {
            // Parse the original file
            delphi_lexer lexer = new delphi_lexer(
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("interpreter_tests/programs/" + programFileName))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            // Create new lexer and parser for optimized code
            delphi_lexer optimizedLexer = new delphi_lexer(CharStreams.fromString(optimized));
            delphi optimizedParser = new delphi(new CommonTokenStream(optimizedLexer));
            
            // Attach error listener to detect syntax warnings/errors
            // Based on:  https://stackoverflow.com/questions/30700576/antlr4-unexpected-behavior-that-i-cant-understand/30701061#30701061
            List<String> syntaxErrors = new ArrayList<>();
            optimizedParser.removeErrorListeners();
            optimizedParser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    syntaxErrors.add("Line " + line + ":" + charPositionInLine + " " + msg);
                }
            });
        
            // Parse the optimized code
            optimizedParser.program();
        
            // Ensure there were no syntax errors
            assertTrue(syntaxErrors.isEmpty(), "ANTLR warnings/errors found: " + syntaxErrors);
        });
    }

    public static Stream<Arguments> testOptimizationOutputIsValidParseTree() throws IOException, URISyntaxException {
        ClassLoader classLoader = OptimizerTest.class.getClassLoader();
        URI uri = classLoader.getResource("interpreter_tests/programs").toURI();
        if (uri == null) {
            throw new IllegalStateException("Resource folder 'programs' not found!");
        }
    
        return Files.list(Paths.get(uri))
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.endsWith(".pas")) // Filter only Pascal files
                .map(Arguments::of); // Convert to Arguments
    }

    /**
     * Ensure the optimizer can correctly optimize boolean operations.
     */
    @Test
    void testOptimizesBooleanOperators() {
        assertDoesNotThrow(() -> {
            // Parse the original file
            delphi_lexer lexer = new delphi_lexer(
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("interpreter_tests/programs/boolean_operators.pas"))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            String expected = "program Boolean_Operators ; begin writeln ( false ) ; writeln ( false ) ; writeln ( false ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ; writeln ( true ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ;  end . ";
            assertEquals(expected, optimized);
        });
    }

    /**
     * Ensure the optimizer can correctly optimize arithmetic operations.
     */
    @Test
    void testOptimizesArithmeticOperators() {
        assertDoesNotThrow(() -> {
            // Parse the original file
            delphi_lexer lexer = new delphi_lexer(
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("interpreter_tests/programs/arithmetic_operators.pas"))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            String expected = "program Arithmetic_Operators ; begin writeln ( 7 ) ; writeln ( 15.5 ) ; writeln ( 'Hello World!' ) ; writeln ( 'Hello World<' ) ; writeln ( 'AB' ) ; writeln ( -1 ) ; writeln ( -1.5 ) ; writeln ( 12 ) ; writeln ( 13.5 ) ; writeln ( 2 ) ; writeln ( 4.0 ) ; writeln ( 0 ) ; writeln ( -5 ) ;  end . ";
            assertEquals(expected, optimized);
        });
    }

    /**
     * Ensure the optimizer can correctly optimize comparison operations.
     */
    @Test
    void testOptimizesComparisonOperators() {
        assertDoesNotThrow(() -> {
            // Parse the original file
            delphi_lexer lexer = new delphi_lexer(
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("interpreter_tests/programs/comparison_operators.pas"))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            String expected = "program Comparison_Operators ; begin writeln ( true ) ; writeln ( false ) ; writeln ( true ) ; writeln ( true ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ; writeln ( true ) ; writeln ( true ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ; writeln ( true ) ; writeln ( false ) ; writeln ( true ) ;  end . ";
            assertEquals(expected, optimized);
        });
    }

    /**
     * Ensure the optimizer can correctly optimize nested calculations (calculations depending on other ones).
     */
    @Test
    void testOptimizesNestedCalculations() {
        assertDoesNotThrow(() -> {
            // Parse the original file
            delphi_lexer lexer = new delphi_lexer(
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("interpreter_tests/programs/nested_calculations.pas"))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            String expected = "program Nested_Calculations ; begin writeln ( 12.0 ) ; writeln ( 16 ) ; writeln ( 'Hello WorldB!!!' ) ; writeln ( true ) ; writeln ( 14 ) ; writeln ( 5.5 ) ;  end . ";
            assertEquals(expected, optimized);
        });
    }

}
