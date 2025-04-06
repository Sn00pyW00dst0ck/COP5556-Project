package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.obermuhlner.math.big.BigDecimalMath;

public class EnvironmentTest {
    
    @ParameterizedTest
    @MethodSource({"provideWriteVariants", "provideWritelnVariants"})
    void testWriteVariants(String methodName, List<RuntimeValue> args, String expectedOutput) {
        Scope scope = Environment.scope();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            RuntimeValue.Method method = RuntimeValue.requireType(
                scope.lookup(methodName).orElseThrow(() -> new RuntimeException("Method not found: " + methodName)),
                RuntimeValue.Method.class
            );

            // NOTE, ensure that the args are wrapped yourself!
            method.invoke(scope, args);
            String output = out.toString();

            assertEquals(expectedOutput, output);
        } finally {
            System.setOut(originalOut);
            originalOut.flush();
        }
    }

    static Stream<Arguments> provideWriteVariants() {
        return Stream.of(
            // Check write/1 works as expected
            Arguments.of("write/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hello"))
            ), "Hello"),
            Arguments.of("write/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(new BigInteger("123")))
            ), "123"),
            Arguments.of("write/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(Boolean.valueOf(true)))
            ), "TRUE"),
            Arguments.of("write/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive('c'))
            ), "c"),
            Arguments.of("write/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(new BigDecimal("0.1")))
            ), " 1.000000000E-01"),
            // Check write/2 works as expected
            Arguments.of("write/2", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hi")),
                new RuntimeValue.Variable("y", new RuntimeValue.Primitive("Mom"))
            ), "HiMom"),
            // Check write/3 works as expected
            Arguments.of("write/3", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hi")),
                new RuntimeValue.Variable("y", new RuntimeValue.Primitive("Mom")),
                new RuntimeValue.Variable("z", new RuntimeValue.Primitive("!"))
            ), "HiMom!")
        );
    }

    static Stream<Arguments> provideWritelnVariants() {
        return Stream.of(
            // Check write/1 works as expected
            Arguments.of("writeln/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hello"))
            ), "Hello\n"),
            Arguments.of("writeln/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(new BigInteger("123")))
            ), "123\n"),
            Arguments.of("writeln/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(Boolean.valueOf(true)))
            ), "TRUE\n"),
            Arguments.of("writeln/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive('c'))
            ), "c\n"),
            Arguments.of("writeln/1", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive(new BigDecimal("0.1")))
            ), " 1.000000000E-01\n"),
            // Check write/2 works as expected
            Arguments.of("writeln/2", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hi")),
                new RuntimeValue.Variable("y", new RuntimeValue.Primitive("Mom"))
            ), "HiMom\n"),
            // Check write/3 works as expected
            Arguments.of("writeln/3", List.of(
                new RuntimeValue.Variable("x", new RuntimeValue.Primitive("Hi")),
                new RuntimeValue.Variable("y", new RuntimeValue.Primitive("Mom")),
                new RuntimeValue.Variable("z", new RuntimeValue.Primitive("!"))
            ), "HiMom!\n")
        );
    }

    @ParameterizedTest
    @MethodSource({"provideReadVariants", "provideReadlnVariants"})
    void testReadVariants(String methodName, String simulatedInput, List<String> expectedValues) throws IOException {    
        var terminal = TerminalBuilder.builder().streams(new ByteArrayInputStream(simulatedInput.getBytes()), System.out).build();
        var reader = LineReaderBuilder.builder().terminal(terminal).build();
        Environment.setReader(reader);
        
        Scope scope = Environment.scope();

        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        try {
            RuntimeValue.Method method = RuntimeValue.requireType(
                scope.lookup(methodName).orElseThrow(() -> new RuntimeException("Method not found: " + methodName)),
                RuntimeValue.Method.class
            );

            List<RuntimeValue.Variable> vars = new ArrayList<>();
            List<RuntimeValue> refs = new ArrayList<>();
            for (int i = 0; i < expectedValues.size(); i++) {
                RuntimeValue.Variable var = new RuntimeValue.Variable("v" + i, new RuntimeValue.Primitive(null));
                vars.add(var);
                refs.add(new RuntimeValue.Reference("v" + i, var));
            }

            Scope methodScope = new Scope(Optional.empty());
            method.invoke(methodScope, refs);

            for (int i = 0; i < expectedValues.size(); i++) {
                String actual = vars.get(i).value().getPrintString();
                assertEquals(expectedValues.get(i), actual, "Value mismatch at index " + i);
            }    

        } finally {
            System.setIn(originalIn);
        }
    }

    static Stream<Arguments> provideReadVariants() {
        return Stream.of(
            Arguments.of("read/X", "123 456\n", List.of("123", "456")),
            Arguments.of("read/X", "42\n", List.of("42")),
            Arguments.of("read/X", "100 200 300\n", List.of("100", "200", "300")),
            Arguments.of("read/X", "100 200 300 400\n", List.of("100", "200", "300", "400"))
        );
    }

    static Stream<Arguments> provideReadlnVariants() {
        return Stream.of(
            Arguments.of("readln/X", "123 456\n", List.of("123", "456")),
            Arguments.of("readln/X", "42\n", List.of("42")),
            Arguments.of("readln/X", "100 200 300\n", List.of("100", "200", "300")),
            Arguments.of("readln/X", "100 200 300 400\n", List.of("100", "200", "300", "400"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideMathematicalFunctionVariants")
    void testMathematicalFunctionVariants(String methodName, List<Object> inputs, Object expectedOutput) {
        Scope scope = Environment.scope();

        assertDoesNotThrow(() -> {
            RuntimeValue.Method method = RuntimeValue.requireType(
                scope.lookup(methodName).orElseThrow(() -> new RuntimeException("Method not found: " + methodName)),
                RuntimeValue.Method.class
            );

            List<RuntimeValue> args = inputs.stream()
                .map(val -> (RuntimeValue) new RuntimeValue.Variable("arg", new RuntimeValue.Primitive(val)))
                .toList();
    
            RuntimeValue result = method.invoke(new Scope(Optional.empty()), args);
            assertTrue(result instanceof RuntimeValue.Primitive);

            Object actual = ((RuntimeValue.Primitive) result).value();
            assertEquals(expectedOutput, actual);
        });
    }

    static Stream<Arguments> provideMathematicalFunctionVariants() {
        return Stream.of(
            Arguments.of("arctan/1", List.of(new BigInteger("0")), BigDecimalMath.atan(new BigDecimal("0.0"), new MathContext(20))),
            Arguments.of("arctan/1", List.of(new BigDecimal("4.4")), BigDecimalMath.atan(new BigDecimal("4.4"), new MathContext(20))),
            Arguments.of("arctan/1", List.of(new BigDecimal("-4.4")), BigDecimalMath.atan(new BigDecimal("-4.4"), new MathContext(20))),
            Arguments.of("chr/1", List.of(new BigInteger("50")), Character.valueOf('2')),
            Arguments.of("chr/1", List.of(new BigInteger("97")), Character.valueOf('a')),
            Arguments.of("chr/1", List.of(new BigInteger("33")), Character.valueOf('!')),
            Arguments.of("chr/1", List.of(new BigInteger("90")), Character.valueOf('Z')),
            Arguments.of("cos/1", List.of(new BigDecimal("1.0")), new BigDecimal("0.54030230586813971740")),
            Arguments.of("cos/1", List.of(new BigDecimal("0.0")), new BigDecimal("1.000000")),
            Arguments.of("exp/1", List.of(new BigInteger("4")), BigDecimalMath.exp(new BigDecimal("4.0"), new MathContext(20))),
            Arguments.of("exp/1", List.of(new BigDecimal("-3.5")), BigDecimalMath.exp(new BigDecimal("-3.5"), new MathContext(20))),
            Arguments.of("exp/1", List.of(new BigDecimal("10.5")), BigDecimalMath.exp(new BigDecimal("10.5"), new MathContext(20))),
            Arguments.of("ln/1", List.of(new BigInteger("1")), new BigDecimal("0")),
            Arguments.of("ln/1", List.of(new BigDecimal("4.4")), BigDecimalMath.log(new BigDecimal("4.4"), new MathContext(20))),
            Arguments.of("ln/1", List.of(new BigInteger("5")), BigDecimalMath.log(new BigDecimal("5.0"), new MathContext(20))),
            Arguments.of("odd/1", List.of(new BigInteger("10")), Boolean.valueOf(false)),
            Arguments.of("odd/1", List.of(new BigInteger("-1")), Boolean.valueOf(true)),
            Arguments.of("odd/1", List.of(new BigInteger("5")), Boolean.valueOf(true)),
            Arguments.of("pi/0", List.of(), BigDecimalMath.pi(new MathContext(20)).setScale(20)),
            Arguments.of("round/1", List.of(new BigDecimal("2.5")), new BigInteger("2")),
            Arguments.of("round/1", List.of(new BigDecimal("-3.4")), new BigInteger("-3")),
            Arguments.of("round/1", List.of(new BigDecimal("-3.5")), new BigInteger("-4")),
            Arguments.of("sin/1", List.of(new BigDecimal("1.0")), new BigDecimal("0.84147098480789650665")),
            Arguments.of("sin/1", List.of(new BigDecimal("0.0")), new BigDecimal("0.000")),
            Arguments.of("sqrt/1", List.of(new BigDecimal("4")), new BigDecimal("2.0")),
            Arguments.of("sqrt/1", List.of(new BigDecimal("3.7")), BigDecimalMath.sqrt(new BigDecimal("3.7"), new MathContext(20))),
            Arguments.of("trunc/1", List.of(new BigDecimal("3.7")), new BigInteger("3")),
            Arguments.of("trunc/1", List.of(new BigDecimal("-13.1")), new BigInteger("-13"))
        );
    }

}
