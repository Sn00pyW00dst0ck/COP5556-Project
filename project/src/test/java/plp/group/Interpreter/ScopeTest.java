package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.*;

import java.util.stream.Stream;

public class ScopeTest {

    @Test
    void testInsertion() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        assertEquals(null, table.lookup("x"));
        table.insert("x", new SymbolInfo("x", 5));
        assertEquals(new SymbolInfo("x", 5).value, table.lookup("x").value);
        table.exitScope();
    }

    @Test
    void testUpdate() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        assertEquals(null, table.lookup("x"));
        table.insert("x", new SymbolInfo("x", 5));
        assertEquals(new SymbolInfo("x", 5).value, table.lookup("x").value);
        table.update("x", new SymbolInfo("x", "Hello World"));
        assertEquals(new SymbolInfo("x", "Hello World").value, table.lookup("x").value);
        table.exitScope();
    }

    @Test
    void testDeletion() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.insert("x", new SymbolInfo("x", 5));
        table.insert("y", new SymbolInfo("y", 15));
        assertEquals(new SymbolInfo("x", 5).value, table.lookup("x").value);
        assertEquals(new SymbolInfo("y", 15).value, table.lookup("y").value);
        table.delete("x");
        assertEquals(null, table.lookup("x"));
        table.exitScope();
    }

    @Test
    void testIsInCurrentScope() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.insert("x", new SymbolInfo("x", 5));
        assertTrue(table.isInCurrentScope("x"));
        assertFalse(table.isInCurrentScope("y"));
        table.enterScope();
        table.insert("y", new SymbolInfo("y", 15));
        assertTrue(table.isInCurrentScope("y"));
        assertFalse(table.isInCurrentScope("x"));
        table.exitScope();
        assertTrue(table.isInCurrentScope("x"));
        assertFalse(table.isInCurrentScope("y"));
    }

    @ParameterizedTest
    @MethodSource
    void testMultiScopeLookups(String testName, String name, SymbolInfo expected) {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.insert("in_parent", new SymbolInfo("in_parent", true));
        table.enterScope();
        table.insert("y", new SymbolInfo("y", 12));
        table.enterScope();
        table.insert("x", new SymbolInfo("x", 1));
        table.insert("y", new SymbolInfo("y", 2));

        // If we expect null, the lookup should fail and return null
        if (expected != null) {
            Assertions.assertEquals(expected.value, table.lookup(name).value);
        } else {
            Assertions.assertEquals(null, table.lookup(name));
        }
    }

    public static Stream<Arguments> testMultiScopeLookups() {
        return Stream.of(
                Arguments.of("Valid Get 1", "x", new SymbolInfo("x", 1)),
                Arguments.of("Valid Get 2", "y", new SymbolInfo("y", 2)),
                Arguments.of("Get from parent", "in_parent", new SymbolInfo("in_parent", true)),
                Arguments.of("Get on not defined name", "z", null));
    }

}