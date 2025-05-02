package plp.group.Compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi_lexer;
import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.project.delphi;

public class CompilerUnitTest {
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
    void testCompiler(String testName, String programFile, String outputFile) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("compiler_tests/programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);
        var tree = parser.program();

        InputStream expectedOutputStream = getClass().getClassLoader().getResourceAsStream("compiler_tests/outputs/" + outputFile);
        String expectedOutput = new String(expectedOutputStream.readAllBytes(), StandardCharsets.UTF_8);

        ASTBuilder builder = new ASTBuilder();
        AST.Program AST = (AST.Program) builder.visit(tree);
        System.out.println((new CompilerContext()).compileToLLVMIR(AST));

        assertEquals(expectedOutput, outContent.toString());
    }

    public static Stream<Arguments> testCompiler() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", "arithmetic_operators.ll"),
            Arguments.of("Boolean Operators", "boolean_operators.pas", "boolean_operators.ll"),
            Arguments.of("Case Statement", "case_statement.pas", "case_statement.ll"),
            Arguments.of("Comparison Operators", "comparison_operators.pas", "comparison_operators.ll"),
            Arguments.of("For Loop", "for_loop_test.pas", "for_loop_test.ll"),
            Arguments.of("Function Definition", "function_definition.pas", "function_definition.ll"),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", "goto_statement_simple.ll"),
            Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", "goto_statement_complex.ll"),
            Arguments.of("Hello World", "hello_world.pas", "hello_world.ll"),
            Arguments.of("If Statement", "if_statement.pas", "if_statement.ll"),
            Arguments.of("Loop Test", "loop_test.pas", "loop_test.ll"), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", "nested_calculations.ll"), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", "procedure_definition.ll"),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", "repetetive_statements.ll"),
            Arguments.of("Simple Math", "simple_math.pas", "simple_math.ll"),
            Arguments.of("While Loop", "while_loop_test.pas", "while_loop_test.ll")
        );
    }
}
