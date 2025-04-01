package plp.group.Optimizer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi;
import plp.group.project.delphi_lexer;
import plp.group.project.delphi.ProgramContext;

/**
 * A simple test suite for the Optimizer. 
 * 
 * Ensures the optimization pass does not break any of the program's parse trees, and that optimizations are properly applied to various operators.
 */
public class OptimizerTest {
    
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
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("programs/" + programFileName))
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
        URI uri = classLoader.getResource("programs").toURI();
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
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("programs/boolean_operators.pas"))
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
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("programs/arithmetic_operators.pas"))
            );
            delphi parser = new delphi(new CommonTokenStream(lexer));
            ProgramContext tree = parser.program();

            // Optimize the tree
            String optimized = (new Optimizer()).visit(tree);

            String expected = "program Arithmetic_Operators ; begin writeln ( 7 ) ; writeln ( 15.5 ) ; writeln ( 'Hello World!' ) ; writeln ( 'Hello World<' ) ; writeln ( 'AB' ) ; writeln ( -1 ) ; writeln ( -1.5 ) ; writeln ( 12 ) ; writeln ( 13.5 ) ; writeln ( 2 ) ; writeln ( 4 ) ; writeln ( 0 ) ; writeln ( -5 ) ;  end . ";
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
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("programs/comparison_operators.pas"))
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
                CharStreams.fromStream(OptimizerTest.class.getClassLoader().getResourceAsStream("programs/nested_calculations.pas"))
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
