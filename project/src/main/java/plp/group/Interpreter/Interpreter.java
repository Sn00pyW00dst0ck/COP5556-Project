package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;
import plp.group.project.delphiParser.AdditiveoperatorContext;
import plp.group.project.delphiParser.Bool_Context;
import plp.group.project.delphiParser.MultiplicativeoperatorContext;
import plp.group.project.delphiParser.RelationaloperatorContext;

/**
 * Interpret valid delphi code according to our grammar.
 * 
 * We override the delphiBaseVisitor with type <Object> so that
 * we can return any type from each function (useful for dealing)
 * with many types of literals.
 * 
 * Below interpreter is a minimum required to interpret the hello_world.pas
 * file.
 * It needs the creation/deletion of scopes in a proper way in order to be 100%
 * correct though,
 * but this can write to the screen the 'Hello World' string properly.
 */
public class Interpreter extends delphiBaseVisitor<Object> {

    private SymbolTable scope = new SymbolTable();

    public Interpreter() {
        scope.enterScope();
        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html

        // #region Built-In IO Functions

        Scanner scanner = new Scanner(System.in);

        scope.insert("read", new SymbolInfo(
                "read",
                (List<Object> arguments) -> {
                    // FIGURE OUT THIS

                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("readln", new SymbolInfo(
                "readln",
                (List<Object> arguments) -> {
                    // FIGURE OUT THIS
                    scanner.nextLine();
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("write", new SymbolInfo(
                "write",
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("writeln", new SymbolInfo(
                "writeln",
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    System.out.println("");
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        // #endregion Built-In IO Functions
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        // The program block introduces the global scope
        scope.enterScope();
        visitChildren(ctx);
        scope.exitScope();
        return null;
    }

    // #region Declarations

    /**
     * Inserts each of the identifiers within the variable declaration to the
     * current scope with no value but specified type.
     * 
     * Returns null.
     */
    @Override
    public Object visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        var type = getJavaType(ctx.getChild(ctx.getChildCount() - 1).getText());

        // For each identifier, insert it to the current scope with the type set.
        var identifiers = (ArrayList<String>) visit(ctx.getChild(0));
        for (String ident : identifiers) {
            scope.insert(ident, new SymbolInfo(ident, null, type));
        }

        return null;
    }

    /**
     * Inserts the procedure into the current scope. Does NOT execute the procedure.
     * 
     * Returns null.
     */
    @Override
    public Object visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) { // TODO: PROPERLY IMPLEMENT
        return visitChildren(ctx);
        /*
         * var procedureName = (SymbolInfo) visit(ctx.getChild(1));
         * 
         * var paramsList = visit(ctx.getChild(2)); // TODO: implement
         * visitFormalParameterList
         * 
         * var body = ctx.getChild(4);
         * 
         * SymbolTable capturedScope = scope;
         * scope.insert(procedureName.name, new SymbolInfo(
         * procedureName.name,
         * (List<Object> arguments) -> {
         * // Create the new function scope (store current scope to restore later)
         * SymbolTable currentScope = scope;
         * scope = capturedScope;
         * scope.enterScope();
         * 
         * try {
         * // Add the arguments
         * for (int i = 0; i < arguments.size(); i++) {
         * scope.insert(null, new SymbolInfo(null, arguments.get(i), null)); // TODO:
         * add type info
         * }
         * 
         * // Visit the body
         * visit(body);
         * } finally { // TODO: Error handling here??
         * // Restore scope no matter what
         * scope.exitScope();
         * scope = currentScope;
         * }
         * 
         * return null;
         * },
         * List.of(Object[].class), // TODO: update the list of arguments here to be
         * correct...
         * Void.class));
         * 
         * return null;
         */
    }

    @Override
    public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) { // TODO: PROPERLY IMPLEMENT
        return visitChildren(ctx);
    }

    // #endregion Declarations

    // #region Identifiers

    /**
     * Returns the SymbolInfo for the variable.
     */
    @Override
    public Object visitVariable(delphiParser.VariableContext ctx) {
        var name = (String) visit(ctx.getChild(0));
        return scope.lookup(name);
    }

    /**
     * Returns the result of visiting all the identifiers in a list.
     */
    @Override
    public Object visitIdentifierList(delphiParser.IdentifierListContext ctx) {
        List<String> identifiers = new ArrayList<String>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) { // Iterate by 2s to skip each COMMA
            identifiers.add((String) visit(ctx.getChild(i)));
        }
        return identifiers;
    }

    /**
     * Returns a string of the identifier name.
     */
    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        return ctx.getText();
    }

    // #endregion Identifiers

    // Related to procedures

    // #region Statements

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        // Get the procedure name/details
        var procedureDetails = (SymbolInfo) scope.lookup(((String) visit(ctx.getChild(0))));

        // Visit parameters to get values to add to the parameters list
        var parameters = new ArrayList<Object>();
        for (var i = 2; i < ctx.getChildCount() - 1; i++) {
            var parameter = visit(ctx.getChild(i));
            if (parameter instanceof SymbolInfo) {
                parameter = ((SymbolInfo) parameter).value; // If parameter is a SymbolInfo, grab its value
            }
            parameters.add(parameter);
        }

        // Call the function and return the result.
        procedureDetails.function.apply(parameters);
        return null;
    }

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        var identifier = (SymbolInfo) visit(ctx.getChild(0));
        var value = (SymbolInfo) visit(ctx.getChild(2));

        scope.update(identifier.name, new SymbolInfo(identifier.name, value.value, value.returnType));
        return null;
    }

    // #endregion Statements

    // #region Expressions

    /**
     * Visits the expression, returns whatever the value of the expression is.
     * 
     * The value is returned within a SymbolInfo (with no name).
     */
    @Override
    public Object visitExpression(delphiParser.ExpressionContext ctx) {
        var lhs = (SymbolInfo) visit(ctx.getChild(0));

        if (ctx.relationaloperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.relationaloperator());
        var rhs = (SymbolInfo) visit(ctx.getChild(2));

        // TODO: add logic for missing operations.
        /*
         * TODO: Write a test case to test all the below operators to ensure that type
         * safety is handled
         */
        return switch (operator) {
            case "=" -> lhs.value.equals(rhs.value);
            case "<>" -> !(lhs.value.equals(rhs.value));
            case "<" -> {
                // case for strings, by lexical/alphabetical order
                if (lhs.value.getClass() == String.class && rhs.value.getClass() == String.class) {
                    yield ((String) (lhs.value)).compareTo((String) (rhs.value)) < 0;
                }

                // booleans & numbers all coerce to BigDecimal
                yield coerceToType(lhs.value, BigDecimal.class)
                        .compareTo(coerceToType(rhs.value, BigDecimal.class)) < 0;
            }
            case "<=" -> {
                // case for strings, by lexical/alphabetical order
                if (lhs.value.getClass() == String.class && rhs.value.getClass() == String.class) {
                    yield ((String) (lhs.value)).compareTo((String) (rhs.value)) <= 0;
                }

                // booleans & numbers all coerce to BigDecimal
                yield coerceToType(lhs.value, BigDecimal.class)
                        .compareTo(coerceToType(rhs.value, BigDecimal.class)) <= 0;
            }
            case ">" -> {
                // case for strings, by lexical/alphabetical order
                if (lhs.value.getClass() == String.class && rhs.value.getClass() == String.class) {
                    yield ((String) (lhs.value)).compareTo((String) (rhs.value)) > 0;
                }

                // booleans & numbers all coerce to BigDecimal
                yield coerceToType(lhs.value, BigDecimal.class)
                        .compareTo(coerceToType(rhs.value, BigDecimal.class)) > 0;
            }
            case ">=" -> {
                // case for strings, by lexical/alphabetical order
                if (lhs.value.getClass() == String.class && rhs.value.getClass() == String.class) {
                    yield ((String) (lhs.value)).compareTo((String) (rhs.value)) >= 0;
                }

                // booleans & numbers all coerce to BigDecimal
                yield coerceToType(lhs.value, BigDecimal.class)
                        .compareTo(coerceToType(rhs.value, BigDecimal.class)) >= 0;
            }
            case "IN" -> throw new UnsupportedOperationException("NOT IMPLEMENTED 'IN' YET");
            default -> throw new RuntimeException("Unhandled or unknown operator: " + operator);
        };
    }

    /**
     * Get the string representation (text) of the relational operator.
     */
    @Override
    public Object visitRelationaloperator(RelationaloperatorContext ctx) {
        return ctx.getText();
    }

    /**
     * Visits the simpleExpression, returns whatever the value of the
     * simpleExpression is.
     */
    @Override
    public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
        var lhs = (SymbolInfo) visit(ctx.getChild(0));

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.additiveoperator());
        var rhs = (SymbolInfo) visit(ctx.getChild(2));

        /*
         * TODO: Write a test case to test all the below operators to ensure that type
         * safety is handled
         */
        var value = switch (operator) {
            case "+" -> {
                // If both strings, concatenate
                if (lhs.value.getClass() == String.class && rhs.value.getClass() == String.class) {
                    yield ((String) (lhs.value)) + ((String) (rhs.value));
                }

                // If both are BigIntegers, use that to add
                // Otherwise, coerce both types into BigDecimals then add
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).add((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class).add(coerceToType(rhs.value, BigDecimal.class));
            }
            case "-" -> {
                // If both are BigIntegers, use that to subtract
                // Otherwise, coerce both types into BigDecimals then subtract
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).subtract((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class).subtract(coerceToType(rhs.value, BigDecimal.class));
            }
            case "OR" -> ((Boolean) (lhs.value)) || ((Boolean) (rhs.value));
            default -> throw new RuntimeException("Unhandled or unknown operator: " + operator);
        };
        return new SymbolInfo(null, value, value.getClass());
    }

    /**
     * Get the string representation (text) of the additive operator.
     */
    @Override
    public Object visitAdditiveoperator(AdditiveoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitTerm(delphiParser.TermContext ctx) {
        var lhs = (SymbolInfo) visit(ctx.getChild(0));

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.multiplicativeoperator());
        var rhs = (SymbolInfo) visit(ctx.getChild(2));

        /*
         * TODO: Write a test case program to test all the below operators to ensure
         * that type safety is handled correctly in all cases we can think of.
         */
        var value = switch (operator) {
            case "*" -> {
                // If both are BigIntegers, use that to multiply
                // Otherwise, coerce both types into BigDecimals then mulitply
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).multiply((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class).multiply(coerceToType(rhs.value, BigDecimal.class));
            }
            case "/" -> {
                // If both are BigIntegers, use that to divide
                // Otherwise, coerce both types into BigDecimals then divide
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).divide((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class).divide(coerceToType(rhs.value, BigDecimal.class),
                                8, RoundingMode.HALF_UP); // Need rounding mode to prevent errors with infinite decimals
            }
            case "MOD" -> {
                // If both are BigIntegers, use that to mod
                // Otherwise, coerce both types into BigDecimals then remainder
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).mod((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class)
                                .remainder(coerceToType(rhs.value, BigDecimal.class));
            }
            case "DIV" -> {
                // If both are BigIntegers, use that to divide
                // Otherwise, coerce both types into BigDecimals then divide
                yield (lhs.value.getClass() == BigInteger.class && rhs.value.getClass() == BigInteger.class)
                        ? ((BigInteger) (lhs.value)).divide((BigInteger) (rhs.value))
                        : coerceToType(lhs.value, BigDecimal.class).divide(coerceToType(rhs.value, BigDecimal.class));
            }
            case "AND" -> ((Boolean) (lhs.value)) && ((Boolean) (rhs.value));
            default -> throw new RuntimeException("Unhandled or unknown operator: " + operator);
        };
        return new SymbolInfo(null, value, value.getClass());
    }

    /**
     * Get the string representation (text) of the multiplicative operator.
     */
    @Override
    public Object visitMultiplicativeoperator(MultiplicativeoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitSignedFactor(delphiParser.SignedFactorContext ctx) {
        // TODO: figure out how to grab sign and apply to factor
        // if (ctx.PLUS() != null || ctx.MINUS() != null) {
        //
        // }
        var factor = visit(ctx.factor());
        return factor;
    }

    /**
     * Returns a SymbolInfo with the information regarding this thing.
     * If it is not in the scope it will have no name associated with it.
     */
    @Override
    public Object visitFactor(delphiParser.FactorContext ctx) {
        int childIndex = (ctx.LPAREN() != null || ctx.NOT() != null) ? 1 : 0;
        var result = visit(ctx.getChild(childIndex));

        if (ctx.NOT() != null) {
            result = !((Boolean) result);
        }

        // Convert any literals to SymbolInfo
        // (for uniform interfaces in things above 'factor')
        if (!(result instanceof SymbolInfo)) {
            result = new SymbolInfo(null, result, result.getClass());
        }

        return result;

        // return visitChildren(ctx);
        // TODO: actually do all the below in their own visit functions...
        /*
         * factor
         * : variable
         * | LPAREN expression RPAREN
         * | functionDesignator
         * | unsignedConstant
         * | set_
         * | NOT factor
         * | bool_
         * ;
         */
    }

    // #endregion Expressions

    // #region Literals

    @Override
    public Object visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return literal.substring(1, literal.length() - 1);
    }

    @Override
    public Object visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
        return new BigInteger(ctx.NUM_INT().toString());
    }

    @Override
    public Object visitUnsignedReal(delphiParser.UnsignedRealContext ctx) {
        return new BigDecimal(ctx.NUM_REAL().toString());
    }

    @Override
    public Object visitConstantChr(delphiParser.ConstantChrContext ctx) {
        var value = (BigInteger) visit(ctx.getChild(2));
        return String.valueOf(value.intValue());
    }

    @Override
    public Object visitBool_(Bool_Context ctx) {
        return Boolean.valueOf(ctx.getText().toLowerCase());
    }

    // #endregion Literals

    // #region Helper Functions

    /**
     * Attempts to convert the given value into the given targetType.
     * 
     * @param <T>        the type class that we coerce to
     * @param value      the value to try to coerce
     * @param targetType the target type to coerce to
     * @return the coerced type
     * @throws IllegalArgumentException if the value cannot be converted
     */
    @SuppressWarnings("unchecked")
    private static <T> T coerceToType(Object value, Class<T> targetType) throws IllegalArgumentException {
        if (value == null || targetType == null) {
            throw new IllegalArgumentException(
                    "Passed null to type coercion! value=" + value + " : targetType=" + targetType);
        }

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // TODO: add more types here as needed.
        // TODO: ensure that all type coercions below are valid.
        return switch (targetType.getSimpleName()) {
            case "String" -> (T) value.toString();
            case "Boolean", "boolean" -> (T) Boolean.valueOf(parseBoolean(value));
            case "BigInteger" -> (T) parseBigDecimal(value).toBigInteger();
            case "BigDecimal" -> (T) parseBigDecimal(value);
            default -> throw new IllegalArgumentException(
                    "Unsupported coercion: " + value.getClass().getSimpleName() + " → " + targetType.getSimpleName());
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

    // Below might not be needed??
    private Class<?> getJavaType(String type) {
        return switch (type.toLowerCase()) {
            // Numeric types
            case "integer" -> int.class;
            case "cardinal" -> long.class;
            case "shortint" -> byte.class;
            case "smallint" -> short.class;
            case "longint" -> long.class;
            case "int64" -> long.class;
            case "byte" -> short.class;
            case "word" -> char.class;
            case "longword" -> long.class;

            // Other basic types
            case "real" -> BigDecimal.class;
            case "char" -> char.class;
            case "boolean" -> boolean.class;

            // Handle strings, arrays, etc
            case "string" -> String.class;

            // Handle user defined types...

            default -> throw new IllegalStateException("Invalid type: " + type);
        };
    }

    // #endregion Helper Functions

}
