package plp.group.Interpreter;

import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a value calculated during the runtime of the program. 
 */
public sealed interface RuntimeValue {

    /**
     * Represents any runtime value which is a primitive type (IE can be represented with a single value in java).
     */
    record Primitive(
        @Nullable Object value
    ) implements RuntimeValue {};

    /**
     * Represents a function or procedure. 
     */
    record Method(
        String name,
        MethodSignature signature,
        MethodDefinition definition
    ) implements RuntimeValue {

        public record MethodSignature(
            List<Class<?>> parameterTypes, // TODO: might have to expand to figure out how to do pass by reference, etc.
            Class<?> returnType // Use Void.class for no return value.
        ) {};

        @FunctionalInterface
        interface MethodDefinition {
            RuntimeValue invoke(List<RuntimeValue> arguments) throws RuntimeException;
        };

        @Override
        public boolean equals(Object obj) {
            // Ensure obj is a method
            if (!(obj instanceof Method method)) return false;

            // Ensure same name and signature for equality
            return name.equals(method.name) && Objects.equals(signature, method.signature);
        }
    };

    // Some sort of REFERENCE implementation here... Reference to another RuntimeValue? Then how to tell apart References to different things when comparing?

    /**
     * Represents the definition of a class. 
     * A class instance is created based on its definition.
     */
    record ClassDefinition(
        // WHAT GOES HERE? NAME AND A SCOPE? 
    ) implements RuntimeValue {};

    /**
     * Creates a new instance of a class (aka an object).
     * Not called an object because it would conflict with 'Object' in Java.
     */
    record ClassInstance(
        // WHAT GOES HERE? NAME AND A SCOPE? Definition and a scope? 
    ) implements RuntimeValue {};

    /**
     * Use requireType to convert a RuntimeValue into an instance of a requested class.
     * 
     * @param <T> 
     * @param value The RuntimeValue to convert to a requested type.
     * @param type The '.class' of the type we want. 
     * @return the RuntimeValue as the requested class. 
     */
    @SuppressWarnings("unchecked")
    public static <T> T requireType(RuntimeValue value, Class<T> type) {
        if (RuntimeValue.class.isAssignableFrom(type)) {
            if (!type.isInstance(value)) {
                throw new RuntimeException("Expected value to be of type " + type + ", received " + value.getClass() + ".");
            }
            return (T) value;
        } else {
            var primitive = requireType(value, RuntimeValue.Primitive.class);
            if (!type.isInstance(primitive.value())) {
                var received = primitive.value() != null ? primitive.value().getClass() : null;
                throw new RuntimeException("Expected value to be of type " + type + ", received " + received + ".");
            }
            return (T) primitive.value();
        }
    }

}
