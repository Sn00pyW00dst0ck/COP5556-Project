package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

        /**
         * Cheat a bit for this by not deep copying the value itself, could cause issues later.
         */
        public RuntimeValue deepCopy() {
            return new RuntimeValue.Primitive(value);
        }
    };

    /**
     * Represents a variable.
     */
    public final class Variable implements RuntimeValue {
        private final String name;
        private RuntimeValue value;

        public Variable(String name, RuntimeValue value) {
            this.name = name;
            this.value = value;
        }

        public String name() {
            return this.name;
        }
    
        public RuntimeValue value() {
            return this.value;
        }
    
        public void setValue(RuntimeValue newValue) {
            this.value = newValue;
        }

        public String getPrintString() {
            return this.value.getPrintString();
        }

        /**
         * Cheat a bit for this.
         */
        public RuntimeValue deepCopy() {
            return new RuntimeValue.Variable(this.name(), value.deepCopy());
        }
    };

    /**
     * Represents an array (used for variadic argument)
     */
    record Array(
        List<RuntimeValue> elements
    ) implements RuntimeValue {

        public RuntimeValue get(int index) {
            return elements().get(index);
        }
        
        public void setValue(int index, RuntimeValue newValue) {
            if (elements.get(index) instanceof RuntimeValue.Reference ref) {
                ref.setValue(newValue);
            } else if (elements.get(index) instanceof RuntimeValue.Variable var) {
                var.setValue(newValue);
            } else {
                elements.set(index, newValue);
            }
        }

        public int size() {
            return elements().size();
        }

        public String getPrintString() {
            return elements().stream()
                .map(RuntimeValue::getPrintString)
                .collect(Collectors.joining(", ", "[", "]"));
        }

        public RuntimeValue deepCopy() {
            return this;
        }
    }

    /**
     * Represents a reference to another variable. When this updates, that variable's value should update too.
     */
    record Reference(
        String name, 
        RuntimeValue.Variable variable
    ) implements RuntimeValue {
        public void setValue(RuntimeValue newValue) {
            variable.setValue(newValue);
        }

        public RuntimeValue getValue() {
            return variable.value();
        }

        public String getPrintString() {
            return variable.getPrintString();
        }

        public RuntimeValue deepCopy() {
            return new RuntimeValue.Reference(this.name(), RuntimeValue.requireType(variable.deepCopy(), RuntimeValue.Variable.class));
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
            boolean isReference, // If true, argument should be wrapped in a Reference
            boolean isVariadic
        ) {};

        public record MethodSignature(
            List<MethodParameter> parameters,
            @Nullable RuntimeValue returnType // Use null to signify no return
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
        public RuntimeValue invoke(Scope callerScope, List<RuntimeValue> arguments) throws RuntimeException {
            if (arguments.size() != signature.parameters().size() && !signature().parameters().getLast().isVariadic()) {
                throw new RuntimeException("Incorrect number of arguments.");
            }

            Scope methodScope = new Scope(Optional.of(callerScope));

            // Pass arguments properly...
            for (int i = 0; i < arguments.size(); i++) {
                MethodParameter param = signature.parameters().get(i);
                RuntimeValue arg = arguments.get(i);

                // If parameter is variadic, pack the rest of the arguments into an Array in the scope.
                if (param.isVariadic()) {
                    List<RuntimeValue> varargs = new ArrayList<>();
                    for (int j = i; j < arguments.size(); j++) {
                        arg = arguments.get(j);
                        if (param.isReference()) {
                            RuntimeValue.Reference reference = requireType(arg, RuntimeValue.Reference.class);
                            requireType(reference.getValue(), param.type().getClass());
                            varargs.add(reference);
                        } else {
                            RuntimeValue.Variable variable = requireType(arg, RuntimeValue.Variable.class);
                            requireType(variable.value(), param.type().getClass());
                            varargs.add(new Variable(variable.name(), variable.value()));
                        }
                    }
                    methodScope.define(param.name(), new Variable(param.name(), new RuntimeValue.Array(varargs)));
                    break; // variadic should be last argument.
                }

                // Check type and add to scope properly...
                if (param.isReference()) {
                    RuntimeValue.Reference reference = requireType(arg, RuntimeValue.Reference.class);
                    requireType(reference.getValue(), param.type().getClass());
                    methodScope.define(param.name(), reference);
                } else {
                    RuntimeValue.Variable variable = requireType(arg, RuntimeValue.Variable.class);
                    requireType(variable.value(), param.type().getClass());
                    methodScope.define(param.name(), variable);
                }
            }

            // If a function we set the result variable...
            if (signature().returnType() != null) {
                methodScope.define("result", new RuntimeValue.Variable("result", signature().returnType()));
            }
            
            // Evaluate, if a return exception is thrown catch it but nothing special has to happen.
            definition().invoke(methodScope);
            
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

        /**
         * Cheat a bit for this.
         */
        public RuntimeValue deepCopy() {
            return this;
        }
    };

    // Some sort of REFERENCE implementation here... Reference to another RuntimeValue? Then how to tell apart References to different things when comparing?

    /**
     * Represents an enumeration type. 
     */
    public final class Enumeration implements RuntimeValue {
        private String value; 
        private final Map<String, RuntimeValue.Primitive> options;

        public Enumeration(@Nullable String value, Map<String, RuntimeValue.Primitive> options) {
            this.value = value;
            this.options = options;
        }

        public String value() {
            return this.value;
        }

        public Map<String, RuntimeValue.Primitive> options() {
            return this.options;
        }

        public void setValue(String newValue) {
            if (!options.keySet().contains(newValue)) {
                throw new RuntimeException("Key '" + newValue + "' does not exist in Enumeration.");
            }
            this.value = newValue;
        }

        public void setValue(BigInteger newValue) {
            for (Map.Entry<String, RuntimeValue.Primitive> entry : this.options().entrySet()) {
                BigInteger entryValue = RuntimeValue.requireType(entry.getValue(), BigInteger.class);
                if (entryValue.equals(newValue)) {
                    this.value = entry.getKey();
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            // Ensure obj is a method
            if (!(obj instanceof Enumeration enumeration)) return false;

            // Ensure same name and signature for equality
            return value().equals(enumeration.value()) && Objects.equals(options(), enumeration.options());
        }

        public String getPrintString() {
            return value;
        }

        /**
         * Cheat a bit for this.
         */
        public RuntimeValue deepCopy() {
            return this;
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

        /**
         * Cheat a bit for this.
         */
        public RuntimeValue deepCopy() {
            return this;
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

        /**
         * Cheat a bit for this, hopefully it won't cause issues later.
         */
        public RuntimeValue deepCopy() {
            return this;
        }
    };

    /**
     * A one off to allow any type to be accepted by a Method. 
     */
    public final class AnyType implements RuntimeValue {
        public String getPrintString() {
            throw new RuntimeException("Don't evaluate directly with AnyType");
        }

        public RuntimeValue deepCopy() {
            return new RuntimeValue.AnyType();
        }
    }

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
        if (type.equals(AnyType.class)) {
            return (T) value;
        }
    
        if (RuntimeValue.class.isAssignableFrom(type)) {
            if (!type.isInstance(value)) {
                throw new RuntimeException("Expected value to be of type " + type + ", received " + value.getClass() + ".");
            }
            return (T) value;
        } else {
            var primitive = requireType(value, RuntimeValue.Primitive.class);
            
            // If it is a BigInteger allow auto convert to BigDecimal
            if (type.equals(BigDecimal.class) && primitive.value() instanceof BigInteger bigInteger) {
                return (T) new BigDecimal(bigInteger);
            }

            if (!type.isInstance(primitive.value())) {
                var received = primitive.value() != null ? primitive.value().getClass() : null;
                throw new RuntimeException("Expected value to be of type " + type + ", received " + received + ".");
            }
            return (T) primitive.value();
        }
    }

    public abstract String getPrintString();
    public abstract RuntimeValue deepCopy();

}
