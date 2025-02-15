package plp.group.Interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import plp.group.Interpreter.Types.Simple.BooleanType;
import plp.group.Interpreter.Types.Simple.CharType;
import plp.group.Interpreter.Types.Simple.StringType;
import plp.group.Interpreter.Types.Simple.Integers.ByteType;
import plp.group.Interpreter.Types.Simple.Integers.Int64Type;
import plp.group.Interpreter.Types.Simple.Reals.DoubleType;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class ScopeTest {

    @Test
    void testInsertion() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        assertEquals(null, table.lookup("x"));

        table.insert("x", new SymbolInfo("x", new BooleanType(true)));
        assertEquals(new SymbolInfo("x", new BooleanType(true)).value.getValue(), table.lookup("x").value.getValue());

        table.exitScope();
    }

    @Test
    void testUpdate() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        assertEquals(null, table.lookup("x"));

        table.insert("x", new SymbolInfo("x", new BooleanType()));
        assertEquals(new SymbolInfo("x", new BooleanType()).value.getValue(), table.lookup("x").value.getValue());

        table.update("x", new SymbolInfo("x", new StringType("Hello World!")));
        assertEquals(new SymbolInfo("x", new StringType("Hello World!")).value.getValue(),
                table.lookup("x").value.getValue());
        table.exitScope();
    }

    @Test
    void testDeletion() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.insert("x", new SymbolInfo("x", new ByteType()));
        table.insert("y", new SymbolInfo("y", new CharType()));
        assertEquals(new SymbolInfo("x", new ByteType()).value.getValue(),
                table.lookup("x").value.getValue());
        assertEquals(new SymbolInfo("y", new CharType()).value.getValue(),
                table.lookup("y").value.getValue());
        table.delete("x");
        assertEquals(null, table.lookup("x"));
        table.exitScope();
    }

    @Test
    void testIsInCurrentScope() {
        SymbolTable table = new SymbolTable();
        table.enterScope();
        table.insert("x", new SymbolInfo("x", new Int64Type()));
        assertTrue(table.isInCurrentScope("x"));
        assertFalse(table.isInCurrentScope("y"));
        table.enterScope();
        table.insert("y", new SymbolInfo("y", new DoubleType()));
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
        table.insert("in_parent", new SymbolInfo("in_parent", new BooleanType(true)));
        table.enterScope();
        table.insert("y", new SymbolInfo("y", new BooleanType(true)));
        table.enterScope();
        table.insert("x", new SymbolInfo("x", new CharType()));
        table.insert("y", new SymbolInfo("y", new DoubleType(new BigDecimal("15"))));

        // If we expect null, the lookup should fail and return null
        if (expected != null) {
            Assertions.assertEquals(expected.value.getValue(),
                    table.lookup(name).value.getValue());
        } else {
            Assertions.assertEquals(null, table.lookup(name));
        }
    }

    public static Stream<Arguments> testMultiScopeLookups() {
        return Stream.of(
                Arguments.of("Valid Get 1", "x", new SymbolInfo("x", new CharType())),
                Arguments.of("Valid Get 2", "y", new SymbolInfo("y", new DoubleType(new BigDecimal("15")))),
                Arguments.of("Get from parent", "in_parent", new SymbolInfo("in_parent",
                        new BooleanType(true))),
                Arguments.of("Get on not defined name", "z", null));
    }

}