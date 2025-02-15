package plp.group.Interpreter.Types.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BooleanTypeTest {

    @Test
    public void testAND() {
        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        // Truth table validity
        assertEquals(F, F.applyOperation("AND", F));
        assertEquals(F, T.applyOperation("AND", F));
        assertEquals(F, F.applyOperation("AND", T));
        assertEquals(T, T.applyOperation("AND", T));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("AND", new StringType("Hello"));
        });
    }

    @Test
    public void testOR() {
        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        // Truth table validity
        assertEquals(F, F.applyOperation("OR", F));
        assertEquals(T, T.applyOperation("OR", F));
        assertEquals(T, F.applyOperation("OR", T));
        assertEquals(T, T.applyOperation("OR", T));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("OR", new StringType("Hello"));
        });
    }

    @Test
    public void testNOT() {
        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        // Truth table validity
        assertEquals(F, T.applyOperation("NOT", null));
        assertEquals(T, F.applyOperation("NOT", null));
    }

    @Test
    public void testXOR() {
        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        // Truth table validity
        assertEquals(F, F.applyOperation("XOR", F));
        assertEquals(T, T.applyOperation("XOR", F));
        assertEquals(T, F.applyOperation("XOR", T));
        assertEquals(F, T.applyOperation("XOR", T));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("XOR", new StringType("Hello"));
        });
    }

    @Test
    public void testComparisons() {
        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, T.applyOperation("=", T));
        assertEquals(F, T.applyOperation("=", F));
        assertEquals(F, F.applyOperation("=", T));

        assertEquals(F, T.applyOperation("<>", T));
        assertEquals(T, T.applyOperation("<>", F));
        assertEquals(T, F.applyOperation("<>", T));

        assertEquals(F, T.applyOperation("<", T));
        assertEquals(F, T.applyOperation("<", F));
        assertEquals(T, F.applyOperation("<", T));

        assertEquals(T, T.applyOperation("<=", T));
        assertEquals(F, T.applyOperation("<=", F));
        assertEquals(T, F.applyOperation("<=", T));

        assertEquals(F, T.applyOperation(">", T));
        assertEquals(T, T.applyOperation(">", F));
        assertEquals(F, F.applyOperation(">", T));

        assertEquals(T, T.applyOperation(">=", T));
        assertEquals(T, T.applyOperation(">=", F));
        assertEquals(F, F.applyOperation(">=", T));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("=", new StringType("Hello"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("<>", new StringType("Hello"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("<", new StringType("Hello"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation("<=", new StringType("Hello"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation(">", new StringType("Hello"));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            T.applyOperation(">=", new StringType("Hello"));
        });
    }

}
