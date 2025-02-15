package plp.group.Interpreter.Types.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CharTypeTest {

    @Test
    public void testConcatenation() {
        var s1 = new StringType("Hello");
        var c1 = new CharType(Character.valueOf('a'));
        var c2 = new CharType(Character.valueOf('b'));

        var result1 = c1.applyOperation("+", c2);
        assertEquals(new StringType("ab"), result1);

        var result2 = c1.applyOperation("+", s1);
        assertEquals(new StringType("aHello"), result2);

        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation("+", new BooleanType(Boolean.valueOf(false)));
        });
    }

    @Test
    public void testComparisons() {
        var c1 = new CharType(Character.valueOf('a'));
        var c2 = new CharType(Character.valueOf('b'));
        var s1 = new StringType("Hello");

        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, c1.applyOperation("=", c1));
        assertEquals(F, c1.applyOperation("=", c2));
        assertEquals(F, c1.applyOperation("=", s1));

        assertEquals(F, c1.applyOperation("<>", c1));
        assertEquals(T, c1.applyOperation("<>", c2));
        assertEquals(T, c1.applyOperation("<>", s1));

        assertEquals(F, c1.applyOperation("<", c1));
        assertEquals(T, c1.applyOperation("<", c2));
        assertEquals(F, c1.applyOperation("<", s1));

        assertEquals(T, c1.applyOperation("<=", c1));
        assertEquals(T, c1.applyOperation("<=", c2));
        assertEquals(F, c1.applyOperation("<=", s1));

        assertEquals(F, c1.applyOperation(">", c1));
        assertEquals(F, c1.applyOperation(">", c2));
        assertEquals(T, c1.applyOperation(">", s1));

        assertEquals(T, c1.applyOperation(">=", c1));
        assertEquals(F, c1.applyOperation(">=", c2));
        assertEquals(T, c1.applyOperation(">=", s1));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation("=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation("<>", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation("<", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation("<=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation(">", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            c1.applyOperation(">=", new BooleanType(Boolean.valueOf(false)));
        });

    }

}
