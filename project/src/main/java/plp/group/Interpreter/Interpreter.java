package plp.group.Interpreter;

import plp.group.project.delphiBaseVisitor;

/**
 * The interpreter that walks the tree and does the actual calculations/running of the program.
 */
public class Interpreter extends delphiBaseVisitor<RuntimeValue> {
    

    /**
     * Use requireType to convert a RuntimeValue into an instance of a requested class. 
     * 
     * @param <T> 
     * @param value The RuntimeValue to convert to a requested type.
     * @param type The '.class' of the type we want. 
     * @return the RuntimeValue as the requested class. 
     */
    @SuppressWarnings("unchecked")
    private static <T> T requireType(RuntimeValue value, Class<T> type) throws Exception {
        if (RuntimeValue.class.isAssignableFrom(type)) {
            if (!type.isInstance(value)) {
                throw new Exception("Expected value to be of type " + type + ", received " + value.getClass() + ".");
            }
            return (T) value;
        } else {
            var primitive = requireType(value, RuntimeValue.Primitive.class);
            if (!type.isInstance(primitive.value())) {
                var received = primitive.value() != null ? primitive.value().getClass() : null;
                throw new Exception("Expected value to be of type " + type + ", received " + received + ".");
            }
            return (T) primitive.value();
        }
    }
}
