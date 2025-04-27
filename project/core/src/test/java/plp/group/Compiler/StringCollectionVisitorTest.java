package plp.group.Compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphi_lexer;
import plp.group.project.delphi;
import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.Compiler.visitors.StringCollectionVisitor;

public class StringCollectionVisitorTest {

    @ParameterizedTest
    @MethodSource
    void testStringCollection(String testName, String programFile, int expectedStringCount) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram, StandardCharsets.UTF_8));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        ASTBuilder builder = new ASTBuilder();
        AST program = (AST) builder.visit(parser.program());

        CompilerContext context = new CompilerContext();
        StringCollectionVisitor visitor = new StringCollectionVisitor(context);

        // Visit the AST
        visitor.visit(program);
 
        // Get collected strings from symbol table
        Map<String, LLVMValue> collectedStrings = context.symbolTable.getEntriesOfType(LLVMValue.String.class, true);

        assertEquals(expectedStringCount, collectedStrings.size(), 
            "Program '" + programFile + "' should collect " + expectedStringCount + " unique strings.");
        for (LLVMValue value : collectedStrings.values()) {
            assertTrue(((LLVMValue.String) value).name().startsWith("@llvm.str."), 
                "LLVM global string name should start with '@llvm.str.'");
        }
    }
 
    public static Stream<Arguments> testStringCollection() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", 2),
            Arguments.of("Boolean Operators", "boolean_operators.pas", 0),
            Arguments.of("Case Statement", "case_statement.pas", 9),
            Arguments.of("Comparison Operators", "comparison_operators.pas", 2),
            Arguments.of("Enumerations", "enumerations.pas", 0),
            Arguments.of("Function Definition", "function_definition.pas", 1),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", 0),
            Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", 5),
            Arguments.of("Hello World", "hello_world.pas", 1),
            Arguments.of("If Statement", "if_statement.pas", 3),
            Arguments.of("Loop Test", "loop_test.pas", 2), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", 2), 
            Arguments.of("Nested Classes", "nested_classes.pas", 2), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", 7),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", 7),
            Arguments.of("Simple Class", "simple_class.pas", 3), 
            Arguments.of("Simple Math", "simple_math.pas", 4)
        );
    }
}
