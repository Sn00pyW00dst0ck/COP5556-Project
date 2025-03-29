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

    // TODO: override the comparisons within 'expression' to evaluate further...
    // TODO: unit test...

    @Override
    public String visitSimpleExpression(delphi.SimpleExpressionContext ctx) {
        String lhs = visit(ctx.term()).trim();
        String op = (ctx.additiveoperator() != null) ? visit(ctx.additiveoperator()).trim().toLowerCase() : "";
        String rhs = (ctx.simpleExpression() != null) ? visit(ctx.simpleExpression()).trim() : "";

        // Try to do the constant evaluation, if fail for any reason fallback to the default visit...
        try {
            return switch (op) {
                case "+" -> {
                    // Attempt to evaluate as concatenation (character cast is tricky)
                    try {
                        if (lhs.charAt(0) != '\'' && lhs.charAt(lhs.length() - 1) != '\'') {
                            if (!lhs.toLowerCase().replaceAll("\\s+", "").startsWith("chr(")) {
                                throw new RuntimeException("Can't evaluate as concatenation!");
                            }
                            lhs = Character.valueOf((char) new BigInteger(lhs.replaceAll("[^0-9]", "")).intValue()).toString();
                        } else {
                            lhs = lhs.substring(1, lhs.length() - 1);
                        }
                        if (rhs.charAt(0) != '\'' && rhs.charAt(rhs.length() - 1) != '\'') {
                            if (!rhs.toLowerCase().replaceAll("\\s+", "").startsWith("chr(")) {
                                throw new RuntimeException("Can't evaluate as concatenation!");
                            }
                            rhs = Character.valueOf((char) new BigInteger(rhs.replaceAll("[^0-9]", "")).intValue()).toString();
                        } else {
                            rhs = rhs.substring(1, rhs.length() - 1);
                        }
                        yield "'" + (lhs + rhs) + "'";
                    } catch (Exception e) {}

                    // Attempt to evaluate as integers
                    try {
                        yield (new BigInteger(lhs)).add(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).add(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    throw new RuntimeException("Can't evaluate, fallback!");
                }
                case "-" -> {
                    // Attempt to evaluate as integers
                    try {
                        yield (new BigInteger(lhs)).subtract(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).subtract(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // If both fail, then we really failed...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "or" -> {
                    lhs = lhs.toLowerCase();
                    rhs = rhs.toLowerCase();

                    // Here we evaluate two booleans if possible and then replace the result...
                    if (!lhs.equals("true") && !lhs.equals("false")) {
                        throw new RuntimeException("Can't cast to bool, fallback!");
                    }

                    if (!rhs.equals("true") && !rhs.equals("false")) {
                        throw new RuntimeException("Can't cast to bool, fallback!");
                    }

                    yield Boolean.valueOf(Boolean.valueOf(lhs) || Boolean.valueOf(rhs)).toString();
                }
                default -> lhs;
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
                        yield (new BigInteger(lhs)).multiply(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).multiply(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // If both fail, then we really failed...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "/" -> {
                    // Attempt to evaluate as integers
                    try {
                        yield (new BigInteger(lhs)).divide(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).divide(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}
                    
                    // If both fail, then we really failed...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "div" -> {
                    // Attempt to evaluate as integers
                    try {
                        yield (new BigInteger(lhs)).divide(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).divide(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}
                    
                    // If both fail, then we really failed...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "mod" -> {
                    // Attempt to evaluate as integers
                    try {
                        yield (new BigInteger(lhs)).remainder(new BigInteger(rhs)).toString();
                    } catch (NumberFormatException e) {}

                    // Attempt to evaluate as decimals
                    try {
                        yield (new BigDecimal(lhs)).remainder(new BigDecimal(rhs)).toString();
                    } catch (NumberFormatException e) {}
                    
                    // If both fail, then we really failed...
                    throw new RuntimeException("Can't evaluate as numbers, fallback!");
                }
                case "and" -> {
                    lhs = lhs.toLowerCase();
                    rhs = rhs.toLowerCase();

                    // Here we evaluate two booleans if possible and then replace the result...
                    if (!lhs.equals("true") && !lhs.equals("false")) {
                        throw new RuntimeException("Can't cast to bool, fallback!");
                    }

                    if (!rhs.equals("true") && !rhs.equals("false")) {
                        throw new RuntimeException("Can't cast to bool, fallback!");
                    }
                    yield Boolean.valueOf(Boolean.valueOf(lhs) && Boolean.valueOf(rhs)).toString();
                }
                default -> lhs;
            };
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    @Override
    public String visitFactor(delphi.FactorContext ctx) {
        try {
            if (ctx.NOT() != null) {
                String value = visit(ctx.factor()).trim().toLowerCase();
                if (!value.equals("true") && !value.equals("false")) {
                    throw new RuntimeException("Can't cast to bool, fallback!");
                }
    
                return Boolean.valueOf(!Boolean.valueOf(value)).toString();
            }

            throw new RuntimeException("Can't constant fold, fallback!");
        } catch (Exception e) {
            return visitChildren(ctx);
        }
    }

    //#endregion Expressions

}
