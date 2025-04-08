package plp.group.Interpreter;

import static plp.group.Interpreter.RuntimeValue.requireType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jline.reader.LineReader;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphi.CaseListElementContext;
import plp.group.project.delphi.ClassMemberDeclarationContext;
import plp.group.Interpreter.ControlFlowExceptions.BreakException;
import plp.group.Interpreter.ControlFlowExceptions.ContinueException;
import plp.group.Interpreter.ControlFlowExceptions.GotoException;
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
    private Scope scope;

    public Interpreter(LineReader input) {
        Environment.setReader(input);
        scope = new Scope(Optional.of(Environment.scope()));
    }

    @Override
    public RuntimeValue visitProgram(delphi.ProgramContext ctx) {
        // Visit the block to initialize variables and run the program
        visit(ctx.block());
        return new RuntimeValue.Primitive(null);
    }


    //#region Types

    @Override
    public RuntimeValue visitTypeDefinition(delphi.TypeDefinitionContext ctx) {
        String typeName = ctx.identifier().getText();

        RuntimeValue typeDefinition = switch (ctx.getChild(ctx.getChildCount() - 1)) {
            case delphi.Type_Context typeContext -> visitType_(typeContext);
            case delphi.ClassTypeContext classTypeContext -> {
                RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(visitClassType(classTypeContext), RuntimeValue.ClassDefinition.class);
                // Copy over the scopes we found into the ClassDefinition with the proper name.
                yield new RuntimeValue.ClassDefinition(
                    typeName, 
                    classDefinition.privateScope(), 
                    classDefinition.protectedScope(), 
                    classDefinition.publicScope()
                );
            }
            default -> throw new RuntimeException("Unexpected item '" + ctx.getChild(ctx.getChildCount() - 1).getText() + "' when attempting to evaluate 'type definition'.");
        };

        // Add the new typeName to the scope with correct typeDefinition (enumerations have special behavior here).
        scope.define(typeName, typeDefinition);
        if (typeDefinition instanceof RuntimeValue.Enumeration enumeration) {
            for (Map.Entry<String, RuntimeValue.Primitive> entry : enumeration.options().entrySet()) {
                scope.define(entry.getKey(), new RuntimeValue.Variable(entry.getKey(), new RuntimeValue.Enumeration(entry.getKey(), enumeration.options())));
            }
        }

        return new RuntimeValue.Primitive(null);
    }
    
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
                    case delphi.ClassFieldDeclarationContext fieldCtx -> visibilityScope.define(fieldCtx.identifier().getText(), new RuntimeValue.Variable(fieldCtx.identifier().getText(), visitType_(fieldCtx.type_())));
                    // TODO: the below 4 cases share a lot of code so it should be cleaned up at some point
                    case delphi.ClassProcedureDeclarationContext procedureCtx -> {
                        String procedureName = procedureCtx.identifier().getText();
                        ArrayList<RuntimeValue.Method.MethodParameter> parameterTypes = procedureCtx.formalParameterList() == null ? new ArrayList<>() : visitFormalParameterList(procedureCtx.formalParameterList());
                        parameterTypes.addFirst(
                            new RuntimeValue.Method.MethodParameter(
                                "Self",
                                new RuntimeValue.ClassInstance(null, null, null, null),
                                false,
                                false
                            )
                        );

                        visibilityScope.define(
                            procedureName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                procedureName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    parameterTypes,
                                    null
                                ), 
                                null
                            )
                        );
                    }
                    case delphi.ClassFunctionDeclarationContext functionCtx -> {
                        String functionName = functionCtx.identifier().getText();
                        ArrayList<RuntimeValue.Method.MethodParameter> parameterTypes = functionCtx.formalParameterList() == null ? new ArrayList<>() : visitFormalParameterList(functionCtx.formalParameterList());
                        RuntimeValue returnType = visitResultType(functionCtx.resultType());
                        parameterTypes.addFirst(
                            new RuntimeValue.Method.MethodParameter(
                                "Self",
                                new RuntimeValue.ClassInstance(null, null, null, null),
                                false,
                                false
                            )
                        );

                        visibilityScope.define(
                            functionName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                functionName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    parameterTypes,
                                    returnType
                                ),
                                null
                            )
                        );
                    }
                    // TODO: should we store constructor and destructor separately within class definition?...
                    case delphi.ConstructorDeclarationContext constructorCtx -> {
                        String constructorName = constructorCtx.identifier().getText();
                        ArrayList<RuntimeValue.Method.MethodParameter> parameterTypes = constructorCtx.formalParameterList() == null ? new ArrayList<>() : visitFormalParameterList(constructorCtx.formalParameterList());
                        parameterTypes.addFirst(
                            new RuntimeValue.Method.MethodParameter(
                                constructorName,
                                new RuntimeValue.ClassInstance(null, null, null, null),
                                false,
                                false
                            )
                        );

                        visibilityScope.define(
                            constructorName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                constructorName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    parameterTypes,
                                    new RuntimeValue.ClassInstance(null, null, null, null)
                                ),
                                null
                            )
                        );
                    }
                    case delphi.DestructorDeclarationContext destructorCtx -> {
                        String destructorName = destructorCtx.identifier().getText();
                        ArrayList<RuntimeValue.Method.MethodParameter> parameterTypes = new ArrayList<>();
                        parameterTypes.addFirst(
                            new RuntimeValue.Method.MethodParameter(
                                "Self",
                                new RuntimeValue.ClassInstance(null, null, null, null),
                                false,
                                false
                            )
                        );

                        visibilityScope.define(
                            destructorName + "/" + parameterTypes.size(), 
                            new RuntimeValue.Method(
                                destructorName + "/" + parameterTypes.size(), 
                                new RuntimeValue.Method.MethodSignature(
                                    parameterTypes,
                                    null
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
    
    /**
     * Returns the type of each argument in the order it appears as a list of RuntimeValue
     */
    @Override
    public ArrayList<RuntimeValue.Method.MethodParameter> visitFormalParameterList(delphi.FormalParameterListContext ctx) {
        ArrayList<RuntimeValue.Method.MethodParameter> parameters = new ArrayList<RuntimeValue.Method.MethodParameter>();

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
                            false,
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
                            true,
                            false
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
        Map<String, RuntimeValue.Primitive> optionsMap = new HashMap<String, RuntimeValue.Primitive>();
        for (int i = 0; i < ctx.identifierList().identifier().size(); i++) {
            String identifierName = ctx.identifierList().identifier().get(i).getText();
            optionsMap.put(identifierName, new RuntimeValue.Primitive(new BigInteger("" + i)));
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
            case delphi.IdentifierContext identifier -> {
                RuntimeValue result = scope.lookup(identifier.getText()).orElseThrow(() -> new NoSuchElementException("Type Identifier '" + ctx.identifier().IDENT().getText() + "' is not present in scope when attempting to evaluate 'type identifier'."));
                if (result instanceof RuntimeValue.ClassDefinition definition) {
                    yield new RuntimeValue.ClassInstance(definition, null, null, null);
                }
                yield result;
            }
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
                    null
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                    } catch(ReturnException e) {
                        
                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    /**
     * Visits the implementation (definition) of a procedure and adds it to the scope.
     * 
     * Returns a RuntimeValue.Primitive(null)
     */
    @Override
    public RuntimeValue visitFunctionImplementation(delphi.FunctionImplementationContext ctx) {
        String functionName = ctx.identifier().getText();
        List<RuntimeValue.Method.MethodParameter> parameters = (ctx.formalParameterList() == null) ? new ArrayList<>() : visitFormalParameterList(ctx.formalParameterList());

        scope.define(
            functionName + "/" + parameters.size(),
            new RuntimeValue.Method(
                functionName + "/" + parameters.size(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    visitResultType(ctx.resultType())
                ),
                (functionScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = functionScope;
                        visitBlock(ctx.block());
                    } catch(ReturnException e) {

                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitClassProcedureImplementation(delphi.ClassProcedureImplementationContext ctx) {
        String className = ctx.identifier(0).getText();
        String procedureName = ctx.identifier(1).getText();
        ArrayList<RuntimeValue.Method.MethodParameter> parameters = (ctx.formalParameterList() == null) ? new ArrayList<>() : visitFormalParameterList(ctx.formalParameterList());

        RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(scope.lookup(className).orElseThrow(() -> new NoSuchElementException("There is no class '" + className + "' defined in scope when attempting to define '" + procedureName + "/" + parameters.size() + "'.")), RuntimeValue.ClassDefinition.class);

        parameters.addFirst(
            new RuntimeValue.Method.MethodParameter(
                "Self",
                new RuntimeValue.ClassInstance(classDefinition, null, null, null),
                false,
                false
            )
        );

        // Lets get the old method reference
        RuntimeValue.Method oldMethod = RuntimeValue.requireType(
            classDefinition.publicScope().lookup(procedureName + "/" + (parameters.size())).orElseThrow(() -> new NoSuchElementException("'" + procedureName + "/" + parameters.size() + "' not defined when parsing class procedure implementation.")),
            RuntimeValue.Method.class
        );

        classDefinition.publicScope().assign(
            oldMethod.name(),
            new RuntimeValue.Method(
                oldMethod.name(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    oldMethod.signature().returnType()
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                    } catch(ReturnException e) {

                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitClassFunctionImplementation(delphi.ClassFunctionImplementationContext ctx) {
        String className = ctx.identifier(0).getText();
        String functionName = ctx.identifier(1).getText();
        ArrayList<RuntimeValue.Method.MethodParameter> parameters = (ctx.formalParameterList() == null) ? new ArrayList<>() : visitFormalParameterList(ctx.formalParameterList());

        RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(scope.lookup(className).orElseThrow(() -> new NoSuchElementException("There is no class '" + className + "' defined in scope when attempting to define '" + functionName + "/" + parameters.size() + "'.")), RuntimeValue.ClassDefinition.class);

        parameters.addFirst(
            new RuntimeValue.Method.MethodParameter(
                "Self",
                new RuntimeValue.ClassInstance(classDefinition, null, null, null),
                false,
                false
            )
        );

        // Lets get the old method reference
        RuntimeValue.Method oldMethod = RuntimeValue.requireType(
            classDefinition.publicScope().lookup(functionName + "/" + (parameters.size())).orElseThrow(() -> new NoSuchElementException("'" + functionName + "/" + parameters.size() + "' not defined when parsing class function implementation.")),
            RuntimeValue.Method.class
        );

        classDefinition.publicScope().assign(
            oldMethod.name(),
            new RuntimeValue.Method(
                oldMethod.name(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    oldMethod.signature().returnType()
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                    } catch(ReturnException e) {

                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitConstructorImplementation(delphi.ConstructorImplementationContext ctx) {
        String className = ctx.identifier(0).getText();
        String constructorName = ctx.identifier(1).getText();
        ArrayList<RuntimeValue.Method.MethodParameter> parameters = (ctx.formalParameterList() == null) ? new ArrayList<>() : visitFormalParameterList(ctx.formalParameterList());

        RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(scope.lookup(className).orElseThrow(() -> new NoSuchElementException("There is no class '" + className + "' defined in scope when attempting to define '" + constructorName + "/" + parameters.size() + "'.")), RuntimeValue.ClassDefinition.class);

        parameters.addFirst(
            new RuntimeValue.Method.MethodParameter(
                "Self",
                new RuntimeValue.ClassInstance(classDefinition, null, null, null),
                false,
                false
            )
        );

        // Lets get the old method reference
        RuntimeValue.Method oldMethod = RuntimeValue.requireType(
            classDefinition.publicScope().lookup(constructorName + "/" + (parameters.size())).orElseThrow(() -> new NoSuchElementException("'" + constructorName + "/" + parameters.size() + "' not defined when parsing class constructor implementation.")),
            RuntimeValue.Method.class
        );

        classDefinition.publicScope().assign(
            oldMethod.name(),
            new RuntimeValue.Method(
                oldMethod.name(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    oldMethod.signature().returnType()
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                        scope.assign("result", scope.lookup("Self").get());
                    } catch(ReturnException e) {

                    } finally {
                        scope = originalScope;
                    }
                }
            )
        );

        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitDestructorImplementation(delphi.DestructorImplementationContext ctx) {
        String className = ctx.identifier(0).getText();
        String destructorName = ctx.identifier(1).getText();
        RuntimeValue.ClassDefinition classDefinition = RuntimeValue.requireType(scope.lookup(className).orElseThrow(() -> new NoSuchElementException("There is no class '" + className + "' defined in scope when attempting to define '" + destructorName + "/" + 1 + "'.")), RuntimeValue.ClassDefinition.class);

        ArrayList<RuntimeValue.Method.MethodParameter> parameters =  new ArrayList<>();
        parameters.addFirst(
            new RuntimeValue.Method.MethodParameter(
                "Self",
                new RuntimeValue.ClassInstance(classDefinition, null, null, null),
                false,
                false
            )
        );

        // Lets get the old method reference
        RuntimeValue.Method oldMethod = RuntimeValue.requireType(
            classDefinition.publicScope().lookup(destructorName + "/" + (parameters.size())).orElseThrow(() -> new NoSuchElementException("'" + destructorName + "/" + parameters.size() + "' not defined when parsing class destructor implementation.")),
            RuntimeValue.Method.class
        );

        classDefinition.publicScope().assign(
            oldMethod.name(),
            new RuntimeValue.Method(
                oldMethod.name(),
                new RuntimeValue.Method.MethodSignature(
                    parameters,
                    oldMethod.signature().returnType()
                ),
                (procedureScope) -> {
                    Scope originalScope = scope;
                    try {
                        scope = procedureScope;
                        visitBlock(ctx.block());
                    } catch(ReturnException e) {

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

    // assignment statement
    @Override
    public RuntimeValue visitAssignmentStatement(delphi.AssignmentStatementContext ctx) {
        RuntimeValue receiver = visitVariable(ctx.variable());
        RuntimeValue newValue = visitExpression(ctx.expression());

        switch (receiver) {
            case RuntimeValue.Variable variable ->  {
                if (variable.value() instanceof RuntimeValue.Enumeration enumeration) {
                    RuntimeValue.Enumeration value = RuntimeValue.requireType(newValue, RuntimeValue.Enumeration.class);
                    enumeration.setValue(value.value());
                } else if (variable.value() instanceof RuntimeValue.ClassDefinition _) {
                    RuntimeValue.ClassInstance value = RuntimeValue.requireType(newValue, RuntimeValue.ClassInstance.class);
                    variable.setValue(value);
                } else {
                    RuntimeValue.requireType(newValue, variable.value().getClass());
                    variable.setValue(newValue);
                }
            }
            case RuntimeValue.Reference reference -> {
                RuntimeValue.requireType(newValue, reference.getValue().getClass());
                reference.setValue(newValue);
            }
            default -> throw new RuntimeException("Unexpected type of receiver when evaluating 'assignment statement'.");
        }

        return new RuntimeValue.Primitive(null);
    }

    // case statement
    @Override
    public RuntimeValue visitCaseStatement(delphi.CaseStatementContext ctx) {
        RuntimeValue expression = visitExpression(ctx.expression());

        // For each case, check if the expression is in the constList and if so do the case, otherwise do the else
        for (CaseListElementContext castCtx : ctx.caseListElement()) {
            for (var constantCtx : castCtx.constList().constant()) {
                RuntimeValue value = visitConstant(constantCtx);
                switch (value) {
                    case RuntimeValue.Variable variable -> {
                        if (expression.equals(variable.value())) {
                            visit(castCtx.statement());
                            return new RuntimeValue.Primitive(null);
                        }
                    }
                    default -> {
                        if (expression.equals(value)) {
                            visit(castCtx.statement());
                            return new RuntimeValue.Primitive(null);
                        }        
                    }
                }
            }
        }

        // If none of cases work, visit else part.
        visit(ctx.statements());
        return new RuntimeValue.Primitive(null);
    }

    @Override
    public RuntimeValue visitProcedureStatement(delphi.ProcedureStatementContext ctx) {
        LinkedHashMap<String, RuntimeValue> parameterValues = visitParameterList(ctx.parameterList());

        String procedureName = ctx.identifier().IDENT().getText() + "/" + parameterValues.size();
        RuntimeValue.Method procedure = RuntimeValue.requireType(
            scope.lookup(procedureName)
                .or(() -> scope.lookup(ctx.identifier().IDENT().getText() + "/X"))
                .orElseThrow(() -> new NoSuchElementException("Method '" + procedureName + "' is not present in scope when attempting to evaluate 'procedure statement', and it is not variadic.")
            ), 
            RuntimeValue.Method.class
        );

        // Turn parameters to be variables valid for the procedure call...
        List<RuntimeValue> parameters = new ArrayList<>();
        int i = 0;
        for (var parameter : parameterValues.entrySet()) {
            if (procedure.signature().parameters().get(i).isReference()) {
                parameters.add(new RuntimeValue.Reference(
                    procedure.signature().parameters().get(i).name(), 
                    requireType(scope.lookup(parameter.getKey()).get(), RuntimeValue.Variable.class)
                ));
            } else {
                parameters.add(new RuntimeValue.Variable(
                    procedure.signature().parameters().get(i).name(), 
                    parameter.getValue()
                ));
            }
            i++;
        }

        procedure.invoke(scope, parameters);
        return new RuntimeValue.Primitive(null);
    }

    // while statement
    @Override
    public RuntimeValue visitWhileStatement(delphi.WhileStatementContext ctx) {
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

    // for statement
    @Override
    public RuntimeValue visitForStatement(delphi.ForStatementContext ctx) {
        String varName = ctx.identifier().getText();
        
        List<RuntimeValue> forList = visitForList(ctx.forList());
        
        Scope oldScope = scope;
        scope = new Scope(Optional.of(oldScope));
        try {
            scope.define(varName, new RuntimeValue.Variable(varName, forList.get(0)));
            
            for (RuntimeValue value : forList) {
                scope.assign(varName, new RuntimeValue.Variable(varName, value));
    
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

    /**
     * Constructs the list of values that will be looped over within a for loop...
     */
    @Override
    public List<RuntimeValue> visitForList(delphi.ForListContext ctx) {
        RuntimeValue start = visitExpression(ctx.initialValue().expression());
        RuntimeValue end = visitExpression(ctx.finalValue().expression());

        return switch (start) {
            case RuntimeValue.Enumeration initialValue -> {
                RuntimeValue.Enumeration finalValue = RuntimeValue.requireType(end, RuntimeValue.Enumeration.class);
                
                List<String> sortedKeys = initialValue.options().entrySet()
                    .stream()
                    .sorted((e1, e2) -> {
                        return RuntimeValue.requireType(e1.getValue(), BigInteger.class).compareTo(RuntimeValue.requireType(e2.getValue(), BigInteger.class));
                    })
                    .filter((e) -> {
                        BigInteger value = RuntimeValue.requireType(e.getValue(), BigInteger.class);

                        BigInteger min = RuntimeValue.requireType(initialValue.options().get(initialValue.value()), BigInteger.class);
                        BigInteger max = RuntimeValue.requireType(finalValue.options().get(finalValue.value()), BigInteger.class);

                        if (ctx.DOWNTO() != null) {
                            max = RuntimeValue.requireType(initialValue.options().get(initialValue.value()), BigInteger.class);
                            min = RuntimeValue.requireType(finalValue.options().get(finalValue.value()), BigInteger.class);
                        }

                        return min.compareTo(value) <= 0 && max.compareTo(value) >= 0;
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

                if (ctx.DOWNTO() != null) {
                    Collections.reverse(sortedKeys);
                }

                yield sortedKeys.stream()
                        .map((key) -> new RuntimeValue.Enumeration(key, initialValue.options()))
                        .collect(Collectors.toList());
            }
            case RuntimeValue.Primitive primitive -> {
                ArrayList<RuntimeValue> result = new ArrayList<>();

                yield switch (primitive.value()) {
                    case BigInteger initialValue -> {
                        BigInteger finalValue = RuntimeValue.requireType(end, BigInteger.class);
                        
                        if (ctx.TO() != null) {
                            for (BigInteger i = initialValue; i.compareTo(finalValue) <= 0 ; i = i.add(new BigInteger("1"))) {
                                result.add(new RuntimeValue.Primitive(i));
                            }
                        } else {
                            for (BigInteger i = initialValue; i.compareTo(finalValue) >= 0; i = i.subtract(new BigInteger("1"))) {
                                result.add(new RuntimeValue.Primitive(i));
                            }
                        }
                        
                        yield result;
                    }
                    case Character initialValue -> {
                        Character finalValue = RuntimeValue.requireType(end, Character.class);
                        
                        if (ctx.TO() != null) {
                            for (Character i = initialValue; i.compareTo(finalValue) <= 0 ; i++) {
                                result.add(new RuntimeValue.Primitive(i));
                            }
                        } else {
                            for (Character i = initialValue; i.compareTo(finalValue) >= 0; i--) {
                                result.add(new RuntimeValue.Primitive(i));
                            }
                        }
                        
                        yield result;
                    }
                    default -> throw new RuntimeException("Unexpected type '" + primitive.value().getClass().getSimpleName() + "' when evaluating 'for list'");
                };
            }
            default -> throw new RuntimeException("Unexpected type '" + start.getClass().getSimpleName() + "' when evaluating 'for list'");
        };
    }

    // repeat statement
    @Override
    public RuntimeValue visitRepeatStatement(delphi.RepeatStatementContext ctx) {
        Scope oldScope = scope;

        do {
            scope = new Scope(Optional.of(oldScope));
            try {
                visit(ctx.statements());
            } catch (ContinueException e) {
                // skip to next iteration
            } catch (BreakException e) {
                break;
            } finally {
                scope = oldScope;
            }
        } while (!RuntimeValue.requireType((RuntimeValue) visit(ctx.expression()), Boolean.class));

        return new RuntimeValue.Primitive(null);
    }

    // if statement
    @Override
    public Object visitIfStatement(delphi.IfStatementContext ctx) {
        // Evaluate the condition
        RuntimeValue conditionValue = visitExpression(ctx.expression());
        boolean condition = RuntimeValue.requireType(conditionValue, Boolean.class);

        Scope oldScope = scope;
        try {
            scope = new Scope(Optional.of(oldScope));

            if (condition) {
                return visit(ctx.statement(0)); // THEN block
            } else if (ctx.statement().size() > 1) {
                return visit(ctx.statement(1)); // ELSE block
            }
        } finally {
            scope = oldScope;
        }

        return null;
    }


    // goto statement
    @Override
    public RuntimeValue visitGotoStatement(delphi.GotoStatementContext ctx) {
        throw new GotoException(ctx.label().getText());
    }
    
    @Override
    public RuntimeValue visitBlock(delphi.BlockContext ctx) {
        if (ctx.declarationPart() != null) {
            for (delphi.DeclarationPartContext part : ctx.declarationPart()) {
                visit(part);
            }
        }

        Map<String, List<delphi.StatementContext>> labelMap = new LabelWalker().walk(ctx);
        List<delphi.StatementContext> stmts = ctx.compoundStatement().statements().statement();
    
        int index = 0;
        while (index < stmts.size()) {
            try {
                visit(stmts.get(index));
                index++;
            } catch (GotoException e) {
                List<delphi.StatementContext> jumpStatements = labelMap.get(e.label);
                if (jumpStatements == null) {
                    throw new RuntimeException("Label not found: " + e.label);
                }
    
                // jumpStatements starts after label, so we skip the label stmt itself
                stmts = jumpStatements;
                index = 0;
            }
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
        LinkedHashMap<String, RuntimeValue> parameterValues = visitParameterList(ctx.parameterList());

        RuntimeValue scopeValue = scope.lookup(ctx.identifier().getText() + "/" + parameterValues.size())
            .or(() -> scope.lookup(ctx.identifier().getText() + "/X"))
            .orElseThrow(() -> new NoSuchElementException("Method '" + ctx.identifier().getText() + "' is not present in scope when attempting to evaluate 'function designator', and it is not variadic.")
        );

        RuntimeValue.Method function = RuntimeValue.requireType(scopeValue, RuntimeValue.Method.class);

        // Turn parameters to be variables valid for the function call...
        List<RuntimeValue> parameters = new ArrayList<>();
        int i = 0;
        for (var parameter : parameterValues.entrySet()) {
            if (function.signature().parameters().get(i).isReference()) {
                parameters.add(new RuntimeValue.Reference(
                    function.signature().parameters().get(i).name(), 
                    requireType(scope.lookup(parameter.getKey()).get(), RuntimeValue.Variable.class)
                ));
            } else {
                parameters.add(new RuntimeValue.Variable(
                    function.signature().parameters().get(i).name(), 
                    parameter.getValue()
                ));
            }
            i++;
        }

        return function.invoke(scope, parameters);
    }

    @Override
    public LinkedHashMap<String, RuntimeValue> visitParameterList(delphi.ParameterListContext ctx) {
        LinkedHashMap<String, RuntimeValue> parameters = new LinkedHashMap<>();
    
        if (ctx != null && ctx.actualParameter() != null) {
            for (delphi.ActualParameterContext parameter : ctx.actualParameter()) {
                parameters.put(parameter.getText(), visitExpression(parameter.expression()));
                // TODO: figure out how to deal with parameter width... What even is it??
                // parameter.parameterwidth();
            }
        }
    
        return parameters;
    }    

    /**
     * TODO: THIS IS NEEDS TO BE FINISHED EVENTUALLY! HANDLE THE postFixPart!
     * 
     * If the variable is a normal variable it is returned normally. 
     * If it is a reference, then the variable the reference is referring to is returned.
     */
    @Override
    public RuntimeValue visitVariable(delphi.VariableContext ctx) {
        String primaryVarName = ctx.identifier().IDENT().getText();
        RuntimeValue current = scope.lookup(primaryVarName).orElseThrow(() -> new NoSuchElementException(primaryVarName + " is not defined in scope!"));
        
        // TODO: Loop over the post fix add ons, use a switch to tell which it is. Modify the 'current' appropriately...
        for (delphi.PostFixPartContext postFixPart : ctx.postFixPart()) {

            current = switch (postFixPart.getChild(0)) {
                case TerminalNode t when t.getSymbol().getType() == delphi.DOT -> {
                    switch (current) {
                        // If we perform this on a class definition, we assume it is the constructor call.
                        case RuntimeValue.ClassDefinition definition -> {
                            switch (postFixPart.getChild(1)) {
                                case delphi.FunctionDesignatorContext functionDesignatorCtx -> {
                                    // Can't just visit the designator becuase we lookup in receiver scope and apply a self argument...
                                    LinkedHashMap<String, RuntimeValue> parameterValues = visitParameterList(functionDesignatorCtx.parameterList());

                                    RuntimeValue.Method constructor = RuntimeValue.requireType(definition.publicScope().lookup(functionDesignatorCtx.identifier().getText() + "/" + (parameterValues.size() + 1))
                                        .or(() -> definition.publicScope().lookup(functionDesignatorCtx.identifier().getText() + "/X"))
                                        .orElseThrow(() -> new NoSuchElementException("Method '" + functionDesignatorCtx.identifier().getText() + "' is not present in scope when attempting to evaluate 'function designator postfix part', and it is not variadic.")
                                    ), RuntimeValue.Method.class);
                                    
                                    Scope instancePrivate = definition.privateScope().deepCopy();
                                    Scope instanceProtected = instancePrivate.getParent().get();
                                    Scope instancePublic = instanceProtected.getParent().get();

                                    // Fix the parameters just like with the normal function designator, but add self in front.
                                    List<RuntimeValue> parameters = new ArrayList<>();
                                    parameters.add(new RuntimeValue.Variable(
                                        "Self",
                                        new RuntimeValue.ClassInstance(definition, instancePublic, instancePrivate, instanceProtected)
                                    ));
                                    int i = 1;
                                    for (var parameter : parameterValues.entrySet()) {
                                        if (constructor.signature().parameters().get(i).isReference()) {
                                            parameters.add(new RuntimeValue.Reference(
                                                constructor.signature().parameters().get(i).name(), 
                                                requireType(scope.lookup(parameter.getKey()).get(), RuntimeValue.Variable.class)
                                            ));
                                        } else {
                                            parameters.add(new RuntimeValue.Variable(
                                                constructor.signature().parameters().get(i).name(), 
                                                parameter.getValue()
                                            ));
                                        }
                                        i++;
                                    }
                                    current = new RuntimeValue.Variable("", constructor.invoke(scope, parameters));
                                }
                                case delphi.IdentifierContext identifierCtx -> {
                                    String name = identifierCtx.getText();
                                    RuntimeValue.Method constructor = RuntimeValue.requireType(definition.publicScope().lookup(name + "/1").get(), RuntimeValue.Method.class);
                                    // Call the constructor then set current to be the new object...
                                    Scope instancePrivate = definition.privateScope().deepCopy();
                                    Scope instanceProtected = instancePrivate.getParent().get();
                                    Scope instancePublic = instanceProtected.getParent().get();
                                    RuntimeValue.Variable newObj = new RuntimeValue.Variable("newObj", new RuntimeValue.ClassInstance(definition, instancePublic, instancePrivate, instanceProtected));
                                    current = new RuntimeValue.Variable("", constructor.invoke(scope, List.of(newObj)));        
                                }

                                default -> throw new RuntimeException("Unexpected error evaluating postfix part '" + postFixPart.getText() + "'when evaluating 'variable'.");
                            }
                            
                        }
                        // If it is a ClassInstance, it can be a method call or a field access.
                        case RuntimeValue.Variable variable when (variable.value() instanceof RuntimeValue.ClassInstance instance) -> {
                            yield switch (postFixPart.getChild(1)) {
                                case delphi.FunctionDesignatorContext functionDesignatorCtx -> {
                                    // Can't just visit the designator becuase we lookup in receiver scope and apply a self argument...
                                    LinkedHashMap<String, RuntimeValue> parameterValues = visitParameterList(functionDesignatorCtx.parameterList());

                                    RuntimeValue.Method method = RuntimeValue.requireType(instance.publicScope().lookup(functionDesignatorCtx.identifier().getText() + "/" + (parameterValues.size() + 1))
                                        .or(() -> instance.publicScope().lookup(functionDesignatorCtx.identifier().getText() + "/X"))
                                        .orElseThrow(() -> new NoSuchElementException("Method '" + functionDesignatorCtx.identifier().getText() + "' is not present in scope when attempting to evaluate 'function designator postfix part', and it is not variadic.")
                                    ), RuntimeValue.Method.class);

                                    // Fix the parameters just like with the normal function designator, but add self in front.
                                    List<RuntimeValue> parameters = new ArrayList<>();
                                    parameters.add(new RuntimeValue.Variable(
                                        "Self",
                                        instance
                                    ));
                                    int i = 1;
                                    for (var parameter : parameterValues.entrySet()) {
                                        if (method.signature().parameters().get(i).isReference()) {
                                            parameters.add(new RuntimeValue.Reference(
                                                method.signature().parameters().get(i).name(), 
                                                requireType(scope.lookup(parameter.getKey()).get(), RuntimeValue.Variable.class)
                                            ));
                                        } else {
                                            parameters.add(new RuntimeValue.Variable(
                                                method.signature().parameters().get(i).name(), 
                                                parameter.getValue()
                                            ));
                                        }
                                        i++;
                                    }
                                    yield new RuntimeValue.Variable("", method.invoke(scope, parameters));
                                }
                                case delphi.IdentifierContext identifierCtx -> {
                                    String name = identifierCtx.getText();
                                    RuntimeValue x = instance.publicScope().lookup(name).or(() -> {
                                        RuntimeValue.Method method = RuntimeValue.requireType(instance.publicScope().lookup(name + "/1").get(), RuntimeValue.Method.class);
                                        return Optional.of(RuntimeValue.requireType(method.invoke(scope, List.of(new RuntimeValue.Variable("Self", instance))), RuntimeValue.class));
                                    }).get();
                                    yield x;
                                }
                                default -> throw new RuntimeException("Unexpected error evaluating postfix part '" + postFixPart.getText() + "'when evaluating 'variable'.");
                            };
                        }
                        default -> throw new RuntimeException("Unexpected error evaluating postfix part '" + postFixPart.getText() + "'when evaluating 'variable'.");
                    }
                    yield current;
                }
                default -> throw new RuntimeException("Unexpected postfix part '" + postFixPart.getText() + "'when evaluating 'variable'.");
            };
        }

        // Return either a variable or a reference...
        return switch (current) {
            case RuntimeValue.Variable variable -> { yield variable; }
            case RuntimeValue.Reference reference -> { yield reference.variable(); }
            default -> throw new RuntimeException("Unexpected type of variable '" + primaryVarName + "' when evaluating 'variable'.");
        };
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
