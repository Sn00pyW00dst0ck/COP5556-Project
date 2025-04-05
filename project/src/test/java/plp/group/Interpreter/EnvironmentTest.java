package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
    // TODO: test other built in functions.
}
