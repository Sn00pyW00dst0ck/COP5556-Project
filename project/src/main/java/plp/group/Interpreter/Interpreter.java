package plp.group.Interpreter;

import java.util.ArrayList;
import java.util.List;
import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;

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

        // IO Functions (TODO: Read and ReadLn)

        scope.insert("write", new SymbolInfo(
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("writeln", new SymbolInfo(
                (List<Object> arguments) -> {
                    for (Object arg : arguments) {
                        System.out.print(arg);
                    }
                    System.out.println("");
                    return null;
                },
                List.of(Object[].class),
                Void.class));
    }

    @Override
    public Object visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
        // Get the procedure name/details
        SymbolInfo procedureDetails = (SymbolInfo) visit(ctx.getChild(0));

        // Visit parameters to get values to add to the parameters list
        var parameters = new ArrayList<Object>();
        for (var i = 2; i < ctx.getChildCount() - 1; i++) {
            parameters.add(visit(ctx.getChild(i)));
        }

        // Call the function and return the result.
        return procedureDetails.function.apply(parameters);
    }

    @Override
    public Object visitClassDeclaration(delphiParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        SymbolTable classScope = new SymbolTable(); // Store attributes and methods inside class scope
        
        for (var member : ctx.classBody().children) {
            if (member instanceof delphiParser.VariableDeclarationContext) {
                visitVariableDeclaration((delphiParser.VariableDeclarationContext) member);
            } else if (member instanceof delphiParser.MethodDeclarationContext) {
                visitMethodDeclaration((delphiParser.MethodDeclarationContext) member);
            } else if (member instanceof delphiParser.ConstructorDeclarationContext) {
                visitConstructorDeclaration((delphiParser.ConstructorDeclarationContext) member);
            }
        }
        
        scope.insert(className, new SymbolInfo(className, classScope, null)); // Store class info
        return null;
    }

    @Override
    public Object visitMethodDeclaration(delphiParser.MethodDeclarationContext ctx) {
        String methodName = ctx.IDENTIFIER().getText();
        SymbolTable classScope = scope; // Class scope should already be active
        
        classScope.insert(methodName, new SymbolInfo(methodName, (Function<List<Object>>) args -> {
            scope.enterScope(); // Enter method scope
            visit(ctx.block()); // Execute method body
            scope.exitScope();
            return null;
        }, List.of(), Void.class));
        
        return null;
    }

    @Override
    public Object visitObjectInstantiation(delphiParser.ObjectInstantiationContext ctx) {
        String objectName = ctx.IDENTIFIER(0).getText();
        String className = ctx.IDENTIFIER(1).getText();
        
        SymbolInfo classInfo = scope.lookup(className);
        if (classInfo == null || !(classInfo.value instanceof SymbolTable)) {
            throw new RuntimeException("Class " + className + " is not defined.");
        }
        
        SymbolTable objectScope = new SymbolTable();
        objectScope.copyFrom((SymbolTable) classInfo.value); // Copy class members
        
        // If constructor exists, call it
        SymbolInfo constructor = objectScope.lookup(className);
        if (constructor != null && constructor.value instanceof Function) {
            ((Function) constructor.value).apply(new ArrayList<>()); // Pass arguments if any
        }
        
        scope.insert(objectName, new SymbolInfo(objectName, objectScope, null)); // Store object
        return null;
    }

    @Override
    public Object visitConstructorDeclaration(delphiParser.ConstructorDeclarationContext ctx) {
        String className = ctx.getParent().getChild(0).getText(); // Parent is class
        SymbolTable classScope = (SymbolTable) scope.lookup(className).value;
        
        classScope.insert(className, new SymbolInfo(className, (Function<List<Object>>) args -> {
            scope.enterScope(); // Enter object scope
            visit(ctx.block()); // Run constructor code
            scope.exitScope();
            return null;
        }, List.of(), Void.class));
        
        return null;
    }

    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        return scope.lookup(ctx.getText());
    }

    @Override
    public Object visitString(delphiParser.StringContext ctx) {
        var literal = ctx.STRING_LITERAL().getText();
        return literal.substring(1, literal.length() - 1);
    }
}
