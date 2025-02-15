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

import java.math.BigDecimal;
import java.util.stream.Stream;

public class ScopeTest {
    /*
     * @Test
     * void testInsertion() {
     * SymbolTable table = new SymbolTable();
     * table.enterScope();
     * assertEquals(null, table.lookup("x"));
     * 
     * table.insert("x", new SymbolInfo("x", new PascalBoolean(true)));
     * assertEquals(new SymbolInfo("x", new PascalBoolean(true)).value.getValue(),
     * table.lookup("x").value.getValue());
     * assertTrue(table.lookup("x").value.isInitialized());
     * 
     * table.exitScope();
     * }
     * 
     * @Test
     * void testUpdate() {
     * SymbolTable table = new SymbolTable();
     * table.enterScope();
     * assertEquals(null, table.lookup("x"));
     * table.insert("x", new SymbolInfo("x", new PascalBoolean()));
     * assertEquals(new SymbolInfo("x", new PascalBoolean()).value.getValue(),
     * table.lookup("x").value.getValue());
     * table.update("x", new SymbolInfo("x", new PascalString("Hello World!")));
     * assertEquals(new SymbolInfo("x", new
     * PascalString("Hello World!")).value.getValue(),
     * table.lookup("x").value.getValue());
     * table.exitScope();
     * }
     * 
     * @Test
     * void testDeletion() {
     * SymbolTable table = new SymbolTable();
     * table.enterScope();
     * table.insert("x", new SymbolInfo("x", new PascalByte()));
     * table.insert("y", new SymbolInfo("y", new PascalChar()));
     * assertEquals(new SymbolInfo("x", new PascalByte()).value.getValue(),
     * table.lookup("x").value.getValue());
     * assertEquals(new SymbolInfo("y", new PascalChar()).value.getValue(),
     * table.lookup("y").value.getValue());
     * table.delete("x");
     * assertEquals(null, table.lookup("x"));
     * table.exitScope();
     * }
     * 
     * @Test
     * void testIsInCurrentScope() {
     * SymbolTable table = new SymbolTable();
     * table.enterScope();
     * table.insert("x", new SymbolInfo("x", new PascalInt64()));
     * assertTrue(table.isInCurrentScope("x"));
     * assertFalse(table.isInCurrentScope("y"));
     * table.enterScope();
     * table.insert("y", new SymbolInfo("y", new PascalDouble()));
     * assertTrue(table.isInCurrentScope("y"));
     * assertFalse(table.isInCurrentScope("x"));
     * table.exitScope();
     * assertTrue(table.isInCurrentScope("x"));
     * assertFalse(table.isInCurrentScope("y"));
     * }
     * 
     * @ParameterizedTest
     * 
     * @MethodSource
     * void testMultiScopeLookups(String testName, String name, SymbolInfo expected)
     * {
     * SymbolTable table = new SymbolTable();
     * table.enterScope();
     * table.insert("in_parent", new SymbolInfo("in_parent", new
     * PascalBoolean(true)));
     * table.enterScope();
     * table.insert("y", new SymbolInfo("y", new PascalBoolean(true)));
     * table.enterScope();
     * table.insert("x", new SymbolInfo("x", new PascalChar()));
     * table.insert("y", new SymbolInfo("y", new PascalDouble(new
     * BigDecimal("15"))));
     * 
     * // If we expect null, the lookup should fail and return null
     * if (expected != null) {
     * Assertions.assertEquals(expected.value.getValue(),
     * table.lookup(name).value.getValue());
     * } else {
     * Assertions.assertEquals(null, table.lookup(name));
     * }
     * }
     * 
     * public static Stream<Arguments> testMultiScopeLookups() {
     * return Stream.of(
     * Arguments.of("Valid Get 1", "x", new SymbolInfo("x", new PascalChar())),
     * Arguments.of("Valid Get 2", "y", new SymbolInfo("y", new PascalDouble(new
     * BigDecimal("15")))),
     * Arguments.of("Get from parent", "in_parent", new SymbolInfo("in_parent",
     * new PascalBoolean(true))),
     * Arguments.of("Get on not defined name", "z", null));
     * }
     * 
     */
}