package plp.group.Interpreter.Types.Structured;

import org.junit.jupiter.api.Test;

import plp.group.Interpreter.Types.Simple.StringType;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class SetTypeTest {

    @Test
    void testConstructorWithValidType() {
        // Create a SetType for Integer with a HashSet of values
        SetType<Integer> setType = new SetType<>(new HashSet<>(Set.of(1, 2, 3)));

        @SuppressWarnings("unchecked")
        var value = (HashSet<Integer>) setType.getValue();

        // Assert the values are correct
        assertEquals(3, value.size());
        assertTrue(value.contains(1));
        assertTrue(value.contains(2));
        assertTrue(value.contains(3));

        // Assert the element type is Integer.class
        assertEquals(Integer.class, setType.getElementType());
    }

    @Test
    void testConstructorWithEmptySet() {
        // Test if SetType can be created with an empty set
        assertThrows(UnsupportedOperationException.class, () -> {
            SetType<Integer> _ = new SetType<Integer>(new HashSet<>());
        });

        SetType<Integer> setType = new SetType<Integer>(Integer.class);
        @SuppressWarnings("unchecked")
        var value = (HashSet<Integer>) setType.getValue();
        assertEquals(0, value.size());
        assertEquals(Integer.class, setType.getElementType());
    }

    @Test
    void testUnionOfSets() {
        // Create two SetTypes of Integer
        SetType<Integer> setType1 = new SetType<>(new HashSet<>(Set.of(1, 2, 3)));
        SetType<Integer> setType2 = new SetType<>(new HashSet<>(Set.of(3, 4, 5)));

        // Perform union operation
        @SuppressWarnings("unchecked")
        SetType<Integer> result = (SetType<Integer>) setType1.applyOperation("+", setType2);
        @SuppressWarnings("unchecked")
        var value = (HashSet<Integer>) result.getValue();

        // Assert the union contains the correct elements
        assertEquals(5, value.size()); // 1, 2, 3, 4, 5
        assertTrue(value.contains(1));
        assertTrue(value.contains(2));
        assertTrue(value.contains(3));
        assertTrue(value.contains(4));
        assertTrue(value.contains(5));

        // Should not be able to union different element types
        SetType<String> setType3 = new SetType<>(new HashSet<>(Set.of("a", "b", "c")));
        assertThrows(UnsupportedOperationException.class, () -> {
            setType1.applyOperation("+", setType3);
        });
    }

    @Test
    void testIntersectionOfSets() {
        // Create two SetTypes of Integer
        SetType<Integer> setType1 = new SetType<>(new HashSet<>(Set.of(1, 2, 3)));
        SetType<Integer> setType2 = new SetType<>(new HashSet<>(Set.of(3, 4, 5)));

        // Perform intersection operation
        @SuppressWarnings("unchecked")
        SetType<Integer> result = (SetType<Integer>) setType1.applyOperation("*", setType2);
        @SuppressWarnings("unchecked")
        var value = (HashSet<Integer>) result.getValue();

        // Assert the union contains the correct elements
        assertEquals(1, value.size()); // 3
        assertTrue(value.contains(3));

        // Should not be able to union different element types
        SetType<String> setType3 = new SetType<>(new HashSet<>(Set.of("a", "b", "c")));
        assertThrows(UnsupportedOperationException.class, () -> {
            setType1.applyOperation("*", setType3);
        });
    }

    @Test
    void testDifferenceOfSets() {
        // Create two SetTypes of Integer
        SetType<Integer> setType1 = new SetType<>(new HashSet<>(Set.of(1, 2, 3)));
        SetType<Integer> setType2 = new SetType<>(new HashSet<>(Set.of(3, 4, 5)));

        // Perform union operation
        @SuppressWarnings("unchecked")
        SetType<Integer> result = (SetType<Integer>) setType1.applyOperation("-", setType2);
        @SuppressWarnings("unchecked")
        var value = (HashSet<Integer>) result.getValue();

        // Assert the union contains the correct elements
        assertEquals(2, value.size()); // 3, 4
        assertTrue(value.contains(1));
        assertTrue(value.contains(2));

        // Should not be able to union different element types
        SetType<String> setType3 = new SetType<>(new HashSet<>(Set.of("a", "b", "c")));
        assertThrows(UnsupportedOperationException.class, () -> {
            setType1.applyOperation("+", setType3);
        });
    }

    @Test
    void testSetContains() {
        // Create a SetType of Integer
        SetType<StringType> setType = new SetType<>(new HashSet<>(Set.of(new StringType("Hi"))));

        var result1 = setType.applyOperation("IN", new StringType("Hello"));
        assertFalse(((Boolean) result1.getValue()).booleanValue());

        var result2 = setType.applyOperation("IN", new StringType("Hi"));
        assertTrue(((Boolean) result2.getValue()).booleanValue());
    }

}
