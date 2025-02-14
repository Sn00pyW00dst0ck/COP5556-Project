package plp.group.PascalTypes.Utils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;

import plp.group.PascalTypes.PascalType;
import plp.group.PascalTypes.Scalars.Standard.PascalBoolean;
import plp.group.PascalTypes.Scalars.Standard.PascalByte;
import plp.group.PascalTypes.Scalars.Standard.PascalChar;
import plp.group.PascalTypes.Scalars.Standard.PascalInt64;
import plp.group.PascalTypes.Scalars.Standard.PascalInteger;
import plp.group.PascalTypes.Scalars.Standard.PascalLongint;
import plp.group.PascalTypes.Scalars.Standard.PascalLongword;
import plp.group.PascalTypes.Scalars.Standard.PascalReal;
import plp.group.PascalTypes.Scalars.Standard.PascalShortint;
import plp.group.PascalTypes.Scalars.Standard.PascalSmallint;
import plp.group.PascalTypes.Scalars.Standard.PascalString;
import plp.group.PascalTypes.Scalars.Standard.PascalWord;

public class PascalOperationHandler {

    /**
     * Given two PascalTypes and an operator, attempts to perform the operation.
     * 
     * @param lhs
     * @param rhs
     * @param operator
     * @return The result of the operation.
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    public static PascalType performBinaryOperation(PascalType lhs, PascalType rhs, String operator)
            throws UnsupportedOperationException {
        return switch (operator) {
            // Relational operators
            case "=", "<>", "<", "<=", ">", ">=" -> compare(lhs, rhs, operator);

            // Additive operators
            case "+" -> add(lhs, rhs);
            case "-" -> subtract(lhs, rhs);
            case "OR" -> or(lhs, rhs);

            // Multiplicitive operators
            case "*" -> multiply(lhs, rhs);
            case "DIV", "/" -> divide(lhs, rhs);
            case "MOD" -> mod(lhs, rhs);
            case "AND" -> and(lhs, rhs);

            // Default throw unsupported
            default -> throw new UnsupportedOperationException("Unhandled or unknown binary operator: " + operator);
        };
    }

    /**
     * Given a PascalType and an operator, attempts to perform the operation.
     * 
     * @param lhs
     * @param rhs
     * @param operator
     * @return The result of the operation.
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    public static PascalType performUnaryOperation(PascalType operand, String operator)
            throws UnsupportedOperationException {
        return switch (operator) {
            case "NOT" -> not(operand);
            case "-" -> negate(operand);

            // Default throw unsupported
            default -> throw new UnsupportedOperationException("Unhandled or unknown unary operator: " + operator);
        };
    }

    private static PascalType compare(PascalType lhs, PascalType rhs, String operator) {
        // TODO: some way to determine the common type of the two sides

        int comparison = 0;
        // TODO: init the comparison type properly...

        return switch (operator) {
            case "=" -> new PascalBoolean(comparison == 0);
            case "<>" -> new PascalBoolean(comparison != 0);
            case "<" -> new PascalBoolean(comparison < 0);
            case "<=" -> new PascalBoolean(comparison <= 0);
            case ">" -> new PascalBoolean(comparison > 0);
            case ">=" -> new PascalBoolean(comparison >= 0);
            default -> throw new UnsupportedOperationException("Unsupported comparison operator: " + operator);
        };
    }

    private static PascalType add(PascalType lhs, PascalType rhs) {
        // Strings & chars concat
        if ((lhs instanceof PascalString || lhs instanceof PascalChar)
                && (rhs instanceof PascalString || rhs instanceof PascalChar)) {
            var result = lhs.toString() + rhs.toString();
            return new PascalString(result);
        }

        // Numbers add, coerce the types and then do the addition
        if (lhs instanceof PascalReal || rhs instanceof PascalReal) {
            var l = coerceType(BigDecimal.class, lhs);
            var r = coerceType(BigDecimal.class, rhs);
            return new PascalReal(l.add(r));
        }
        if (lhs instanceof PascalInteger || rhs instanceof PascalInteger) {
            var l = coerceType(BigInteger.class, lhs);
            var r = coerceType(BigInteger.class, rhs);

            // TODO: instead of PascalInteger, do the smallest integer type that applies.
            return new PascalInteger(l.add(r));
        }

        throw new UnsupportedOperationException("Unsupported addition: " + lhs.toString() + " + " + rhs.toString());
    }

    private static PascalType subtract(PascalType lhs, PascalType rhs) {
        // Numbers subtract, coerce the types and then do the operation
        if (lhs instanceof PascalReal || rhs instanceof PascalReal) {
            var l = coerceType(BigDecimal.class, lhs);
            var r = coerceType(BigDecimal.class, rhs);
            return new PascalReal(l.subtract(r));
        }
        if (lhs instanceof PascalInteger || rhs instanceof PascalInteger) {
            var l = coerceType(BigInteger.class, lhs);
            var r = coerceType(BigInteger.class, rhs);

            // TODO: instead of PascalInteger, do the smallest integer type that applies.
            return new PascalInteger(l.subtract(r));
        }

        throw new UnsupportedOperationException("Unsupported subtraction: " + lhs.toString() + " - " + rhs.toString());
    }

    private static PascalType or(PascalType lhs, PascalType rhs) {
        if (lhs instanceof PascalBoolean && rhs instanceof PascalBoolean) {
            boolean result = ((PascalBoolean) lhs).getValue() || ((PascalBoolean) rhs).getValue();
            return new PascalBoolean(result);
        }
        throw new UnsupportedOperationException("OR operation only supports Boolean types");
    }

    private static PascalType multiply(PascalType lhs, PascalType rhs) {
        // Numbers multiply, coerce the types and then do the operation
        if (lhs instanceof PascalReal || rhs instanceof PascalReal) {
            var l = coerceType(BigDecimal.class, lhs);
            var r = coerceType(BigDecimal.class, rhs);
            return new PascalReal(l.multiply(r));
        }
        if (lhs instanceof PascalInteger || rhs instanceof PascalInteger) {
            var l = coerceType(BigInteger.class, lhs);
            var r = coerceType(BigInteger.class, rhs);

            // TODO: instead of PascalInteger, do the smallest integer type that applies.
            return new PascalInteger(l.multiply(r));
        }

        throw new UnsupportedOperationException(
                "Unsupported multiplication: " + lhs.toString() + " * " + rhs.toString());
    }

    private static PascalType divide(PascalType lhs, PascalType rhs) {
        // Numbers divide, coerce the types and then do the operation
        if (lhs instanceof PascalReal || rhs instanceof PascalReal) {
            var l = coerceType(BigDecimal.class, lhs);
            var r = coerceType(BigDecimal.class, rhs);
            return new PascalReal(l.divide(r));
        }
        if (lhs instanceof PascalInteger || rhs instanceof PascalInteger) {
            var l = coerceType(BigInteger.class, lhs);
            var r = coerceType(BigInteger.class, rhs);

            // TODO: instead of PascalInteger, do the smallest integer type that applies.
            return new PascalInteger(l.divide(r));
        }

        throw new UnsupportedOperationException(
                "Unsupported division: " + lhs.toString() + " / " + rhs.toString());
    }

    private static PascalType mod(PascalType lhs, PascalType rhs) {
        // Numbers do mod (not reals tho), coerce the types and then do the operation
        if (lhs instanceof PascalInteger || rhs instanceof PascalInteger) {
            var l = coerceType(BigInteger.class, lhs);
            var r = coerceType(BigInteger.class, rhs);

            // TODO: instead of PascalInteger, do the smallest integer type that applies.
            return new PascalInteger(l.mod(r));
        }

        throw new UnsupportedOperationException(
                "Unsupported mod: " + lhs.toString() + " % " + rhs.toString());
    }

    private static PascalType and(PascalType lhs, PascalType rhs) {
        if (lhs instanceof PascalBoolean && rhs instanceof PascalBoolean) {
            boolean result = ((PascalBoolean) lhs).getValue() && ((PascalBoolean) rhs).getValue();
            return new PascalBoolean(result);
        }
        throw new UnsupportedOperationException("AND operation only supports Boolean types");
    }

    private static PascalType not(PascalType operand) {
        if (operand instanceof PascalBoolean) {
            boolean result = !((PascalBoolean) operand).getValue();
            return new PascalBoolean(result);
        }
        throw new UnsupportedOperationException("NOT operation only supports Boolean type");
    }

    private static PascalType negate(PascalType operand) {
        if (operand instanceof PascalInteger) {
            BigInteger result = ((PascalInteger) operand).getValue().negate();
            /*
             * Using reflection to dynamically build the proper output type based on the
             * input subclass type. Requires each subclass possible to have an identical
             * constructor signature, which we do in this case.
             */
            try {
                return operand.getClass().getDeclaredConstructor(BigInteger.class).newInstance(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (operand instanceof PascalReal) {
            BigDecimal result = ((PascalReal) operand).getValue().negate();
            return new PascalReal(result);
        }
        throw new UnsupportedOperationException("NEGATE operation only supports Numeric types");
    }

    /**
     * If possible, turns the input into the target type.
     * Throws error if not possible.
     * 
     * @param targetType
     * @param input
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> T coerceType(Class<T> targetType, PascalType input) {
        // Already right type, do nothing
        if (targetType.isInstance(input)) {
            return (T) input;
        }

        return switch (targetType.getSimpleName()) {
            case "String" -> (T) input.toString();
            case "Boolean", "boolean" -> (T) Boolean.valueOf(parseBoolean(input.getValue()));
            case "BigInteger" -> (T) parseBigDecimal(input.getValue()).toBigInteger();
            case "BigDecimal" -> (T) parseBigDecimal(input.getValue());

            default ->
                throw new UnsupportedOperationException(
                        "Unsupported coercion: " + input.getClass().getSimpleName() + " → "
                                + targetType.getSimpleName());
        };
    }

    /**
     * Attempts to convert Object value to a BigDecimal.
     * 
     * @param value the value to convert
     * @return the value as a BigDecimal
     * @throws IllegalArgumentException if the value cannot be converted
     */
    private static BigDecimal parseBigDecimal(Object value) throws IllegalArgumentException {
        return switch (value) {
            case BigDecimal bd -> bd;
            case BigInteger bi -> new BigDecimal(bi);
            case Number num -> BigDecimal.valueOf(num.doubleValue());
            case Boolean bool -> bool ? BigDecimal.ONE : BigDecimal.ZERO;
            case String str -> {
                try {
                    yield new BigDecimal(str);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot coerce String to a BigDecimal: " + str);
                }
            }
            default -> throw new IllegalArgumentException(
                    "Cannot convert " + value.getClass().getSimpleName() + " to BigDecimal");
        };
    }

    /**
     * Attempts to convert Object value to a Boolean.
     * 
     * @param value the value to convert
     * @return the value as a Boolean
     * @throws IllegalArgumentException if the value cannot be converted
     */
    private static Boolean parseBoolean(Object value) throws IllegalArgumentException {
        return switch (value) {
            case BigInteger bi -> bi == BigInteger.ONE;
            case BigDecimal bd -> bd == BigDecimal.ONE;
            case Number num -> num.doubleValue() != 0;
            case Boolean bool -> bool;
            case String str -> {
                String s = str.trim().toLowerCase();
                yield s.equals("true") || s.equals("1");
            }
            default -> throw new IllegalArgumentException(
                    "Cannot convert " + value.getClass().getSimpleName() + " to Boolean");
        };
    }

}
