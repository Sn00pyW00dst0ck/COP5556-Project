package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphi.ClassMemberDeclarationContext;
import plp.group.Interpreter.ControlFlowExceptions.BreakException;
import plp.group.Interpreter.ControlFlowExceptions.ContinueException;
import plp.group.Interpreter.ControlFlowExceptions.ReturnException;

/**
 * The interpreter that walks the tree and does the actual calculations/running of the program.
 */
public class Interpreter extends delphiBaseVisitor<Object> {

    /**
     * The scope of the interpreter as it is running.
     * 
     * The interpreter starts in the global scope, and the parent of the global scope is the built in functions. 
     */
    private Scope scope = new Scope(Optional.of(Environment.scope()));

    /*
     * TODO: 
     *  1. Built in function definitions (read and readln).
     *  2. visitVariable
     *  3. Statements (broad / biggest thing)
     *      3.5. Break/continue keywords.
     *  4. Function/Procedure Definitions.
     *  5. Class Definitions / Usage -> Pair program? 
     *  6. Other types we had in P1 (range, enum, etc).
     * 
     *  * Write unit tests!
     */

    //#region Types

    @Override
    public RuntimeValue visitTypeDefinition(delphi.TypeDefinitionContext ctx) {
        String typeName = ctx.identifier().getText();

        RuntimeValue typeDefinition = switch (ctx.getChild(ctx.getChildCount() - 1)) {
            case delphi.Type_Context typeContext -> visitType_(typeContext);
            case delphi.FunctionTypeContext functionTypeContext -> visitFunctionType(functionTypeContext);
            case delphi.ProcedureTypeContext procedureTypeContext -> visitProcedureType(procedureTypeContext);
            // case delphi.ClassTypeContext classTypeContext -> {
            //     RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(visitClassType(classTypeContext), RuntimeValue.ClassDefinition.class);
            //     // Copy over the scopes we found into the ClassDefinition with the proper name.
            //     yield new RuntimeValue.ClassDefinition(
            //         typeName, 
            //         classDefinition.privateScope(), 
            //         classDefinition.protectedScope(), 
            //         classDefinition.publicScope()
            //     );
            // }
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'type definition'.");
        };

        // Add the new typeName to the scope with correct typeDefinition
        scope.define(typeName, typeDefinition);

        return new RuntimeValue.Primitive(null);
    }
    /*
    @Override
    public RuntimeValue visitClassType(delphi.ClassTypeContext ctx) {
        // Because of the different visibility sections we have to have three separate scopes.
        // This complicates visiting visibility sections and their children, so I do it all here.
        // Using the 'parent' of scopes to make sure that lookup within private scope can gurantee checking public scope too.
        Scope publicScope = new Scope(Optional.of(Environment.scope()));
        Scope protectedScope = new Scope(Optional.of(publicScope));
        Scope privateScope = new Scope(Optional.of(protectedScope));

        // Visit the children and add things to the scope...
        for (delphi.VisibilitySectionContext sectionCtx : ctx.visibilitySection()) {
            // Updating currentScope will update the correct scope above! 
            Scope visibilityScope = switch (sectionCtx.getChild(0)) {
                case TerminalNode t when(t.getSymbol().getType() == delphi.PRIVATE) -> privateScope;
                case TerminalNode t when(t.getSymbol().getType() == delphi.PROTECTED) -> protectedScope;
                case TerminalNode t when(t.getSymbol().getType() == delphi.PUBLIC) -> publicScope;
                default -> throw new RuntimeException("Unexpected visibility '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'class type'.");
            };

            // For each class member, we add it to the visibilityScope...
            for (ClassMemberDeclarationContext memberCtx : sectionCtx.classMemberDeclaration()) {
                switch (memberCtx.getChild(0)) {
                    case delphi.ClassFieldDeclarationContext fieldCtx -> 
                        visibilityScope.define(fieldCtx.identifier().getText(), visitType_(fieldCtx.type_()));
                    case delphi.ClassProcedureDeclarationContext procedureCtx -> {
                        String procedureName = procedureCtx.identifier().getText();
                        LinkedHashMap<String, RuntimeValue> parameterTypes = visitFormalParameterList(procedureCtx.formalParameterList());
                        parameterTypes.putFirst("this", new RuntimeValue.ClassInstance(null, null, null, null));

                        visibilityScope.define(
                            procedureName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                procedureName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    new ArrayList<RuntimeValue>(parameterTypes.values()),
                                    new RuntimeValue.Primitive(null)
                                ), 
                                null
                            )
                        );
                    }
                    case delphi.ClassFunctionDeclarationContext functionCtx -> {
                        String functionName = functionCtx.identifier().getText();
                        LinkedHashMap<String, RuntimeValue> parameterTypes = visitFormalParameterList(functionCtx.formalParameterList());
                        parameterTypes.putFirst("this", new RuntimeValue.ClassInstance(null, null, null, null));
                        RuntimeValue returnType = visitResultType(functionCtx.resultType());

                        visibilityScope.define(
                            functionName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                functionName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    new ArrayList<RuntimeValue>(parameterTypes.values()),
                                    returnType
                                ), 
                                null
                            )
                        );
                    }
                    // TODO: should we store constructor and destructor separately...
                    case delphi.ConstructorDeclarationContext constructorCtx -> {
                        String constructorName = constructorCtx.identifier().getText();
                        LinkedHashMap<String, RuntimeValue> parameterTypes = new LinkedHashMap<>();
                        parameterTypes.putFirst("this", new RuntimeValue.ClassInstance(null, null, null, null));

                        if (constructorCtx.formalParameterList() != null) {
                            parameterTypes = visitFormalParameterList(constructorCtx.formalParameterList());
                        }
                        
                        visibilityScope.define(
                            constructorName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                constructorName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    new ArrayList<RuntimeValue>(parameterTypes.values()),
                                    new RuntimeValue.Primitive(null)
                                ), 
                                null
                            )
                        );
                    }
                    case delphi.DestructorDeclarationContext destructorCtx -> {
                        LinkedHashMap<String, RuntimeValue> parameterTypes = new LinkedHashMap<>();
                        parameterTypes.putFirst("this", new RuntimeValue.ClassInstance(null, null, null, null));

                        visibilityScope.define(
                            destructorCtx.identifier().getText() + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                destructorCtx.identifier().getText() + "/" + parameterTypes.size(),
                                new RuntimeValue.Method.MethodSignature(
                                    new ArrayList<RuntimeValue>(parameterTypes.values()),
                                    new RuntimeValue.Primitive(null)
                                ),
                                null
                            )
                        );
                    }
                    default -> throw new RuntimeException("Unexpected item '" + memberCtx.getChild(0).getText() + "' when attempting to evaluate member within 'class type'.");
                }
            }
        }
        
        return new RuntimeValue.ClassDefinition(
            null,
            privateScope,
            protectedScope,
            publicScope
        );
    }
    */

    /**
     * Creates a RuntimeValue.Method with a null function definition and a null name...
     */
    @Override
    public RuntimeValue visitFunctionType(delphi.FunctionTypeContext ctx) {
        throw new UnsupportedOperationException("Operation not implemented");

        // TODO: Get the parameter types...
        // return new RuntimeValue.Method(
        //     null,
        //     new RuntimeValue.Method.MethodSignature(
        //         List.of(),
        //         visitResultType(ctx.resultType())
        //     ),
        //     null
        // );
    }

    /**
     * Creates a RuntimeValue.Method with a null function definition and a null name...
     */
    @Override
    public RuntimeValue visitProcedureType(delphi.ProcedureTypeContext ctx) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Returns the type of each argument in the order it appears as a list of RuntimeValue
     */
    @Override
    public List<RuntimeValue.Method.MethodParameter> visitFormalParameterList(delphi.FormalParameterListContext ctx) {
        List<RuntimeValue.Method.MethodParameter> parameters = new ArrayList<RuntimeValue.Method.MethodParameter>();

        // Handle each of the sections...
        for (delphi.FormalParameterSectionContext section: ctx.formalParameterSection()) {
            if (section.getChild(0) == null) {
                continue;
            }

            switch (section.getChild(0)) {
                case delphi.ParameterGroupContext parameterGroupCtx -> {
                    RuntimeValue type = visitTypeIdentifier(parameterGroupCtx.typeIdentifier());

                    // For every identifier push the type
                    for (delphi.IdentifierContext identifier : parameterGroupCtx.identifierList().identifier()) {
                        parameters.add(new RuntimeValue.Method.MethodParameter(
                            identifier.getText(),
                            type,
                            false
                        ));
                    }
                }
                case TerminalNode t when (t.getSymbol().getType() == delphi.VAR) -> {
                    RuntimeValue type = visitTypeIdentifier(section.parameterGroup().typeIdentifier());

                    // For every identifier push the type
                    for (delphi.IdentifierContext identifier : section.parameterGroup().identifierList().identifier()) {
                        parameters.add(new RuntimeValue.Method.MethodParameter(
                            identifier.getText(),
                            type,
                            true
                        ));
                    }
                }
                case TerminalNode t when (t.getSymbol().getType() == delphi.FUNCTION) -> {
                    // TODO: These are weird...
                }
                case TerminalNode t when (t.getSymbol().getType() == delphi.PROCEDURE) -> {
                    // TODO: These are weird...
                }
                default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'formal parameter list'.");
            }
        }

        return parameters;
    }

    /**
     * Returns the result of visiting the result type's type identifier.
     */
    @Override
    public RuntimeValue visitResultType(delphi.ResultTypeContext ctx) {
        return visitTypeIdentifier(ctx.typeIdentifier());
    }

    /**
     * Returns the 'default value' for primitive types, otherwise it returns the type definition/blueprint...
     */
    @Override
    public RuntimeValue visitType_(delphi.Type_Context ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.SimpleTypeContext simpleTypeContext -> visitSimpleType(simpleTypeContext);
            // case delphi.StructuredTypeContext structuredTypeContext -> visitStructuredType(simpleTypeContext);
            // case delphi.PointerTypeContext pointerTypeContext -> visitPointerType(simpleTypeContext);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'type_'.");
        };
    }

    @Override
    public RuntimeValue visitSimpleType(delphi.SimpleTypeContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.ScalarTypeContext scalarTypeContext -> visitScalarType(scalarTypeContext);
            // TODO: subrange type
            case delphi.TypeIdentifierContext typeIdentifierContext -> visitTypeIdentifier(typeIdentifierContext);
            // TODO: string type
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'simple type'.");
        };
    }

    /* Scalar Type AKA Enumeration */
    @Override
    public RuntimeValue visitScalarType(delphi.ScalarTypeContext ctx) {
        // Looks weird, but looping over all identifiers in the identifier list..
        Map<String, Integer> optionsMap = new HashMap<String, Integer>();
        for (int i = 0; i < ctx.identifierList().identifier().size(); i++) {
            optionsMap.put(ctx.identifierList().identifier().get(i).getText(), i);
        }

        // Create a new Enumeration RuntimeValue and return it.
        return new RuntimeValue.Enumeration(null, optionsMap);
    }

    /**
     * Visits the type identifier. Looks it up in the scope if it is an identifier.
     * If it is one of the built in types it returns default value for that type.
     */
    @Override
    public RuntimeValue visitTypeIdentifier(delphi.TypeIdentifierContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.IdentifierContext identifier -> scope.lookup(identifier.getText()).orElseThrow(() -> new NoSuchElementException("Type Identifier '" + ctx.identifier().IDENT().getText() + "' is not present in scope when attempting to evaluate 'type identifier'."));
            // NOTE: arbitrary choices for the primitive default values...
            case TerminalNode t when(t.getSymbol().getType() == delphi.CHAR) -> new RuntimeValue.Primitive((char)'a');
            case TerminalNode t when(t.getSymbol().getType() == delphi.STRING) -> new RuntimeValue.Primitive("");
            case TerminalNode t when(t.getSymbol().getType() == delphi.BOOLEAN) -> new RuntimeValue.Primitive(true);
            case TerminalNode t when(t.getSymbol().getType() == delphi.INTEGER) -> new RuntimeValue.Primitive(new BigInteger("0"));
            case TerminalNode t when(t.getSymbol().getType() == delphi.REAL) -> new RuntimeValue.Primitive(new BigDecimal("0.0"));
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'type identifier'.");
        };
    }

    //#endregion Types

    //#region Variable Declaration

    @Override
    public RuntimeValue visitVariableDeclaration(delphi.VariableDeclarationContext ctx) {
        RuntimeValue typeDefault = visitType_(ctx.type_());

        for (var identifier : ctx.identifierList().identifier()) {
            scope.define(identifier.getText(), new RuntimeValue.Variable(identifier.getText(), typeDefault));
        }

        return new RuntimeValue.Primitive(null);
    }

    //#endregion Variable Declaration

    //#region Implementations

    /**
     * Visits the implementation (definition) of a procedure and adds it to the scope.
     * 
     * Returns a RuntimeValue.Primitive(null)
     */
    @Override
    public RuntimeValue visitProcedureImplementation(delphi.ProcedureImplementationContext ctx) {
        String procedureName = ctx.identifier().getText();
        List<RuntimeValue.Method.MethodParameter> parameters = (ctx.formalParameterList() == null) ? new ArrayList<>() : visitFormalParameterList(ctx.formalParameterList());

        scope.define(
            procedureName + "/" + parameters.size(),
            new RuntimeValue.Method(
                procedureName + "/" + parameters.size(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    new RuntimeValue.Primitive(null)
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    //#endregion Implementations

    //#region Statements

    // TODO: if statement
    // TODO: procedure and function calls
    // TODO: goto statement (very difficult leave til last)

    @Override
    public RuntimeValue visitAssignmentStatement(delphi.AssignmentStatementContext ctx) {
        // Get the variable from the scope...
        RuntimeValue.Variable variable = RuntimeValue.requireType(visitVariable(ctx.variable()), RuntimeValue.Variable.class);
        RuntimeValue newValue = visitExpression(ctx.expression());

        // Require the type of the new value to be same as variable's type
        RuntimeValue.requireType(newValue, variable.value().getClass());
        // TODO: might need more checks than above to handle special cases...

        scope.assign(variable.name(), new RuntimeValue.Variable(variable.name(), newValue));
        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitProcedureStatement(delphi.ProcedureStatementContext ctx) {
        List<RuntimeValue> parameterValues = visitParameterList(ctx.parameterList());

        String procedureName = ctx.identifier().IDENT().getText() + "/" + parameterValues.size();
        RuntimeValue.Method procedure = RuntimeValue.requireType(
            scope.lookup(procedureName).orElseThrow(() -> new NoSuchElementException("Method '" + procedureName + "' is not present in scope when attempting to evaluate 'procedure statement'.")), 
            RuntimeValue.Method.class
        );

        // Turn parameters to be variables
        List<RuntimeValue.Variable> parameters = new ArrayList<>();
        for (int i = 0; i < parameterValues.size(); i++) {
            parameters.add(new RuntimeValue.Variable(
                procedure.signature().parameters().get(i).name(), 
                parameterValues.get(i)
            ));
        }

        procedure.invoke(scope, parameters);
        return new RuntimeValue.Primitive(null);
    }

    @Override
    public Object visitWhileStatement(delphi.WhileStatementContext ctx) {
        Scope oldScope = scope;

        while (RuntimeValue.requireType((RuntimeValue) visit(ctx.expression()), Boolean.class)) {
            // Create new scope for each loop iteration (static scoping inside loop body)
            scope = new Scope(Optional.of(oldScope));
            
            try {
                visit(ctx.statement());
            } catch (ContinueException e) {
                // skip to next iteration
            } catch (BreakException e) {
                break;
            } finally {
                scope = oldScope;
            }
        }
        return new RuntimeValue.Primitive(null);
    }

    @Override
    public Object visitForStatement(delphi.ForStatementContext ctx) {
        String varName = ctx.identifier().getText();
        delphi.ForListContext list = ctx.forList();
        
        // TODO: update for working with more things, characters, enums, etc.
        BigInteger start = RuntimeValue.requireType((RuntimeValue) visit(list.initialValue()), BigInteger.class);
        BigInteger end = RuntimeValue.requireType((RuntimeValue) visit(list.finalValue()), BigInteger.class);
        
        boolean isTo = list.getChild(1).getText().equalsIgnoreCase("to");
        
        Scope oldScope = scope;
        scope = new Scope(Optional.of(oldScope));
        
        try {
            scope.define(varName, new RuntimeValue.Variable(varName, new RuntimeValue.Primitive(start)));
            
            for (BigInteger i = start; isTo ? i.compareTo(end) <= 0 : i.compareTo(end) >= 0; i = i.add(isTo ? BigInteger.ONE : BigInteger.ONE.negate())) {
                scope.assign(varName, new RuntimeValue.Variable(varName, new RuntimeValue.Primitive(i)));    
    
                try {
                    visit(ctx.statement());
                } catch (ContinueException e) {
                    continue;
                } catch (BreakException e) {
                    break;
                }
            }
        } finally {
            scope = oldScope;
        }
        return new RuntimeValue.Primitive(null);
    }

    //#endregion Statements

    //#region Expressions

    @Override
    public RuntimeValue visitExpression(delphi.ExpressionContext ctx) {
        RuntimeValue lhs = visitSimpleExpression(ctx.simpleExpression());

        if (ctx.relationaloperator() == null) {
            return lhs;
        }
        
        return switch (ctx.relationaloperator().getChild(0)) {
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.EQUAL ||
                t.getSymbol().getType() == delphi.NOT_EQUAL ||
                t.getSymbol().getType() == delphi.LT ||
                t.getSymbol().getType() == delphi.LE ||
                t.getSymbol().getType() == delphi.GT ||
                t.getSymbol().getType() == delphi.GE
            ) -> {
                RuntimeValue rhs = visitExpression(ctx.expression());

                // If either one is not a comparable there is an issue
                var lhsValue = RuntimeValue.requireType(lhs, Comparable.class);
                var rhsValue = RuntimeValue.requireType(rhs, lhsValue.getClass());

                @SuppressWarnings("unchecked")
                var result = lhsValue.compareTo(rhsValue);

                yield switch (t.getSymbol().getType()) {
                    case delphi.EQUAL -> new RuntimeValue.Primitive(result == 0);
                    case delphi.NOT_EQUAL -> new RuntimeValue.Primitive(result != 0);
                    case delphi.LT -> new RuntimeValue.Primitive(result < 0);
                    case delphi.LE -> new RuntimeValue.Primitive(result <= 0);
                    case delphi.GT -> new RuntimeValue.Primitive(result > 0);
                    case delphi.GE -> new RuntimeValue.Primitive(result >= 0);
                    default -> throw new RuntimeException("Unexpected item '" + ctx.relationaloperator().getText() + "' when attempting to evaluate 'expression'.");
                };
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.relationaloperator().getText() + "' when attempting to evaluate 'expression'.");
        };
    }

    @Override
    public RuntimeValue visitSimpleExpression(delphi.SimpleExpressionContext ctx) {
        RuntimeValue lhs = visitTerm(ctx.term());

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        return switch (ctx.additiveoperator().getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.OR -> {
                Boolean result = RuntimeValue.requireType(lhs, Boolean.class) || RuntimeValue.requireType(visitSimpleExpression(ctx.simpleExpression()), Boolean.class);
                yield new RuntimeValue.Primitive(result);
            }
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.PLUS  
            ) -> {
                RuntimeValue rhs = visitSimpleExpression(ctx.simpleExpression());
                RuntimeValue.Primitive lhsPrimitive = RuntimeValue.requireType(lhs, RuntimeValue.Primitive.class);
                RuntimeValue.Primitive rhsPrimitive = RuntimeValue.requireType(rhs, RuntimeValue.Primitive.class);

                // The result of the operation will depend on the types passed in...
                Object result = switch (lhsPrimitive.value()) {
                    // Concatenation...
                    case String left when rhsPrimitive.value() instanceof String right -> left + right;
                    case String left when rhsPrimitive.value() instanceof Character right -> left + right.toString();
                    case Character left when rhsPrimitive.value() instanceof String right -> left.toString() + right;
                    case Character left when rhsPrimitive.value() instanceof Character right -> left.toString() + right.toString();
                    // Numerical Addition...
                    case BigInteger left when rhsPrimitive.value() instanceof BigInteger right -> left.add(right);
                    case BigInteger left when rhsPrimitive.value() instanceof BigDecimal right -> new BigDecimal(left.toString()).add(right);
                    case BigDecimal left when rhsPrimitive.value() instanceof BigInteger right -> left.add(new BigDecimal(right.toString()));
                    case BigDecimal left when rhsPrimitive.value() instanceof BigDecimal right -> left.add(right);
                    // Bad types passed in...
                    default -> throw new RuntimeException("Unexpected types received for '" + ctx.additiveoperator().getText() + "' when attempting to evaluate 'simple expression'.");
                };

                yield new RuntimeValue.Primitive(result);
            }
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.MINUS
            ) -> {
                RuntimeValue rhs = visitSimpleExpression(ctx.simpleExpression());
                RuntimeValue.Primitive lhsPrimitive = RuntimeValue.requireType(lhs, RuntimeValue.Primitive.class);
                RuntimeValue.Primitive rhsPrimitive = RuntimeValue.requireType(rhs, RuntimeValue.Primitive.class);

                Object result = switch (lhsPrimitive.value()) {
                    // Numerical Subtraction...
                    case BigInteger left when rhsPrimitive.value() instanceof BigInteger right -> left.subtract(right);
                    case BigInteger left when rhsPrimitive.value() instanceof BigDecimal right -> new BigDecimal(left.toString()).subtract(right);
                    case BigDecimal left when rhsPrimitive.value() instanceof BigInteger right -> left.subtract(new BigDecimal(right.toString()));
                    case BigDecimal left when rhsPrimitive.value() instanceof BigDecimal right -> left.subtract(right);
                    // Bad types passed in...
                    default -> throw new RuntimeException("Unexpected types received for '" + ctx.additiveoperator().getText() + "' when attempting to evaluate 'simple expression'.");
                };

                yield new RuntimeValue.Primitive(result);
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.additiveoperator().getText() + "' when attempting to evaluate 'simple expression'.");
        };
    }

    @Override
    public RuntimeValue visitTerm(delphi.TermContext ctx) {
        RuntimeValue lhs = visitSignedFactor(ctx.signedFactor());

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        return switch (ctx.multiplicativeoperator().getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.AND -> {
                Boolean result = RuntimeValue.requireType(lhs, Boolean.class) && RuntimeValue.requireType(visitTerm(ctx.term()), Boolean.class);
                yield new RuntimeValue.Primitive(result);
            }
            case TerminalNode t when (
                t.getSymbol().getType() == delphi.STAR ||
                t.getSymbol().getType() == delphi.SLASH ||
                t.getSymbol().getType() == delphi.DIV ||
                t.getSymbol().getType() == delphi.MOD
            ) -> {
                RuntimeValue rhs = visitTerm(ctx.term());

                // If either one is not a number there is an issue
                var lhsValue = RuntimeValue.requireType(lhs, Number.class);
                var rhsValue = RuntimeValue.requireType(rhs, Number.class);

                if (lhsValue instanceof BigDecimal || rhsValue instanceof BigDecimal) {
                    // We know the result type is BigDecimal
                    yield switch (t.getSymbol().getType()) {
                        case delphi.STAR -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).multiply(new BigDecimal(rhsValue.toString())));
                        case delphi.DIV -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).divide(new BigDecimal(rhsValue.toString())));
                        case delphi.SLASH -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).divide(new BigDecimal(rhsValue.toString())));
                        case delphi.MOD -> new RuntimeValue.Primitive(new BigDecimal(lhsValue.toString()).remainder(new BigDecimal(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'term'."); // Should never happen
                    };
                } else {
                    // We know the result type is BigInteger
                    yield switch (t.getSymbol().getType()) {
                        case delphi.STAR -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).multiply(new BigInteger(rhsValue.toString())));
                        case delphi.DIV -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).divide(new BigInteger(rhsValue.toString())));
                        case delphi.SLASH -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).divide(new BigInteger(rhsValue.toString())));
                        case delphi.MOD -> new RuntimeValue.Primitive(new BigInteger(lhsValue.toString()).remainder(new BigInteger(rhsValue.toString())));
                        default -> throw new RuntimeException("Unexpected symbol '" + t.getText() + "' when attempting to evaluate 'term'."); // Should never happen
                    };    
                }
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.multiplicativeoperator().getText() + "' when attempting to evaluate 'term'.");
        };
    }

    @Override
    public RuntimeValue visitSignedFactor(delphi.SignedFactorContext ctx) {
        return switch (ctx.getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.PLUS -> visitFactor(ctx.factor());
            case TerminalNode t when t.getSymbol().getType() == delphi.MINUS -> {
                Number n = RuntimeValue.requireType(visitFactor(ctx.factor()), Number.class);
                yield switch (n) {
                    case BigInteger bigInteger -> new RuntimeValue.Primitive(bigInteger.negate());
                    case BigDecimal bigDecimal -> new RuntimeValue.Primitive(bigDecimal.negate());
                    default -> throw new RuntimeException("Expected BigInteger or BigDecimal when attempting to negate value when attempting to evaluate 'signed factor'.");
                };
            }
            case delphi.FactorContext factorCtx -> visitFactor(factorCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'signed factor'.");
        };
    }

    @Override
    public RuntimeValue visitFactor(delphi.FactorContext ctx) {
        // We can tell what we are parsing based on the first child, so use a switch expression to return the proper thing.
        return switch (ctx.getChild(0)) {
            // TODO: variable parsing might require a bit more nuance to handle complex variable types, but is working for primitive values...
            case delphi.VariableContext variableCtx -> RuntimeValue.requireType(visitVariable(variableCtx), RuntimeValue.Variable.class).value();
            case TerminalNode t when t.getSymbol().getType() == delphi.LPAREN -> visitExpression(ctx.expression());
            case delphi.FunctionDesignatorContext functionDesignatorCtx -> visitFunctionDesignator(functionDesignatorCtx);
            case delphi.UnsignedConstantContext unsignedConstantCtx -> visitUnsignedConstant(unsignedConstantCtx);
            // case delphi.Set_Context setContext -> null; TODO: add set representation
            case TerminalNode t when t.getSymbol().getType() == delphi.NOT -> new RuntimeValue.Primitive(!RuntimeValue.requireType(visitFactor(ctx.factor()), Boolean.class));
            case delphi.Bool_Context boolCtx -> visitBool_(boolCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'factor'.");
        };
    }

    @Override
    public RuntimeValue visitFunctionDesignator(delphi.FunctionDesignatorContext ctx) {
        List<RuntimeValue> parameterValues = visitParameterList(ctx.parameterList());

        RuntimeValue scopeValue = scope.lookup(ctx.identifier().getText() + "/" + parameterValues.size()).orElseThrow(() -> new NoSuchElementException("Method '" + ctx.identifier().IDENT().getText() + "' is not present in scope when attempting to evaluate 'function designator'."));
        RuntimeValue.Method function = RuntimeValue.requireType(scopeValue, RuntimeValue.Method.class);

        // Turn parameters to be variables
        List<RuntimeValue.Variable> parameters = new ArrayList<>();
        for (int i = 0; i < parameterValues.size(); i++) {
            parameters.add(new RuntimeValue.Variable(
                function.signature().parameters().get(i).name(), 
                parameterValues.get(i)
            ));
        }

        return function.invoke(scope, parameters);
    }

    @Override
    public List<RuntimeValue> visitParameterList(delphi.ParameterListContext ctx) {
        List<RuntimeValue> parameters = new ArrayList<>();
        for (delphi.ActualParameterContext parameter: ctx.actualParameter()) {
            parameters.add(visitExpression(parameter.expression()));
            // TODO: figure out how to deal with parameter width... What even is it??
            // parameter.parameterwidth();
        }
        return parameters;
    }

    /**
     * TODO: THIS IS NEEDS TO BE FINISHED EVENTUALLY!
     */
    @Override
    public RuntimeValue visitVariable(delphi.VariableContext ctx) {
        String primaryVarName = ctx.identifier(0).IDENT().getText();
        // var primary = ctx.primary().identifier().IDENT().getText();
        // TODO: HANDLE MEMBER ACCESS HERE!!!
        return scope.lookup(primaryVarName).orElseThrow(() -> new NoSuchElementException(primaryVarName + " is not defined in scope!"));
    }


    //#endregion Expressions

    //#region Constants

    @Override
    public RuntimeValue visitConstant(delphi.ConstantContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.IdentifierContext identifierCtx -> {
                Optional<RuntimeValue> result = scope.lookup(identifierCtx.IDENT().getText());
                yield result.orElseThrow(() -> new NoSuchElementException("Variable '" + identifierCtx.IDENT().getText() + "' is not present in scope when attempting to evaluate 'constant'."));
            }
            case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
            case delphi.SignContext signCtx -> {
                RuntimeValue value = switch (ctx.getChild(1)) {
                    case delphi.IdentifierContext identifierCtx -> {
                        Optional<RuntimeValue> result = scope.lookup(identifierCtx.IDENT().getText());
                        yield result.orElseThrow(() -> new NoSuchElementException("Variable '" + identifierCtx.IDENT().getText() + "' is not present in scope when attempting to evaluate 'constant'."));
                    }
                    case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
                    default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(1).getText() + "' when attempting to evaluate 'constant'.");
                };

                if (signCtx.MINUS() != null) {
                    Number n = RuntimeValue.requireType(value, Number.class);
                    value = switch (n) {
                        case BigInteger bigInteger -> new RuntimeValue.Primitive(bigInteger.negate());
                        case BigDecimal bigDecimal -> new RuntimeValue.Primitive(bigDecimal.negate());
                        default -> throw new RuntimeException("Expected BigInteger or BigDecimal when attempting to negate value when attempting to evaluate 'constant'.");
                    };
                }

                yield value;
            }
            case delphi.StringContext stringCtx -> visitString(stringCtx);
            case delphi.ConstantChrContext constantChrCtx -> visitConstantChr(constantChrCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'constant'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedConstant(delphi.UnsignedConstantContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.UnsignedNumberContext unsignedNumberCtx -> visitUnsignedNumber(unsignedNumberCtx);
            case delphi.ConstantChrContext constantChrCtx -> visitConstantChr(constantChrCtx);
            case delphi.StringContext stringContext -> visitString(stringContext);
            case TerminalNode t when t.getSymbol().getType() == delphi.NIL -> new RuntimeValue.Primitive(null);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'UnsignedConstant'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedNumber(delphi.UnsignedNumberContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.UnsignedIntegerContext unsignedIntegerCtx -> visitUnsignedInteger(unsignedIntegerCtx);
            case delphi.UnsignedRealContext unsignedRealCtx -> visitUnsignedReal(unsignedRealCtx);
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(0).getText() + "' when attempting to evaluate 'UnsignedNumber'.");
        };
    }

    @Override
    public RuntimeValue visitUnsignedInteger(delphi.UnsignedIntegerContext ctx) {
        return new RuntimeValue.Primitive(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public RuntimeValue visitUnsignedReal(delphi.UnsignedRealContext ctx) {
        return new RuntimeValue.Primitive(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public RuntimeValue visitBool_(delphi.Bool_Context ctx) {
        return new RuntimeValue.Primitive(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    @Override
    public RuntimeValue visitString(delphi.StringContext ctx) {
        String literal = ctx.STRING_LITERAL().getText();
        return new RuntimeValue.Primitive(literal.substring(1, literal.length() - 1));
    }

    @Override
    public RuntimeValue visitConstantChr(delphi.ConstantChrContext ctx) {
        int charCode = Integer.parseInt(ctx.unsignedInteger().NUM_INT().getText());
        return new RuntimeValue.Primitive((char) charCode);
    }

    //#endregion Constants

}
