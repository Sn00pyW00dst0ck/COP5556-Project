package plp.group.Compiler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

public class FunctionCollectionVisitorTest {
    @ParameterizedTest
    @MethodSource
    void testGetsCorrectFunctions(String testName, String programFile, List<LLVMValue.Function> functions) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        ASTBuilder builder = new ASTBuilder();
        AST ast = (AST) builder.visit(parser.program());

        CompilerContext context = new CompilerContext();
        new FunctionCollectionVisitor(context).visit(ast);
        
        functions.forEach((function) -> {
            assertEquals(function, (LLVMValue.Function) context.symbolTable.lookup(function.name(), false).get());
        });
    }

    public static Stream<Arguments> testGetsCorrectFunctions() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", List.of()),
            Arguments.of("Boolean Operators", "boolean_operators.pas", List.of()),
            Arguments.of("Break Continue", "break_continue.pas", List.of()),
            Arguments.of("Case Statement", "case_statement.pas", List.of(
                // TODO: figure out custom types
                new LLVMValue.Function("ShowColor", "void", List.of("TColor"))
            )),
            Arguments.of("Comparison Operators", "comparison_operators.pas", List.of()),
            Arguments.of("Echo", "echo.pas", List.of()),
            Arguments.of("Enumerations", "enumerations.pas", List.of()),
            Arguments.of("Function Definition", "function_definition.pas", List.of(
                new LLVMValue.Function("Sum", "i32", List.of("i32", "i32", "i32")),
                new LLVMValue.Function("GetMessage", "i8*", List.of()),
                new LLVMValue.Function("SumByRef", "i32", List.of("i32", "i32", "i32", "i32")) // TODO: figure out pass by ref
            )),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", List.of()),
            Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", List.of()),
            Arguments.of("Hello World", "hello_world.pas", List.of()),
            Arguments.of("If Statement", "if_statement.pas", List.of()),
            Arguments.of("Loop Test", "loop_test.pas", List.of()), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", List.of()), 
            Arguments.of("Nested Classes", "nested_classes.pas", List.of(
                // TODO: figure out custom types
                new LLVMValue.Function("TTeacher.Create", "TTeacher", List.of("TTeacher", "i8*")), // TODO: figure out 'this' and figure out ref
                new LLVMValue.Function("TStudent.Create", "TTeacher", List.of("TTeacher", "i8*", "TTeacher")) // TODO: figure out 'this' and figure out ref
            )), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", List.of(
                new LLVMValue.Function("display", "void", List.of()),
                new LLVMValue.Function("parameter_display", "void", List.of("i32", "i32")),
                new LLVMValue.Function("SumByRef", "void", List.of("i32", "i32", "i32", "i32")) // TODO: figure out reference types
            )),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", List.of()),
            Arguments.of("Return", "return.pas", List.of(
                new LLVMValue.Function("Sum", "i32", List.of("i32", "i32", "i32"))
            )),
            Arguments.of("Simple Class", "simple_class.pas", List.of(
                // TODO: implicit 'this' - figure out class types
                new LLVMValue.Function("TPerson.Create", "TPerson", List.of("TPerson")),
                new LLVMValue.Function("TPerson.greet", "void", List.of("TPerson")),
                new LLVMValue.Function("TPerson.getAge", "i32", List.of("TPerson"))
            )),
            Arguments.of("Simple Math", "simple_math.pas", List.of())
        );
    }
}