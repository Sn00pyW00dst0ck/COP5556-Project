package plp.group.Optimizer;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;

/**
 * This is the optimizer, which performs constant folding on the tree and returns the string representation fo the folded tree. 
 * 
 * Right now, the following code foldings are applied: 
 *  - Constant literal values are evaluated as much as possible, with the exception of relational operators.
 *  - Functions, Variables, and other fancy things are NOT evaluated at all (even if the variable value never changes).
 *  
 */
public class Optimizer extends delphiBaseVisitor<String> {
    
    //#region String Accumulations

    /**
     * Visits the intermediate levels of the node and concatenates their string representations.
     */
    @Override
    public String visitChildren(RuleNode node) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            result.append(visit(node.getChild(i)));
            if (i < node.getChildCount() - 1) {
                result.append(" "); // This makes things 'just work'.
            }
        }
        return result.toString();
    }

    /**
     * Visits the bottom level nodes of the tree and gets their text representations.
     */
    @Override
    public String visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == delphi.EOF) { return ""; }
        return node.getText();
    }

    //#endregion String Accumulations

    //#region Expressions

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String visitExpression(delphi.ExpressionContext ctx) {
        String lhs = visit(ctx.simpleExpression()).trim();
        String op = (ctx.relationaloperator() != null) ? visit(ctx.relationaloperator()).trim().toLowerCase() : "";
        String rhs = (ctx.expression() != null) ? visit(ctx.expression()).trim() : "";

        try {
            Comparable lhsValue = null;
            Comparable rhsValue = null;

            try {
                lhsValue = requireType(lhs, Boolean.class);
                rhsValue = requireType(rhs, Boolean.class);
            } catch (Exception e) {}

            try {
                lhsValue = requireType(lhs, BigInteger.class);
                rhsValue = requireType(rhs, BigInteger.class);
            } catch (Exception e) {}

            try {
                lhsValue = requireType(lhs, BigDecimal.class);
                rhsValue = requireType(rhs, BigDecimal.class);
            } catch (Exception e) {}

            try {
                lhsValue = requireType(lhs, String.class);
                rhsValue = requireType(rhs, String.class);
            } catch (Exception e) {}

            try {
                lhsValue = requireType(lhs, Character.class);
                rhsValue = requireType(rhs, Character.class);
            } catch (Exception e) {}

            return switch (op) {
                case "=" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) == 0).toString();
                }
                case "<>" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) != 0).toString();
                }
                case "<" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) < 0).toString();
                }
                case "<=" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) <= 0).toString();
                }
                case ">" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) > 0).toString();
                }
                case ">=" -> {
                    yield Boolean.valueOf(lhsValue.compareTo(rhsValue) >= 0).toString();
                }
                default -> throw new RuntimeException("Unexpected operator: " + op);
            };
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    @Override
    public String visitSimpleExpression(delphi.SimpleExpressionContext ctx) {
        String lhs = visit(ctx.term()).trim();
        String op = (ctx.additiveoperator() != null) ? visit(ctx.additiveoperator()).trim().toLowerCase() : "";
        String rhs = (ctx.simpleExpression() != null) ? visit(ctx.simpleExpression()).trim() : "";

        // Try to do the constant evaluation, if fail for any reason fallback to the default visit...
        try {
            return switch (op) {
                case "+" -> {
                    // Attempt to evaluate as concatenation (there are 4 cases becuase string and character)
                    try {
                        String result = requireType(lhs, String.class) + requireType(rhs, String.class);
                        yield "'" + result + "'";
                    } catch (Exception e) {}

                    try {
                        String result = requireType(lhs, String.class) + requireType(rhs, Character.class).toString();
                        yield "'" + result + "'";
                    } catch (Exception e) {}

                    try {
                        String result = requireType(lhs, Character.class).toString() + requireType(rhs, String.class).toString();
                        yield "'" + result + "'";
                    } catch (Exception e) {}   

                    try {
                        String result = requireType(lhs, Character.class).toString() + requireType(rhs, Character.class).toString();
                        yield "'" + result + "'";
                    } catch (Exception e) {}   

                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).add(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).add(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;                    } catch (Exception e) {}

                    // If ALL fail, then we can't do it...
                    throw new RuntimeException("Can't evaluate, fallback!");
                }
                case "-" -> {
                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).subtract(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).subtract(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;
                    } catch (Exception e) {}

                    // If both fail, then we can't do it...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "or" -> {
                    Boolean value = requireType(lhs.toLowerCase(), Boolean.class) || requireType(rhs.toLowerCase(), Boolean.class);
                    yield value.toString();
                }
                default -> throw new RuntimeException("Unexpected operator: " + op);
            };
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    @Override
    public String visitTerm(delphi.TermContext ctx) {
        String lhs = visit(ctx.signedFactor()).trim();
        String op = (ctx.multiplicativeoperator() != null) ? visit(ctx.multiplicativeoperator()).trim().toLowerCase() : "";
        String rhs = (ctx.term() != null) ? visit(ctx.term()).trim() : "";

        // Try to do the constant evaluation, if fail for any reason fallback to the default visit...
        try {
            return switch (op) {
                case "*" -> {
                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).multiply(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).multiply(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;
                    } catch (Exception e) {}

                    // If both fail, then we can't do it...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "/" -> {
                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).divide(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).divide(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;
                    } catch (Exception e) {}

                    // If both fail, then we can't do it...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "div" -> {
                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).divide(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).divide(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;
                    } catch (Exception e) {}

                    // If both fail, then we can't do it...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "mod" -> {
                    // Attempt to evaluate as integers
                    try {
                        BigInteger result = requireType(lhs, BigInteger.class).remainder(requireType(rhs, BigInteger.class));
                        yield result.toString();
                    } catch (Exception e) {}

                    // Attempt to evaluate as decimals
                    try {
                        String result = requireType(lhs, BigDecimal.class).remainder(requireType(rhs, BigDecimal.class)).toString();
                        if (!result.contains(".")) {
                            result += ".0";
                        }
                        yield result;
                    } catch (Exception e) {}
                    
                    // If both fail, then we can't do it..
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "and" -> {
                    Boolean value = requireType(lhs.toLowerCase(), Boolean.class) && requireType(rhs.toLowerCase(), Boolean.class);
                    yield value.toString();
                }
                default -> throw new RuntimeException("Unexpected operator: " + op);
            };
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    /**
     * Signed factor override to prevent a space between sign and the factor.
     */
    @Override
    public String visitSignedFactor(delphi.SignedFactorContext ctx) {
        String sign = "";
        if (ctx.MINUS() != null) { 
            sign += ctx.MINUS().getText();
        } else if (ctx.PLUS() != null) {
            sign += ctx.PLUS().getText();
        }
        return sign + visit(ctx.factor());
    }

    @Override
    public String visitFactor(delphi.FactorContext ctx) {
        try {
            if (ctx.NOT() != null) {                
                Boolean result = !requireType(visit(ctx.factor()).trim().toLowerCase(), Boolean.class);
                return result.toString();
            }

            throw new RuntimeException("Can't constant fold, fallback!");
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    //#endregion Expressions

    //#region Helpers

    /**
     * Given an instance of a string, force it to the given type. 
     * 
     * @param <T> 
     * @param value the value to cast
     * @param type the type to cast to (one of delphi's primitive types)
     * @return the value as the specific type, if the Delphi rules allow the type cast...
     */
    private static <T> T requireType(String value, Class<T> type) {
        if (value == null || value == "") {
            throw new RuntimeException("Unsupported type: " + type.getName());
        }

        // Handle case of the value being wrapped in parenthesis, we want to ignore those characters...
        while (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        

        return switch (type.getSimpleName()) {
            case "Boolean" -> {
                if (!value.equals("true") && !value.equals("false")) {
                    throw new RuntimeException(value + " is not a " + type.getSimpleName());
                }
                yield type.cast(Boolean.valueOf(value));
            }
            case "BigInteger" -> type.cast(new BigInteger(value));
            case "BigDecimal" -> type.cast(new BigDecimal(value));
            case "Character" -> {
                if (!value.toLowerCase().replaceAll("\\s+", "").startsWith("chr(")) {
                    throw new RuntimeException(value + "is not a " + type.getSimpleName());
                }
                yield type.cast(Character.valueOf((char) new BigInteger(value.replaceAll("[^0-9]", "")).intValue()));
            }
            case "String" -> {
                if (value.charAt(0) != '\'' && value.charAt(value.length() - 1) != '\'') {
                    throw new RuntimeException(value + " is not a " + type.getSimpleName());
                }
                yield type.cast(value.substring(1, value.length() - 1));
            }

            default -> throw new IllegalArgumentException("Unsupported type: " + type.getName());
        };
    }

    //#endregion Helpers

}
