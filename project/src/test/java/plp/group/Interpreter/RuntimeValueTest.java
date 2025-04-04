package plp.group.Interpreter;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x", 
                        new RuntimeValue.Primitive(new BigInteger("0")), 
                        false, 
                        false
                    ),
                new RuntimeValue.Method.MethodParameter(
                        "y", 
                        new RuntimeValue.Primitive(new BigInteger("0")), 
                        false, 
                        false
                    )
                ), 
                new RuntimeValue.Primitive(new BigInteger("0"))
            ), 
            (Scope methodScope) -> {
                BigInteger x = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class).value(), BigInteger.class);
                BigInteger y = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("y").get(), RuntimeValue.Variable.class).value(), BigInteger.class);
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(new RuntimeValue.Primitive(x.add(y)));
            }
        );

        List<RuntimeValue> parameters1 = List.of(
            new RuntimeValue.Variable("a", new RuntimeValue.Primitive(new BigInteger("5"))),
            new RuntimeValue.Variable("b", new RuntimeValue.Primitive(new BigInteger("-12")))
        );
        List<RuntimeValue> parameters2 = List.of(
            new RuntimeValue.Variable("x", new RuntimeValue.Primitive(new BigInteger("15"))),
            new RuntimeValue.Variable("y", new RuntimeValue.Primitive(new BigInteger("-10")))
        );
        List<RuntimeValue> parameters3 = List.of(
            new RuntimeValue.Variable("inavlid_type", new RuntimeValue.Primitive(Boolean.valueOf(true))),
            new RuntimeValue.Variable("y", new RuntimeValue.Primitive(new BigInteger("-10")))
        );

        assertDoesNotThrow(() -> {
            assertEquals(new RuntimeValue.Primitive(new BigInteger("-7")), sumMethod.invoke(new Scope(Optional.empty()), parameters1));
            assertEquals(new RuntimeValue.Primitive(new BigInteger("5")), sumMethod.invoke(new Scope(Optional.empty()), parameters2));
        });

        assertThrows(RuntimeException.class, () -> {
            sumMethod.invoke(new Scope(Optional.empty()), parameters3);
        });
    }

    @Test
    void testMethodInvocationWithReferenceParameters() {
        RuntimeValue.Method sumMethod = new RuntimeValue.Method(
            "sum", 
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x", 
                        new RuntimeValue.Primitive(new BigInteger("0")), 
                        false, 
                        false
                    ),
                new RuntimeValue.Method.MethodParameter(
                        "y", 
                        new RuntimeValue.Primitive(new BigInteger("0")), 
                        false, 
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "output", 
                        new RuntimeValue.Primitive(new BigInteger("0")), 
                        true, 
                        false
                    )
                ), 
                null
            ), 
            (Scope methodScope) -> {
                BigInteger x = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class).value(), BigInteger.class);
                BigInteger y = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("y").get(), RuntimeValue.Variable.class).value(), BigInteger.class);
                RuntimeValue.requireType(methodScope.lookup("output").get(), RuntimeValue.Reference.class).setValue(new RuntimeValue.Primitive(x.add(y)));
            }
        );

        var a = new RuntimeValue.Variable("a", new RuntimeValue.Primitive(new BigInteger("5")));
        var b = new RuntimeValue.Variable("b", new RuntimeValue.Primitive(new BigInteger("-12")));
        var result = new RuntimeValue.Variable("result", new RuntimeValue.Primitive(new BigInteger("-12")));
        List<RuntimeValue> parameters1 = List.of(a, b, new RuntimeValue.Reference("c", result));

        assertDoesNotThrow(() -> {
            // Setup the parent scope
            Scope s = new Scope(Optional.empty());
            s.define("a", a);
            s.define("b", b);
            s.define("result", result);
            // Procedure should return null, but result should update.
            assertEquals(new RuntimeValue.Primitive(null), sumMethod.invoke(new Scope(Optional.of(s)), parameters1));
            assertEquals(new RuntimeValue.Primitive(new BigInteger("-7")), result.value());
        });
    }
    
    @Test
    void testMethodToString() {
        RuntimeValue.Method method = new RuntimeValue.Method(
            "print", 
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                null
            ),
            (Scope _) -> {}
        );
        assertTrue(method.toString().contains("print"));
    }

    //#endregion
    
}
