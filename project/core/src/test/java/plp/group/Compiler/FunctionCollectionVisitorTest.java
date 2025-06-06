package plp.group.Compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.AST.AST;
import plp.group.AST.ASTBuilder;
import plp.group.Compiler.visitors.FunctionCollectionVisitor;
import plp.group.project.delphi;
import plp.group.project.delphi_lexer;

public class FunctionCollectionVisitorTest {

    @ParameterizedTest
    @MethodSource
    void testGetsCorrectFunctions(String testName, String programFile, List<LLVMValue.LLVMFunction.UserFunction> functions) throws IOException {
        InputStream inputProgram = getClass().getClassLoader().getResourceAsStream("compiler_tests/programs/" + programFile);
        delphi_lexer lexer = new delphi_lexer(CharStreams.fromStream(inputProgram));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphi parser = new delphi(tokens);

        ASTBuilder builder = new ASTBuilder();
        AST ast = (AST) builder.visit(parser.program());

        CompilerContext context = new CompilerContext();
        new FunctionCollectionVisitor(context).visit(ast);
        
        functions.forEach((function) -> {
            context.symbolTable.lookup(function.name(), false).ifPresent((value) -> {
                var received = (LLVMValue.LLVMFunction.UserFunction) context.symbolTable.lookup(function.name(), false).get();
                assertEquals(function.name(), received.name());
                assertEquals(function.paramTypes(), received.paramTypes());
                assertEquals(function.returnType(), received.returnType());
            });
        });
    }

    public static Stream<Arguments> testGetsCorrectFunctions() {
        return Stream.of(
            Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", List.of()),
            Arguments.of("Boolean Operators", "boolean_operators.pas", List.of()),
            Arguments.of("Break Continue", "break_continue.pas", List.of()),
            Arguments.of("Comparison Operators", "comparison_operators.pas", List.of()),
            Arguments.of("Function Definition", "function_definition.pas", List.of(
                new LLVMValue.LLVMFunction.UserFunction("Sum", "i32", List.of("x", "y", "z"), List.of("i32", "i32", "i32"), Optional.empty()),
                new LLVMValue.LLVMFunction.UserFunction("GetMessage", "ptr", List.of(), List.of(), Optional.empty()),
                new LLVMValue.LLVMFunction.UserFunction("SumByRef", "i32", List.of("x", "y", "z", "m"), List.of("i32", "i32", "i32", "i32"), Optional.empty()) // TODO: figure out pass by ref
            )),
            Arguments.of("Goto Statement Simple", "goto_statement_simple.pas", List.of()),
            Arguments.of("Goto Statement Complex", "goto_statement_complex.pas", List.of()),
            Arguments.of("Hello World", "hello_world.pas", List.of()),
            Arguments.of("If Statement", "if_statement.pas", List.of()),
            Arguments.of("Loop Test", "loop_test.pas", List.of()), 
            Arguments.of("Nested Calculations", "nested_calculations.pas", List.of()), 
            Arguments.of("Procedure Definition", "procedure_definition.pas", List.of(
                new LLVMValue.LLVMFunction.UserFunction("display", "void", List.of(), List.of(), Optional.empty()),
                new LLVMValue.LLVMFunction.UserFunction("parameter_display", "void", List.of("x", "y"), List.of("i32", "i32"), Optional.empty()),
                new LLVMValue.LLVMFunction.UserFunction("SumByRef", "void", List.of("a", "b", "c", "m"), List.of("i32", "i32", "i32", "i32"), Optional.empty()) // TODO: figure out reference types
            )),
            Arguments.of("Repetitive Statements", "repetetive_statements.pas", List.of()),
            Arguments.of("Simple Math", "simple_math.pas", List.of())
        );
    }
}
