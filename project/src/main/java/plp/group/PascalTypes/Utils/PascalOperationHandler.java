package plp.group.PascalTypes.Utils;

import plp.group.PascalTypes.PascalType;
import plp.group.PascalTypes.Scalars.Standard.PascalBoolean;
import plp.group.PascalTypes.Scalars.Standard.PascalInteger;
import plp.group.PascalTypes.Scalars.Standard.PascalReal;

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
            case "%" -> mod(lhs, rhs);
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
        return null;
    }

    private static PascalType subtract(PascalType lhs, PascalType rhs) {
        return null;
    }

    private static PascalType or(PascalType lhs, PascalType rhs) {
        if (lhs instanceof PascalBoolean && rhs instanceof PascalBoolean) {
            boolean result = ((PascalBoolean) lhs).getValue() || ((PascalBoolean) rhs).getValue();
            return new PascalBoolean(result);
        }
        throw new UnsupportedOperationException("OR operation only supports Boolean types");
    }

    private static PascalType multiply(PascalType lhs, PascalType rhs) {
        return null;
    }

    private static PascalType divide(PascalType lhs, PascalType rhs) {
        return null;
    }

    private static PascalType mod(PascalType lhs, PascalType rhs) {
        return null;
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
        return null;
    }
}
