package plp.group.Optimizer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi;
import plp.group.project.delphi_lexer;
import plp.group.project.delphi.ProgramContext;

public class OptimizerTest {
    
    /**
     * This test optimizes each of the sample programs, and ensures they are still valid after optimizations.
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

    // TODO: ensure that correct optimizations are applied. 

}
