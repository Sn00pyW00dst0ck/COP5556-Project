package plp.group.Interpreter.Types.Simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class EnumTypeTest {

    @Test
    public void testComparisons() {
        var enumMap1 = new HashMap<String, Integer>();
        enumMap1.put("Hello", 0);
        enumMap1.put("World", 1);

        var e1 = new EnumType(enumMap1, "Hello");
        var e2 = new EnumType(enumMap1, "World");

        var T = new BooleanType(Boolean.valueOf(true));
        var F = new BooleanType(Boolean.valueOf(false));

        assertEquals(T, e1.applyOperation("=", e1));
        assertEquals(F, e1.applyOperation("=", e2));

        assertEquals(F, e1.applyOperation("<>", e1));
        assertEquals(T, e1.applyOperation("<>", e2));

        assertEquals(F, e1.applyOperation("<", e1));
        assertEquals(T, e1.applyOperation("<", e2));

        assertEquals(T, e1.applyOperation("<=", e1));
        assertEquals(T, e1.applyOperation("<=", e2));

        assertEquals(F, e1.applyOperation(">", e1));
        assertEquals(F, e1.applyOperation(">", e2));

        assertEquals(T, e1.applyOperation(">=", e1));
        assertEquals(F, e1.applyOperation(">=", e2));

        // Assert throws when bad operands
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation("=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation("<>", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation("<", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation("<=", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation(">", new BooleanType(Boolean.valueOf(false)));
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            e1.applyOperation(">=", new BooleanType(Boolean.valueOf(false)));
        });
    }

}
