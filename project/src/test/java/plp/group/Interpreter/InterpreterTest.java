package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.project.delphiLexer;
import plp.group.project.delphiParser;

public class InterpreterTest {
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	@BeforeEach
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@AfterEach
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@ParameterizedTest
	@MethodSource
	void testInterpreter(String testName, String programFileName, String expectedWrittenOutput) throws IOException {
		var lexer = new delphiLexer(
				CharStreams.fromStream(InterpreterTest.class.getClassLoader()
						.getResourceAsStream(programFileName)));
		var tokens = new CommonTokenStream(lexer);
		var parser = new delphiParser(tokens);
		var tree = parser.program();

		var interpreter = new Interpreter(); // TODO: Add way to specify output stream for testing purposes
		interpreter.visit(tree);

		assertEquals(expectedWrittenOutput, outContent.toString());
	}

	// Test cases created using:
	// https://www.onlinegdb.com/online_pascal_compiler
	// https://www.tutorialspoint.com/compile_pascal_online.php

	public static Stream<Arguments> testInterpreter() {
		return Stream.of(
				Arguments.of("Hello World", "hello_world.pas", "Hello, world.\n"),
				// TODO: ensure that type is carried in output (IE these are reals so should
				// have decimals)...
				Arguments.of("Simple Math", "simple_math.pas", """
						2 + 3 = 5
						2 - 3 = -1
						2 * 3 = 6
						2 / 3 = 0
						"""),
				Arguments.of("Boolean Operators", "boolean_operators.pas", """
						FALSE
						FALSE
						FALSE
						TRUE
						FALSE
						TRUE
						TRUE
						TRUE
						FALSE
						TRUE
						"""),
				Arguments.of("Comparison Operators", "comparison_operators.pas", """
						TRUE
						FALSE
						TRUE
						TRUE
						TRUE
						FALSE
						TRUE
						TRUE
						TRUE
						TRUE
						FALSE
						TRUE
						FALSE
						TRUE
						TRUE
						FALSE
						TRUE
						"""),
				Arguments.of("Arithmetic Operators", "arithmetic_operators.pas", """
						7
						 1.550000000E+01
						Hello World!
						Hello World<
						AB
						-1
						-1.500000000E+00
						12
						 1.350000000E+01
						2
						 4.000000000E+00
						0
						-5
						"""),
				Arguments.of("Enumerations", "enumerations.pas", """
						GREEN
						Sat
						"""),
				Arguments.of("Subranges", "subranges.pas", """
						5
						b
						Mon
						"""),
				Arguments.of("If Statement", "if_statement.pas", """
						a is not less than 20
						value of a is : 100
						"""),
				Arguments.of("Procedure Definition", "procedure_definition.pas", """
						Winthin the program
						value of a = 100 b =  200 and c = 300
						Winthin the procedure display
						value of a = 10 b =  20 and c = 30
						Winthin the procedure parameter_display
						value of a = 15 b =  45 and c = 60
						"""),
				Arguments.of("Function Definition", "function_definition.pas", """
						127
						Hello from GetMessage
						"""),
				Arguments.of("Repetetive Statements", "repetetive_statements.pas", """
						While Statement
						1 4 9 16 25 36 49 64 81 100\s
						Repeat Statement:
						1 4 9 16 25 36 49 64 81 100\s
						For Statement (to variant):
						1 2 3 4 5 6 7 8 9 10\s
						For Statement (down to variant):
						10 9 8 7 6 5 4 3 2 1\s
						Loop over chars
						ABCDEFGHIJKLMNOPQRSTUVWXYZ
						zyxwvutsrqponmlkjihgfedcba
						Loop over ENUMS
						ONE TWO THREE FOUR FIVE SIX SEVEN EIGHT NINE TEN JACK QUEEN KING ACE\s
						KING QUEEN JACK\s
						"""),
				Arguments.of("Case Statement", "case_statement.pas", """
						The color is Green
						17 mod 2 = 1
						17 is either 17 or 0
						"""));
	}
}
