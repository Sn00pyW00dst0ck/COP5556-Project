package plp.group.Interpreter.Types.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SubrangeTypeTest {

    @Test
    void testContains() {

        var s1 = new SubrangeType<StringType>(new StringType("AA"), new StringType("ZZ"), new StringType("Ab"));
        var s2 = new SubrangeType<StringType>(new StringType("aa"), new StringType("zz"), new StringType("ba"));

        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, s1.applyOperation("IN", new StringType("AA")));
        assertEquals(T, s1.applyOperation("IN", new StringType("MB")));
        assertEquals(T, s1.applyOperation("IN", new StringType("ZZ")));
        assertEquals(F, s1.applyOperation("IN", new StringType("aa")));
        assertEquals(F, s1.applyOperation("IN", new StringType(".'")));
        assertEquals(F, s1.applyOperation("IN", new StringType("{}")));

        assertEquals(T, s2.applyOperation("IN", new StringType("aa")));
        assertEquals(T, s2.applyOperation("IN", new StringType("mb")));
        assertEquals(T, s2.applyOperation("IN", new StringType("zz")));
        assertEquals(F, s2.applyOperation("IN", new StringType("AA")));
        assertEquals(F, s2.applyOperation("IN", new StringType(".'")));
        assertEquals(F, s2.applyOperation("IN", new StringType("{}")));

        // Throws when bad operands
        assertThrows(Exception.class, () -> {
            s1.applyOperation("IN", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(Exception.class, () -> {
            s2.applyOperation("IN", new CharType(Character.valueOf('h')));
        });
    }

    @Test
    public void testComparisons() {
        // Should work the same with any comparable, so this is fine to use Character
        // here...
        var s1 = new SubrangeType<Character>(Character.valueOf('a'), Character.valueOf('z'), Character.valueOf('g'));
        var s2 = new SubrangeType<Character>(Character.valueOf('a'), Character.valueOf('z'), Character.valueOf('l'));

        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, s1.applyOperation("=", s1));
        assertEquals(F, s1.applyOperation("=", s2));

        assertEquals(F, s1.applyOperation("<>", s1));
        assertEquals(T, s1.applyOperation("<>", s2));

        assertEquals(F, s1.applyOperation("<", s1));
        assertEquals(T, s1.applyOperation("<", s2));

        assertEquals(T, s1.applyOperation("<=", s1));
        assertEquals(T, s1.applyOperation("<=", s2));

        assertEquals(F, s1.applyOperation(">", s1));
        assertEquals(F, s1.applyOperation(">", s2));

        assertEquals(T, s1.applyOperation(">=", s1));
        assertEquals(F, s1.applyOperation(">=", s2));

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
