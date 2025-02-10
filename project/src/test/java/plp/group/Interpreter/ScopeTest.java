package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.*;

import java.util.stream.Stream;

public class ScopeTest {

    @ParameterizedTest
    @MethodSource
    void testGets(String testName, String name, Object expected, boolean success) {
        Scope parentScope = new Scope(null);
        parentScope.define("in_parent", true);
        Scope testScope = new Scope(parentScope);
        testScope.define("x", 1);
        testScope.define("y", 2);

        if (success) {
            Assertions.assertEquals(expected, testScope.get(name));
        } else {
            assertThrows(RuntimeException.class, () -> testScope.get(name));
        }
    }

    public static Stream<Arguments> testGets() {
        return Stream.of(
                Arguments.of("Valid Get 1", "x", 1, true),
                Arguments.of("Valid Get 2", "y", 2, true),
                Arguments.of("Get from parent", "in_parent", true, true),
                Arguments.of("Get on not defined name", "z", null, false));
    }

    @ParameterizedTest
    @MethodSource
    void testAssigns(String testName, String name, Object value, boolean success) {
        Scope parentScope = new Scope(null);
        parentScope.define("in_parent", true);
        Scope testScope = new Scope(parentScope);
        testScope.define("x", 1);
        testScope.define("y", 2);

        if (success) {
            assertDoesNotThrow(() -> testScope.assign(name, value));
        } else {
            assertThrows(RuntimeException.class, () -> testScope.assign(name, value));
        }
    }

    public static Stream<Arguments> testAssigns() {
        return Stream.of(
                Arguments.of("Valid Get 1", "x", 4, true),
                Arguments.of("Valid Get 2", "y", 8, true),
                Arguments.of("Assign in parent", "in_parent", false, true),
                Arguments.of("Assign on not defined name", "z", null, false));
    }

    @Test
    void testMultipleOperations() {
        Scope parentScope = new Scope(null);
        parentScope.define("in_parent", true);
        Scope testScope = new Scope(parentScope);
        testScope.define("x", 1);
        testScope.define("y", 2);

        Assertions.assertEquals(2, testScope.get("y"));
        Assertions.assertEquals(true, testScope.get("in_parent"));

        testScope.assign("in_parent", "Updated Value");
        Assertions.assertEquals("Updated Value", testScope.get("in_parent"));

        assertThrows(RuntimeException.class, () -> testScope.assign("undefined", 10));
        testScope.define("undefined", 10);
        assertDoesNotThrow(() -> testScope.assign("undefined", 10));
    }
}
