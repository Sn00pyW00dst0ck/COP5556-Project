package plp.group.Interpreter;

import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import plp.group.Interpreter.ControlFlowExceptions.ReturnException;
import plp.group.Interpreter.RuntimeValue.Method.MethodDefinition;

/**
 * Represents a value calculated during the runtime of the program. 
 */
public sealed interface RuntimeValue {

    /**
     * Represents any runtime value which is a primitive type (IE can be represented with a single value in java).
     */
    record Primitive(
        @Nullable Object value
    ) implements RuntimeValue {
        public String getPrintString() {
            return switch (value) {
                case Boolean bool -> bool.toString().toUpperCase();
                case String string -> string;
                case Character character -> character.toString();
                case BigInteger integer -> integer.toString();
                case BigDecimal decimal -> {
                    DecimalFormat singlePrecision = new DecimalFormat(" 0.000000000E00;-0.000000000E00");
                    DecimalFormat doublePrecision = new DecimalFormat(" 0.0000000000000000E000;-0.0000000000000000E000");

                    String result = "";
                    if (decimal.abs().compareTo(new BigDecimal("1.18e-38")) >= 0 || decimal.abs().compareTo(new BigDecimal("3.40e+38")) <= 0) {
                        result = singlePrecision.format(decimal);
                    } else if (decimal.abs().compareTo(new BigDecimal("2.23e-308")) >= 0 || decimal.abs().compareTo(new BigDecimal("1.79e+308")) <= 0) {
                        result = doublePrecision.format(decimal);
                    } else {
                        throw new ArithmeticException("Overflow for DoubleType " + decimal + " when printing primitive");
                    }

                    // Last formatting pass.
                    if (!result.contains("E-")) {
                        result = result.replace("E", "E+");
                    }
                    yield result.toString();
                }
                default -> throw new RuntimeException("Unexpected error when attempting to get print string for: " + this.toString());
            };
        }
    };

    /**
     * Represents a variable.
     */
    record Variable(
        String name, 
        RuntimeValue value
    ) implements RuntimeValue {
        public String getPrintString() {
            return value.getPrintString();
        }
    };

    /**
     * Represents a function or procedure. 
     */
    record Method(
        String name,
        MethodSignature signature,
        MethodDefinition definition
    ) implements RuntimeValue {

        public record MethodParameter(
            String name,
            RuntimeValue type,
            boolean isReference // If true, argument should be wrapped in a Reference
            // TODO: boolean for isVariadic...
        ) {};

        public record MethodSignature(
            List<MethodParameter> parameters, // TODO: might have to expand to figure out how to do pass by reference, etc.
            @Nullable RuntimeValue returnType // Use RuntimeValue.Primitive(null) for no return value.
        ) {};

        @FunctionalInterface
        interface MethodDefinition {
            void invoke(Scope callFrame) throws RuntimeException;
        };

        /**
         * Invoke the method to execute its code with given parameters from a given stack. 
         * 
         * @param callerScope the scope that the method was called from. It is the parent scope of the method call.
         * @param arguments the parameters to the method. 
         * @return the result of the function, or null if a procedure.
         * @throws RuntimeException if something goes wrong
         */
        public RuntimeValue invoke(Scope callerScope, List<RuntimeValue.Variable> arguments) throws RuntimeException {
            if (arguments.size() != signature.parameters().size()) {
                throw new RuntimeException("Incorrect number of arguments.");
            }

            Scope methodScope = new Scope(Optional.of(callerScope));

            // Pass arguments properly...
            for (int i = 0; i < arguments.size(); i++) {
                MethodParameter param = signature.parameters().get(i);
                RuntimeValue.Variable arg = arguments.get(i);

                // Ensure the type matches up...
                requireType(arg.value(), param.type().getClass());

                // Add to scope properly...
                if (param.isReference()) {
                    methodScope.define(param.name(), arg); // TODO: update this!!!
                } else {
                    methodScope.define(param.name(), arg);
                }
            }

            // If a function we set the result variable...
            if (signature().returnType() != null) {
                methodScope.define("result", new RuntimeValue.Variable("result", signature().returnType()));
            }
            
            // Evaluate, if a return exception is thrown catch it but nothing special has to happen.
            try {
                definition().invoke(methodScope);
            } catch (ReturnException r) {}

            // If a function, return the value of the result variable...
            if (signature().returnType() != null) {
                return requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).value();
            }

            return new RuntimeValue.Primitive(null);
        }

        @Override
        public boolean equals(Object obj) {
            // Ensure obj is a method
            if (!(obj instanceof Method method)) return false;

            // Ensure same name and signature for equality
            return name.equals(method.name) && Objects.equals(signature, method.signature);
        }

        public String getPrintString() {
            throw new RuntimeException("Unexpected error when attempting to get print string for: " + this.toString());
        }
    };

    // Some sort of REFERENCE implementation here... Reference to another RuntimeValue? Then how to tell apart References to different things when comparing?

    /**
     * Represents an enumeration type. 
     */
    record Enumeration(
        @Nullable String value,
        Map<String, Integer> options
    ) implements RuntimeValue {
        public String getPrintString() {
            throw new RuntimeException("Unexpected error when attempting to get print string for: " + this.toString());
        }
    };

    /**
     * Represents the definition of a class. 
     * A class instance is created based on its definition.
     */
    record ClassDefinition(
        String typeName, 
        Scope privateScope, 
        Scope protectedScope,
        Scope publicScope
    ) implements RuntimeValue {
        public String getPrintString() {
            throw new RuntimeException("Unexpected error when attempting to get print string for: " + this.toString());
        }
    };

    /**
     * Creates a new instance of a class (aka an object).
     * Not called an object because it would conflict with 'Object' in Java.
     */
    record ClassInstance(
        ClassDefinition definition,
        Scope publicScope,
        Scope privateScope, 
        Scope protectedScope
    ) implements RuntimeValue {
        public String getPrintString() {
            throw new RuntimeException("Unexpected error when attempting to get print string for: " + this.toString());
        }
    };

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

    public abstract String getPrintString();

}
