package plp.group.Interpreter;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the RuntimeValue interface and related things.
 */
public class RuntimeValueTest {
    
    //#region Primitive Test Cases

    @Test
    void testPrimitiveStoresValue() {
        var primitive = new RuntimeValue.Primitive(5);
        assertEquals(5, primitive.value());
    }
    
    @Test
    void testPrimitiveEquality() {
        RuntimeValue.Primitive primitive1 = new RuntimeValue.Primitive(100);
        RuntimeValue.Primitive primitive2 = new RuntimeValue.Primitive(100);
        RuntimeValue.Primitive primitive3 = new RuntimeValue.Primitive("Test");
        assertEquals(primitive1, primitive2);
        assertNotEquals(primitive1, primitive3);
        assertNotEquals(primitive2, primitive3);
    }

    @Test
    void testPrimitiveToString() {
        RuntimeValue.Primitive primitive = new RuntimeValue.Primitive("Hello");
        assertEquals("Primitive[value=Hello]", primitive.toString());
    }

    //#endregion

    //#region Method Test Cases

    @Test
    void testMethodInvocation() {
        RuntimeValue.Method sumMethod = new RuntimeValue.Method(
            "sum",
            new RuntimeValue.Method.MethodSignature(List.of(BigInteger.class, BigInteger.class), BigInteger.class),
            args -> {
                var first = (BigInteger) ((RuntimeValue.Primitive) args.get(0)).value();
                var second = (BigInteger) ((RuntimeValue.Primitive) args.get(1)).value();
                return new RuntimeValue.Primitive(first.add(second));
            }
        );

        List<RuntimeValue> parameters1 = List.of(
            new RuntimeValue.Primitive(new BigInteger("5")),
            new RuntimeValue.Primitive(new BigInteger("7"))
        );
        List<RuntimeValue> parameters2 = List.of(
            new RuntimeValue.Primitive(new BigInteger("15")),
            new RuntimeValue.Primitive(new BigInteger("-10"))
        );

        // Test valid usage of sample method
        assertDoesNotThrow(() -> {
            assertEquals(new RuntimeValue.Primitive(new BigInteger("12")), sumMethod.definition().invoke(parameters1));
            assertEquals(new RuntimeValue.Primitive(new BigInteger("5")), sumMethod.definition().invoke(parameters2));    
        });
    }

    @Test
    void testMethodEquality() {
        RuntimeValue.Method methodA = new RuntimeValue.Method(
            "testMethodA",
            new RuntimeValue.Method.MethodSignature(List.of(), String.class),
            _ -> {
                return new RuntimeValue.Primitive("String");
            }
        );
        
        // Same as methodA, for testing equality
        RuntimeValue.Method methodB = new RuntimeValue.Method(
            "testMethodA",
            new RuntimeValue.Method.MethodSignature(List.of(), String.class),
            _ -> {
                return new RuntimeValue.Primitive("String");
            }
        );
        
        // Different from method A and B.
        RuntimeValue.Method methodC = new RuntimeValue.Method(
            "testMethodC",
            new RuntimeValue.Method.MethodSignature(List.of(BigInteger.class, BigInteger.class), BigInteger.class),
            _ -> {
                return new RuntimeValue.Primitive(new BigInteger("95"));
            }
        );

        // Same name as methods A and B, but different signature
        RuntimeValue.Method methodD = new RuntimeValue.Method(
            "testMethodA",
            new RuntimeValue.Method.MethodSignature(List.of(String.class, String.class), String.class),
            _ -> {
                return new RuntimeValue.Primitive(new BigInteger("String"));
            }
        );

        // Same as methodC, except for different return type
        RuntimeValue.Method methodE = new RuntimeValue.Method(
            "testMethodC",
            new RuntimeValue.Method.MethodSignature(List.of(BigInteger.class, BigInteger.class), Void.class),
            _ -> {
                return new RuntimeValue.Primitive("Output");
            }
        );

        // Test equalities
        assertEquals(methodA, methodA);
        assertEquals(methodB, methodB);
        assertEquals(methodC, methodC);
        assertEquals(methodD, methodD);
        assertEquals(methodE, methodE);
        assertEquals(methodA, methodB);

        // Test inequalities
        assertNotEquals(methodA, methodC);
        assertNotEquals(methodA, methodD);
        assertNotEquals(methodA, methodE);

        assertNotEquals(methodB, methodC);
        assertNotEquals(methodB, methodD);
        assertNotEquals(methodB, methodE);
    
        assertNotEquals(methodC, methodD);
        assertNotEquals(methodC, methodE);

        assertNotEquals(methodD, methodE);
    }
    
    @Test
    void testMethodToString() {
        RuntimeValue.Method method = new RuntimeValue.Method(
            "print", 
            new RuntimeValue.Method.MethodSignature(List.of(), Void.class),
            _ -> {
                return new RuntimeValue.Primitive("Printed");
            }
        );

        assertTrue(method.toString().contains("print"));
    }

    //#endregion

}
