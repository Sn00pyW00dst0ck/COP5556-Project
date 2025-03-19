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
            RuntimeValue invoke(List<RuntimeValue> arguments) throws Exception;
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

    record ClassDefinition(
        // WHAT GOES HERE? NAME AND A SCOPE? 
    ) implements RuntimeValue {};

    record ClassInstance(
        // WHAT GOES HERE? NAME AND A SCOPE? Definition and a scope? 
    ) implements RuntimeValue {};

}
