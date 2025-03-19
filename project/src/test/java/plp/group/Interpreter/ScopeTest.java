package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Scope class.
 */
public class ScopeTest {

    @Test
    void testUndefinedVariableLookup() {
        Scope scope = new Scope(Optional.empty());
        assertTrue(scope.lookup("undefined").isEmpty());
    }

    @Test
    void testDefinedVariableLookup() {
        Scope scope = new Scope(Optional.empty());
        scope.define("x", new RuntimeValue.Primitive(new BigInteger("5")));
        assertEquals(Optional.of(new RuntimeValue.Primitive(new BigInteger("5"))), scope.lookup("x"));
    }

    @Test
    void testUndefinedVariableAssignment() {
        Scope scope = new Scope(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scope.assign("x", new RuntimeValue.Primitive(new BigInteger("5")));
        });
        assertEquals("Attempting to assign a value to name 'x' not in scope!", exception.getMessage());
    }

    @Test
    void testDefinedVariableAssignment() {
        Scope scope = new Scope(Optional.empty());
        scope.define("x", new RuntimeValue.Primitive(new BigInteger("5")));
        scope.assign("x", new RuntimeValue.Primitive(new BigInteger("10")));
        assertEquals(Optional.of(new RuntimeValue.Primitive(new BigInteger("10"))), scope.lookup("x"));
        scope.assign("x", new RuntimeValue.Primitive("Hello World"));
        assertEquals(Optional.of(new RuntimeValue.Primitive("Hello World")), scope.lookup("x"));
    }

    @Test
    void testUndefinedVariableDefinition() {
        Scope scope = new Scope(Optional.empty());
        assertDoesNotThrow(() -> {
            scope.define("x", new RuntimeValue.Primitive(new BigInteger("5")));
        });
    }

    @Test
    void testDefinedVariableDefinition() {
        Scope scope = new Scope(Optional.empty());
        scope.define("x", new RuntimeValue.Primitive(new BigInteger("5")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scope.define("x", new RuntimeValue.Primitive(new BigInteger("25")));
        });
        assertEquals("Attempting to redefine name 'x' in scope!", exception.getMessage());
    }

    @Test
    void testNestedScopes() {
        Scope globalScope = new Scope(Optional.empty());
        globalScope.define("x", new RuntimeValue.Primitive("Global Variable"));
        globalScope.define("y", new RuntimeValue.Primitive(new BigInteger("5")));

        Scope mainScope = new Scope(Optional.of(globalScope));
        mainScope.define("x", new RuntimeValue.Primitive("Overridden Variable"));
        mainScope.define("z", new RuntimeValue.Primitive("Local Variable"));

        Scope secondaryScope = new Scope(Optional.of(globalScope));
        secondaryScope.define("var", new RuntimeValue.Primitive("My variable!"));

        // Assert we can define y in the mainScope, then check mainScope has correct values from lookup
        assertDoesNotThrow(() -> {
            mainScope.define("y", new RuntimeValue.Primitive(new BigInteger("25")));
        });
        assertEquals(Optional.of(new RuntimeValue.Primitive("Overridden Variable")), mainScope.lookup("x"));
        assertEquals(Optional.of(new RuntimeValue.Primitive(new BigInteger("25"))), mainScope.lookup("y"));
        assertEquals(Optional.of(new RuntimeValue.Primitive("Local Variable")), mainScope.lookup("z"));

        // Assert we can overwrite a thing in globalScope from secondaryScope, and that all lookups are correct afterwards
        assertDoesNotThrow(() -> {
            secondaryScope.assign("x", new RuntimeValue.Primitive("New Global Variable Value!"));
        });
        assertEquals(Optional.of(new RuntimeValue.Primitive("My variable!")), secondaryScope.lookup("var"));
        assertEquals(Optional.of(new RuntimeValue.Primitive("New Global Variable Value!")), secondaryScope.lookup("x"));
        assertEquals(Optional.of(new RuntimeValue.Primitive(new BigInteger("5"))), secondaryScope.lookup("y"));

        assertEquals(Optional.of(new RuntimeValue.Primitive("New Global Variable Value!")), globalScope.lookup("x"));
        assertEquals(Optional.of(new RuntimeValue.Primitive("Overridden Variable")), mainScope.lookup("x"));
    }

}
