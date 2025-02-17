package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import plp.group.Interpreter.Types.GeneralType;
import plp.group.Interpreter.Types.GeneralTypeFactory;
import plp.group.Interpreter.Types.Procedural.FunctionImplementation;
import plp.group.Interpreter.Types.Procedural.ProcedureImplementation;
import plp.group.Interpreter.Types.Simple.BooleanType;
import plp.group.Interpreter.Types.Simple.CharType;
import plp.group.Interpreter.Types.Simple.EnumType;
import plp.group.Interpreter.Types.Simple.StringType;
import plp.group.Interpreter.Types.Simple.Integers.GeneralInteger;
import plp.group.Interpreter.Types.Simple.Reals.GeneralReal;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;
import plp.group.project.delphiParser.AdditiveoperatorContext;
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
    private Map<String, ObjectInstance> objectInstances = new HashMap<>();

    public Interpreter() {
        super();
        scope.enterScope();

        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html

        // #region Built-In IO Functions

        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);

        scope.insert("readln", new SymbolInfo(
                "readln",
                GeneralTypeFactory.createProcedure(arguments -> {
                    scope.enterScope();
                    var user_inputs = scanner.nextLine().split(" ");

                    for (var i = 0; i < user_inputs.length; i++) {
                        var variableName = ((ParseTree) arguments[i]).getText();
                        scope.update(variableName, new SymbolInfo(variableName,
                                GeneralTypeFactory.createInteger(new BigInteger(user_inputs[i]))));
                    }

                    scope.exitScope();
                })));

        scope.insert("write", new SymbolInfo(
                "write",
                GeneralTypeFactory.createProcedure((arguments -> {
                    for (var arg : arguments) {
                        var value = (GeneralType) visit((ParseTree) arg);
                        System.out.print(value.toString());
                    }
                }))));

        scope.insert("writeln", new SymbolInfo(
                "writeln",
                GeneralTypeFactory.createProcedure((arguments -> {
                    for (var arg : arguments) {
                        var value = (GeneralType) visit((ParseTree) arg);
                        System.out.print(value.toString());
                    }
                    System.out.println();
                }))));

        // #endregion Built-In IO Functions
    }

    // object storage and class method execution starts here
    
    @Override
    public Void visitClassDeclaration(delphiParser.ClassDeclarationContext ctx) {
        String className = ctx.identifier().getText();
        ClassDefinition classDef = new ClassDefinition(className, ctx);
        scope.insert(className, new SymbolInfo(className, classDef, "public"));
        return null;
    }

    @Override
    public Void visitObjectInstantiation(delphiParser.ObjectInstantiationContext ctx) {
        String variableName = ctx.identifier(0).getText();
        String className = ctx.identifier(1).getText();

        // Lookup class definition
        SymbolInfo classInfo = scope.lookup(className);
        if (classInfo == null || !(classInfo.value instanceof ClassDefinition)) {
            throw new RuntimeException("Class not found: " + className);
        }

        ObjectInstance instance = new ObjectInstance((ClassDefinition) classInfo.value);
        // Store the instance in the interpreter's memory
        objectInstances.put(variableName, instance);
        scope.insert(variableName, new SymbolInfo(variableName, instance));
        // Check if constructor exists
        if (((ClassDefinition) classInfo.value).constructor != null) {
            instance.invokeConstructor(ctx.argumentList());
        }
        return null;
    }

    @Override
    public Void visitMethodCall(delphiParser.MethodCallContext ctx) {
        String objectName = ctx.identifier(0).getText();
        String methodName = ctx.identifier(1).getText();

        ObjectInstance instance = objectInstances.get(objectName);
        if (instance == null) {
            throw new RuntimeException("Object not found: " + objectName);
        }
        if (methodName.equals("Free")) {
            deleteObject(objectName);
        } else {
            instance.invokeMethod(methodName, ctx.argumentList());
        }
        return null;
    }

    class ObjectInstance {
        private ClassDefinition classDefinition;
        private Map<String, Object> fields = new HashMap<>();

        ObjectInstance(ClassDefinition classDefinition) {
            this.classDefinition = classDefinition;
            
            // Initialize fields from class definition
            for (String fieldName : classDefinition.fields.keySet()) {
                fields.put(fieldName, null); 
            }
        }
        
        void setField(String fieldName, Object value) {
            if (!fields.containsKey(fieldName)) {
                throw new RuntimeException("Field not found: " + fieldName);
            }
            fields.put(fieldName, value);
        }
        
        Object getField(String fieldName) {
            if (!fields.containsKey(fieldName)) {
                throw new RuntimeException("Field not found: " + fieldName);
            }
            return fields.get(fieldName);
        }

        void invokeConstructor(delphiParser.ArgumentListContext args) {
            if (classDefinition.constructor != null) {
                scope.enterScope();
                
                // Bind 'Self' to the instance
                scope.insert("Self", new SymbolInfo("Self", this));
                // Pass arguments to constructor
                if (args != null && classDefinition.constructor.parameterList() != null) {
                    List<ParseTree> passedArgs = args.expression();
                    List<delphiParser.ParameterContext> expectedParams = classDefinition.constructor.parameterList().parameter();
                    
                    if (passedArgs.size() != expectedParams.size()) {
                        throw new RuntimeException("Argument count mismatch in constructor");
                    }
                    
                    for (int i = 0; i < passedArgs.size(); i++) {
                        String paramName = expectedParams.get(i).identifier().getText();
                        Object paramValue = visit(passedArgs.get(i)); // Evaluate argument
                        scope.insert(paramName, new SymbolInfo(paramName, paramValue));
                    }
                }
                
                visit(classDefinition.constructor.block());
                scope.exitScope();
            }
        }
        
        void destroy() {
            if (classDefinition.destructor != null) {
                scope.enterScope();
                scope.insert("Self", new SymbolInfo("Self", this));
                visit(classDefinition.destructor.block());
                scope.exitScope(); 
            } 
        }

        Object invokeMethod(String methodName, delphiParser.ArgumentListContext args) {
            // Search for the method in the class definition
            delphiParser.MethodDeclarationContext method = null;
            for (var section : classDefinition.body.classSection()) {
                for (var m : section.methodDeclaration()) {
                    if (m.identifier().getText().equals(methodName)) {
                        method = m;
                        break;
                    }
                }
            }

            if (method == null) {
                throw new RuntimeException("Method not found: " + methodName + " in class " + classDefinition.name);
            }

            scope.enterScope();
            // Bind 'Self' to reference the current instance
            scope.insert("Self", new SymbolInfo("Self", this));
            if (args != null && method.parameterList() != null) {
                List<ParseTree> passedArgs = args.expression();
                List<delphiParser.ParameterContext> expectedParams = method.parameterList().parameter();
                
                if (passedArgs.size() != expectedParams.size()) {
                    throw new RuntimeException("Argument count mismatch for method " + methodName);
                }
                
                for (int i = 0; i < passedArgs.size(); i++) {
                    String paramName = expectedParams.get(i).identifier().getText();
                    Object paramValue = visit(passedArgs.get(i)); // This evaluates argument
                    scope.insert(paramName, new SymbolInfo(paramName, paramValue));
                }
            }
            visit(method.block());
            Object result = scope.lookup("Result") != null ? scope.lookup("Result").value : null;
            scope.exitScope();
            return result;
        }
    }

    class ClassDefinition {
        String name;
        delphiParser.ClassBodyContext body;
        delphiParser.MethodDeclarationContext constructor;
        delphiParser.MethodDeclarationContext destructor;
        Map<String, Object> fields = new HashMap<>();

        ClassDefinition(String name, delphiParser.ClassDeclarationContext ctx) {
            this.name = name;
            this.body = ctx.classBody();
            // Iterate over methods to find the constructor
            for (var section : ctx.classBody().classSection()) {
                for (var method : section.methodDeclaration()) {
                    if (method.CONSTRUCTOR() != null) {
                        this.constructor = method;
                    }
                    if (method.DESTRUCTOR() != null) {
                        this.destructor = method;
                }
            }

                // Extract fields from 'fieldDeclaration'
                for (var field : section.fieldDeclaration()) {
                    String fieldName = field.identifier().getText();
                    fields.put(fieldName, null); 
            }
        }
    }
} 

// object storage and class method execution ends here

public void deleteObject(String objectName) {
    if (!objectInstances.containsKey(objectName)) {
        return;  // exit instead of throwing error
    }
    
    ObjectInstance instance = objectInstances.get(objectName);
    if (instance != null) {
        instance.destroy(); // Call destructor before deletion
        objectInstances.remove(objectName);
        scope.remove(objectName);
    } 
}
    
    @Override
    public Void visitBlock(delphiParser.BlockContext ctx) {
        scope.enterScope();
        // Add all the labels into the new scope for the block.
        if (ctx != null) {
            var labels = (new LabelWalker(ctx)).getLabels();
            for (var entry : labels.entrySet()) {
                scope.insert(entry.getKey(), new SymbolInfo(entry.getKey(), entry.getValue()));
            }

            // This "infinite loop" runs until execution completes without a goto.
            RuleNode node = ctx;
            while (true) {
                try {
                    visitChildren(node); // Execute block normally
                    break; // Exit loop when execution completes without `goto`
                } catch (GoToException e) {
                    RuleNode targetNode = (RuleNode) scope.lookup(e.label).value;
                    if (targetNode == null) {
                        throw new RuntimeException("Undefined label: " + e.label);
                    }

                    // Grab the part of the 'ctx' that is after the 'goto'
                    node = getRemainingRules(targetNode);
                }
            }
        }
        scope.exitScope();
        return null;
    }

    private RuleNode getRemainingRules(RuleNode targetNode) {
        RuleNode parent = (RuleNode) targetNode.getParent();
        if (parent == null) {
            return targetNode; // If there's no parent, just return the target node
        }

        // Get the start index (where targetNode is)
        int startIndex = -1;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i) == targetNode) {
                startIndex = i;
                break;
            }
        }
        if (startIndex == -1) {
            return targetNode; // Should never happen
        }

        // Creating context like this is really bad...
        // BUT since we control where it goes 100% this is okay-ish...
        var newContext = new delphiParser.BlockContext((ParserRuleContext) parent, startIndex);
        for (int i = startIndex; i < parent.getChildCount(); i++) {
            newContext.addAnyChild(parent.getChild(i));
        }
        return newContext;
    }

    // #region Declarations

    @Override
    public Void visitTypeDefinition(delphiParser.TypeDefinitionContext ctx) {
        var typeIdentifier = (String) visit(ctx.getChild(0));
        var typeDefinition = (GeneralType) visit(ctx.getChild(2));
        GeneralTypeFactory.registerType(typeIdentifier.toLowerCase(), typeDefinition);
        return null;
    }

    @Override
    public Void visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        GeneralType instance;
        try {
            instance = GeneralTypeFactory.constructType(ctx.getChild(ctx.getChildCount() - 1).getText().toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        @SuppressWarnings("unchecked")
        var identifiers = (ArrayList<String>) visit(ctx.getChild(0));
        for (String identifier : identifiers) {
            scope.insert(identifier, new SymbolInfo(identifier, instance));
        }
        return null;
    }

    @Override
    public Void visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
        var identifier = (String) visit(ctx.getChild(1));

        @SuppressWarnings("unchecked")
        var parameters = (List<List<SymbolInfo>>) visit(ctx.getChild(2));

        var returnType = (String) visit(ctx.getChild(ctx.getChildCount() - 3));

        var body = GeneralTypeFactory.createFunction(arguments -> {
            scope.enterScope();
            // If we have expected parameters then visit them here and process
            // appropriately...
            if (parameters != null) {
                var total = 0;
                for (var i = 0; i < parameters.size(); i++) {
                    for (var j = 0; j < parameters.get(i).size(); j++) {
                        var currParameter = parameters.get(i).get(j);

                        var actualParameter = ((ParseTree) arguments[total]);
                        var actualParameterVisited = (GeneralType) visit(actualParameter);

                        if (i == 1) {
                            scope.insert(currParameter.name,
                                    new SymbolInfo(currParameter.name, new Reference(actualParameter.getText())));
                        } else {
                            scope.insert(currParameter.name,
                                    new SymbolInfo(currParameter.name, actualParameterVisited));
                        }
                        total++;
                    }
                }
            }

            // Define a result variable to hold the result
            try {
                scope.insert("Result", new SymbolInfo("Result", GeneralTypeFactory.constructType(returnType)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Execute the code and get the result after
            visit(ctx.getChild(ctx.getChildCount() - 1));
            var result = scope.lookup("Result");

            scope.exitScope();
            return (GeneralType) result.value;
        });

        scope.insert(identifier, new SymbolInfo(identifier, body));
        return null;
    }

    @Override
    public String visitResultType(delphiParser.ResultTypeContext ctx) {
        return (String) visit(ctx.getChild(0));
    }

    @Override
    public Void visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {
        var identifier = (String) visit(ctx.getChild(1));

        @SuppressWarnings("unchecked")
        var parameters = (List<List<SymbolInfo>>) visit(ctx.getChild(2));

        var body = GeneralTypeFactory.createProcedure(arguments -> {
            scope.enterScope();
            // If we have expected parameters then visit them here and process
            // appropriately...
            if (parameters != null) {
                var total = 0;
                for (var i = 0; i < parameters.size(); i++) {
                    for (var j = 0; j < parameters.get(i).size(); j++) {
                        var currParameter = parameters.get(i).get(j);

                        var actualParameter = ((ParseTree) arguments[total]);
                        var actualParameterVisited = (GeneralType) visit(actualParameter);

                        if (i == 1) {
                            scope.insert(currParameter.name,
                                    new SymbolInfo(currParameter.name, new Reference(actualParameter.getText())));
                        } else {
                            scope.insert(currParameter.name,
                                    new SymbolInfo(currParameter.name, actualParameterVisited));
                        }
                        total++;
                    }
                }
            }
            visit(ctx.getChild(ctx.getChildCount() - 1));
            scope.exitScope();
        });

        scope.insert(identifier, new SymbolInfo(identifier, body));
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<List<SymbolInfo>> visitFormalParameterList(delphiParser.FormalParameterListContext ctx) {
        var parameters = new ArrayList<List<SymbolInfo>>();
        for (var i = 1; i < ctx.getChildCount(); i += 2) {
            var list = (List<SymbolInfo>) visit(ctx.getChild(i));
            if (list != null) {
                parameters.add(list);
            }
        }
        return parameters;
    }

    // How to handle formal parameter section??
    @Override
    public List<SymbolInfo> visitFormalParameterSection(delphiParser.FormalParameterSectionContext ctx) {
        if (ctx.getChildCount() < 1) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        var parameters = (List<SymbolInfo>) visit(ctx.getChild(ctx.getChildCount() - 1));

        // Create reference types...
        if (ctx.VAR() != null) {
            var parameterGroup = ctx.getChild(ctx.getChildCount() - 1);
            @SuppressWarnings("unchecked")
            var identifiers = (List<String>) visit(parameterGroup.getChild(0));
            for (var i = 0; i < parameters.size(); i++) {
                parameters.set(i, new SymbolInfo(parameters.get(i).name, new Reference(identifiers.get(i))));
            }
        }

        return parameters;
    }

    @Override
    public List<SymbolInfo> visitParameterGroup(delphiParser.ParameterGroupContext ctx) {
        @SuppressWarnings("unchecked")
        var identifiers = (List<String>) visit(ctx.getChild(0));
        var typeIdentifier = (String) visit(ctx.getChild(ctx.getChildCount() - 1));

        var parameters = new ArrayList<SymbolInfo>();
        for (var identifier : identifiers) {
            try {
                parameters.add(
                        new SymbolInfo(identifier, GeneralTypeFactory.constructType(typeIdentifier.toLowerCase())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parameters;
    }

    // #endregion Declarations

    // #region Identifiers

    /**
     * Returns the SymbolInfo for the variable.
     */
    @Override
    public SymbolInfo visitVariable(delphiParser.VariableContext ctx) {
        var name = (String) visit(ctx.getChild(0));
        return scope.lookup(name);
    }

    /**
     * Returns the result of visiting all the identifiers in a list.
     */
    @Override
    public List<String> visitIdentifierList(delphiParser.IdentifierListContext ctx) {
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
    public String visitIdentifier(delphiParser.IdentifierContext ctx) {
        return ctx.getText();
    }

    // #endregion Identifiers

    // #region Types

    @Override
    public GeneralType visitScalarType(delphiParser.ScalarTypeContext ctx) {
        @SuppressWarnings("unchecked")
        var identifiers = (List<String>) visit(ctx.getChild(1));

        // No overriding of the underlying values...
        var temp = new HashMap<String, Integer>();
        for (var i = 0; i < identifiers.size(); i++) {
            temp.put(identifiers.get(i), i);
        }

        // We can now add all the enum names into scope...
        for (var i = 0; i < identifiers.size(); i++) {
            scope.insert(identifiers.get(i),
                    new SymbolInfo(identifiers.get(i), GeneralTypeFactory.createEnum(temp, identifiers.get(i))));
        }

        return GeneralTypeFactory.createEnum(temp);
    }

    @Override
    public GeneralType visitSubrangeType(delphiParser.SubrangeTypeContext ctx) {
        var lhs = visit(ctx.getChild(0));
        var rhs = visit(ctx.getChild(2));

        if (lhs instanceof String) {
            lhs = scope.lookup((String) lhs).value;
            rhs = scope.lookup((String) rhs).value;
        }

        if (lhs instanceof GeneralInteger) {
            return GeneralTypeFactory.createSubrange((GeneralInteger) lhs, (GeneralInteger) rhs);
        }

        if (lhs instanceof GeneralReal) {
            return GeneralTypeFactory.createSubrange((GeneralInteger) lhs, (GeneralInteger) rhs);
        }

        if (lhs instanceof CharType) {
            return GeneralTypeFactory.createSubrange((CharType) lhs, (CharType) rhs);
        }

        if (lhs instanceof StringType) {
            return GeneralTypeFactory.createSubrange((StringType) lhs, (StringType) rhs);
        }

        if (lhs instanceof BooleanType) {
            return GeneralTypeFactory.createSubrange((StringType) lhs, (StringType) rhs);
        }

        if (lhs instanceof EnumType) {
            return GeneralTypeFactory.createSubrange((EnumType) lhs, (EnumType) rhs);
        }

        return null;
    }

    @Override
    public String visitTypeIdentifier(delphiParser.TypeIdentifierContext ctx) {
        return (String) ctx.getText();
    }

    // @Override
    // public Void visitStringtype(delphiParser.StringtypeContext ctx) {
    // return null;
    // }

    // #endregion Types

    // #region Statements

    @Override
    public Void visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        // Check if we're assigning to an object field
        if (ctx.variable().DOT() != null) {
            String objectName = ctx.variable().identifier(0).getText();
            String fieldName = ctx.variable().identifier(1).getText();
            ObjectInstance instance = objectInstances.get(objectName);
            if (instance == null) {
                throw new RuntimeException("Object not found: " + objectName);
            }
            
            Object value = visit(ctx.expression());  // for expressions
            instance.setField(fieldName, value);
            return null;
        }

        // Normal variable assignment
        var identifier = (SymbolInfo) visit(ctx.getChild(0));
        var value = (GeneralType) visit(ctx.getChild(2));

        scope.update(identifier.name, new SymbolInfo(identifier.name, value));
        return null;
    }

    @Override
    public Void visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        var procedureDetails = (SymbolInfo) scope.lookup(((String) visit(ctx.getChild(0))));

        @SuppressWarnings("unchecked")
        var parameters = (List<GeneralType>) visit(ctx.getChild(2));

        // Below craziness passes the parameters one at a time...
        var implementation = ((GeneralType) procedureDetails.value).getValue();
        if (implementation instanceof ProcedureImplementation) {
            ((ProcedureImplementation) ((GeneralType) procedureDetails.value).getValue())
                    .execute(parameters.toArray(new Object[0]));
        } else {
            ((FunctionImplementation) ((GeneralType) procedureDetails.value).getValue())
                    .execute(parameters.toArray(new Object[0]));
        }

        return null;
    }

    /**
     * Returns the ParseTree of each parameter.
     */
    @Override
    public List<ParseTree> visitParameterList(delphiParser.ParameterListContext ctx) {
        var parameters = new ArrayList<ParseTree>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) {
            parameters.add(ctx.getChild(i));
        }
        return parameters;
    }

    @Override
    public Void visitGotoStatement(delphiParser.GotoStatementContext ctx) {
        throw new GoToException(ctx.getChild(1).getText());
    }

    @Override
    public Void visitIfStatement(delphiParser.IfStatementContext ctx) {
        var expr = (BooleanType) visit(ctx.getChild(1));

        if (((Boolean) expr.getValue()).equals(Boolean.TRUE)) {
            visit(ctx.getChild(3));
        } else {
            visit(ctx.getChild(5));
        }

        return null;
    }

    @Override
    public Void visitCaseStatement(delphiParser.CaseStatementContext ctx) {
        // CASE expression OF caseListElement (SEMI caseListElement)* (SEMI ELSE
        // statements)? END
        var condition = (GeneralType) visit(ctx.getChild(1));

        var i = 3;
        Object[] caseListElement;
        do {
            caseListElement = (Object[]) visit(ctx.getChild(i));
            // If the condition is one of the possibilities, then visit body and exit
            @SuppressWarnings("unchecked")
            var possibilities = (List<GeneralType>) caseListElement[0];
            if (possibilities.contains(condition)) {
                visit(ctx.getChild(i).getChild(2)); // This is the body of the caselist
                return null;
            }
            // Otherwise keep going
            i += 2;
        } while (caseListElement != null);

        // If an else block exists, we visit that body now.
        if (ctx.ELSE() != null) {
            visit(ctx.getChild(ctx.getChildCount() - 2));
        }
        return null;
    }

    @Override
    public Object[] visitCaseListElement(delphiParser.CaseListElementContext ctx) {
        // constList COLON statement
        var constList = (List<GeneralType>) visit(ctx.getChild(0));
        var body = ctx.getChild(2); // Don't visit the body now, we visit only if the case condition is true
        // TODO: find better return type because this is UGLY!!!
        return new Object[] { constList, body };
    }

    @Override
    public Void visitWhileStatement(delphiParser.WhileStatementContext ctx) {
        // While visiting the condition results in a boolean true, repetitively visit
        // the statement.
        while (((Boolean) (((BooleanType) visit(ctx.getChild(1))).getValue())).booleanValue()) {
            visit(ctx.getChild(ctx.getChildCount() - 1));
        }
        return null;
    }

    @Override
    public Void visitRepeatStatement(delphiParser.RepeatStatementContext ctx) {
        // Guranteed to visit the statements once, then repeat as long as the condition
        // is true.
        do {
            visit(ctx.getChild(1));
        } while (!(((Boolean) (((BooleanType) visit(ctx.getChild(ctx.getChildCount() - 1))).getValue()))
                .booleanValue()));
        return null;
    }

    @Override
    public Void visitForStatement(delphiParser.ForStatementContext ctx) {
        var identifier = scope.lookup((String) visit(ctx.getChild(1)));

        @SuppressWarnings("unchecked")
        var list = (List<GeneralType>) visit(ctx.getChild(3));

        for (var value : list) {
            scope.update(identifier.name, new SymbolInfo(identifier.name, value));
            visit(ctx.getChild(ctx.getChildCount() - 1));
        }
        return null;
    }

    @Override
    public List<GeneralType> visitForList(delphiParser.ForListContext ctx) {
        var initialValue = (GeneralType) visit(ctx.getChild(0));
        var finalValue = (GeneralType) visit(ctx.getChild(2));

        var result = new ArrayList<GeneralType>();

        // Integer
        if (initialValue instanceof GeneralInteger && finalValue instanceof GeneralInteger) {
            // TODO: this might break for large numbers...
            if (ctx.TO() != null) {
                for (var i = ((BigInteger) initialValue.getValue())
                        .intValue(); i <= ((BigInteger) finalValue.getValue())
                                .intValue(); i++) {
                    result.add(GeneralTypeFactory.createInteger(BigInteger.valueOf(i)));
                }
            } else {
                for (var i = ((BigInteger) initialValue.getValue())
                        .intValue(); i >= ((BigInteger) finalValue.getValue())
                                .intValue(); i--) {
                    result.add(GeneralTypeFactory.createInteger(BigInteger.valueOf(i)));
                }
            }
            return result;
        }
        // Character
        if (initialValue instanceof CharType && finalValue instanceof CharType) {
            if (ctx.TO() != null) {
                for (var i = ((Character) initialValue.getValue()).charValue(); i <= ((Character) finalValue.getValue())
                        .charValue(); i++) {
                    result.add(GeneralTypeFactory.createChar(Character.valueOf(i)));
                }
            } else {
                for (var i = ((Character) initialValue.getValue()).charValue(); i >= ((Character) finalValue.getValue())
                        .charValue(); i--) {
                    result.add(GeneralTypeFactory.createChar(Character.valueOf(i)));
                }
            }
            return result;
        }
        // Enum
        if (initialValue instanceof EnumType && finalValue instanceof EnumType) {
            var options = ((EnumType) initialValue).getOptions();
            var keys = options.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .filter(e -> {
                        if (ctx.DOWNTO() != null) {
                            return (e.getValue() <= (Integer) ((EnumType) initialValue).getValue())
                                    && (e.getValue() >= (Integer) ((EnumType) finalValue).getValue());
                        }
                        return (e.getValue() >= (Integer) ((EnumType) initialValue).getValue())
                                && (e.getValue() <= (Integer) ((EnumType) finalValue).getValue());
                    }).map(Map.Entry::getKey).collect(Collectors.toList());

            if (ctx.DOWNTO() != null) {
                Collections.reverse(keys);
            }
            for (var key : keys) {
                result.add(GeneralTypeFactory.createEnum(options, key));
            }

            return result;
        }

        throw new RuntimeException("Invalid ForList: " + ctx.getText());
    }

    // #endregion Statements

    // #region Expressions

    /**
     * Visits the expression, returns whatever the value of the expression is.
     * 
     * The value is returned within a GeneralType.
     */
    @Override
    public GeneralType visitExpression(delphiParser.ExpressionContext ctx) {
        // Check if the expression is 'Self'
        if (ctx.identifier() != null && ctx.identifier().getText().equals("Self")) {
            SymbolInfo selfSymbol = scope.lookup("Self");
            return (selfSymbol != null) ? selfSymbol.value : null;
        }
        // Otherwise its normal expression evaluation
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.relationaloperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.relationaloperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator, rhs);
    }

    /**
     * Get the string representation (text) of the relational operator.
     */
    @Override
    public String visitRelationaloperator(RelationaloperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.additiveoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.additiveoperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator, rhs);
    }

    /**
     * Get the string representation (text) of the additive operator.
     */
    @Override
    public String visitAdditiveoperator(AdditiveoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitTerm(delphiParser.TermContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.multiplicativeoperator() == null) {
            return lhs;
        }

        var operator = (String) visit(ctx.multiplicativeoperator());
        var rhs = (GeneralType) visit(ctx.getChild(2));
        return lhs.applyOperation(operator.toUpperCase(), rhs);
    }

    /**
     * Get the string representation (text) of the multiplicative operator.
     */
    @Override
    public String visitMultiplicativeoperator(MultiplicativeoperatorContext ctx) {
        return ctx.getText();
    }

    @Override
    public GeneralType visitSignedFactor(delphiParser.SignedFactorContext ctx) {
        var factor = (GeneralType) visit(ctx.factor());
        // NOTE: the ctx.PLUS() does nothing...
        if (ctx.MINUS() != null) {
            factor = factor.applyOperation("NEGATE", null);
        }
        return factor;
    }

    /**
     * Returns a GeneralType with the information regarding this thing.
     */
    @Override
    public GeneralType visitFactor(delphiParser.FactorContext ctx) {
        int childIndex = (ctx.LPAREN() != null || ctx.NOT() != null) ? 1 : 0;
        var result = visit(ctx.getChild(childIndex));

        // If its a symbol info, grab the GeneralType from it.
        if (result instanceof SymbolInfo) {
            result = ((SymbolInfo) result).value;
        }

        // If its a reference, repeatedly go up til it isn't anymore ()
        while (result instanceof Reference) {
            result = ((SymbolInfo) scope.lookup(((Reference) result).refereeName)).value;
        }

        // If a NOT is present, perform that operation.
        if (ctx.NOT() != null) {
            result = ((GeneralType) result).applyOperation("NOT", null);
        }

        return (GeneralType) result;

        // TODO: actually do all the below in their own visit functions (_set)...
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
    public List<GeneralType> visitConstList(delphiParser.ConstListContext ctx) {
        var result = new ArrayList<GeneralType>();
        for (var i = 0; i < ctx.getChildCount(); i += 2) {
            result.add((GeneralType) visit(ctx.getChild(i)));
        }
        return result;
    }

    @Override
    public GeneralType visitConstant(delphiParser.ConstantContext ctx) {
        var value = visit(ctx.getChild((ctx.sign() != null ? 1 : 0)));
        // value might be an identifier, so handle that case
        if (value instanceof String) {
            value = scope.lookup((String) value).value;
        }

        // Handle the minus sign
        if (ctx.sign() != null && ctx.sign().MINUS() != null) {
            value = ((GeneralType) value).applyOperation("NEGATE", null);
        }
        return (GeneralType) value;
    }

    @Override
    public GeneralType visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return GeneralTypeFactory.createString(literal.substring(1, literal.length() - 1));
    }

    @Override
    public GeneralType visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
        return GeneralTypeFactory.createInteger(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public GeneralType visitUnsignedReal(delphiParser.UnsignedRealContext ctx) {
        return GeneralTypeFactory.createReal(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public GeneralType visitConstantChr(delphiParser.ConstantChrContext ctx) {
        var charCode = (BigInteger) ((GeneralType) visit(ctx.getChild(2))).getValue();
        return GeneralTypeFactory.createChar(Character.valueOf((char) charCode.intValue()));
    }

    @Override
    public GeneralType visitBool_(delphiParser.Bool_Context ctx) {
        return GeneralTypeFactory.createBoolean(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    @Override
    public GeneralType visitFunctionDesignator(delphiParser.FunctionDesignatorContext ctx) {
        var function = (FunctionImplementation) ((GeneralType) (scope.lookup((String) visit(ctx.getChild(0))).value))
                .getValue();

        @SuppressWarnings("unchecked")
        var parameters = (List<GeneralType>) visit(ctx.getChild(2));

        return function.execute(parameters.toArray(new Object[0]));
    }

    @Override
    public GeneralType visitSet_(delphiParser.Set_Context ctx) {
        @SuppressWarnings("unchecked")
        var elements = (List<GeneralType>) visit(ctx.getChild(1));
        return GeneralTypeFactory.createSet(new HashSet<GeneralType>(elements));
    }

    @Override
    public List<GeneralType> visitElementList(delphiParser.ElementListContext ctx) {
        var result = new ArrayList<GeneralType>();
        for (var i = 0; i < ctx.getChildCount() - 1; i++) {
            result.add(((GeneralType) visit(ctx.getChild(i))));
        }
        return result;
    }

    @Override
    public GeneralType visitElement(delphiParser.ElementContext ctx) {
        var lhs = (GeneralType) visit(ctx.getChild(0));

        if (ctx.DOTDOT() == null) {
            return lhs;
        }

        return lhs.applyOperation("..", (GeneralType) visit(ctx.getChild(2)));
    }

    // #endregion Literals

}
