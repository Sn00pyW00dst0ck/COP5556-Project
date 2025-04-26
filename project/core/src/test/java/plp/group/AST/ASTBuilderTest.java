package plp.group.AST;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

/**
 * Tests for the AST Builder.
 * 
 * TODO: figure out how to test individual parts for the structure being correct...
 */
public class ASTBuilderTest {

    /**
     * Ensures all the valid programs in the resources folder do turn into ASTs without error.
     * @throws IOException 
     */
    @ParameterizedTest
    @MethodSource
    void testValidProgramsTurnToAST(String testName, String programFile) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        ASTBuilder builder = new ASTBuilder();
        
        assertDoesNotThrow(() -> builder.visit(parser.program()));
    }

    public static Stream<Arguments> testValidProgramsTurnToAST() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas"),
            Arguments.of("Boolean Operators", "boolean_operators.pas"),
            Arguments.of("Break Continue", "break_continue.pas"),
            Arguments.of("Case Statement", "case_statement.pas"),
            Arguments.of("Comparison Operators", "comparison_operators.pas"),
            Arguments.of("Echo", "echo.pas"),
            Arguments.of("Enumerations", "enumerations.pas"),
            Arguments.of("Function Definition", "function_definition.pas"),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas"),
            Arguments.of("Goto Statement Complex", "goto_statement_complex.pas"),
            Arguments.of("Hello World", "hello_world.pas"),
            Arguments.of("If Statement", "if_statement.pas"),
            Arguments.of("Loop Test", "loop_test.pas"), 
            Arguments.of("Nested Calculations", "nested_calculations.pas"), 
            Arguments.of("Nested Classes", "nested_classes.pas"), 
            Arguments.of("Procedure Definition", "procedure_definition.pas"),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas"),
            Arguments.of("Return", "return.pas"),
            Arguments.of("Simple Class", "simple_class.pas"), 
            Arguments.of("Simple Math", "simple_math.pas")
        );
    }    
}
