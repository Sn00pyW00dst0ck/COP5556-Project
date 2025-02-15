package plp.group.Interpreter.Types.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class StringTypeTest {

    @Test
    public void testConcatenation() {
        var s1 = new StringType("Hello");
        var s2 = new StringType(", World!");
        var c1 = new CharType(Character.valueOf('a'));

        var result1 = s1.applyOperation("+", s2);
        assertEquals(new StringType("Hello, World!"), result1);

        var result2 = s1.applyOperation("+", c1);
        assertEquals(new StringType("Helloa"), result2);

        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation("+", new BooleanType(Boolean.valueOf(false)));
        });
    }

    @Test
    public void testComparisons() {
        var s1 = new StringType("Hello");
        var s2 = new StringType(", World!");
        var c1 = new CharType(Character.valueOf('a'));

        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, s1.applyOperation("=", s1));
        assertEquals(F, s1.applyOperation("=", s2));
        assertEquals(F, s1.applyOperation("=", c1));

        assertEquals(F, s1.applyOperation("<>", s1));
        assertEquals(T, s1.applyOperation("<>", s2));
        assertEquals(T, s1.applyOperation("<>", c1));

        assertEquals(F, s1.applyOperation("<", s1));
        assertEquals(F, s1.applyOperation("<", s2));
        assertEquals(T, s1.applyOperation("<", c1));

        assertEquals(T, s1.applyOperation("<=", s1));
        assertEquals(F, s1.applyOperation("<=", s2));
        assertEquals(T, s1.applyOperation("<=", c1));

        assertEquals(F, s1.applyOperation(">", s1));
        assertEquals(T, s1.applyOperation(">", s2));
        assertEquals(F, s1.applyOperation(">", c1));

        assertEquals(T, s1.applyOperation(">=", s1));
        assertEquals(T, s1.applyOperation(">=", s2));
        assertEquals(F, s1.applyOperation(">=", c1));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation("=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation("<>", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation("<", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation("<=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation(">", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            s1.applyOperation(">=", new BooleanType(Boolean.valueOf(false)));
        });
    }

}
