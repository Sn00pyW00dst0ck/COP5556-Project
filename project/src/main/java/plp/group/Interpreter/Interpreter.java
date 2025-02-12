package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

import plp.group.project.delphiBaseVisitor;
import plp.group.project.delphiParser;
import plp.group.project.delphiParser.AdditiveoperatorContext;
import plp.group.project.delphiParser.Bool_Context;
import plp.group.project.delphiParser.MultiplicativeoperatorContext;
import plp.group.project.delphiParser.RelationaloperatorContext;

public class Interpreter extends delphiBaseVisitor<Object> {

    private SymbolTable scope = new SymbolTable();

    public Interpreter() {
        scope.enterScope();

        Scanner scanner = new Scanner(System.in);

        scope.insert("read", new SymbolInfo(
                "read",
                (List<Object> arguments) -> {
                    return null;
                },
                List.of(Object[].class),
                Void.class));

        scope.insert("readln", new SymbolInfo(
                "readln",
                (List<Object> arguments) -> {
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
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        scope.enterScope();
        visitChildren(ctx);
        scope.exitScope();
        return null;
    }

    @Override
    public Object visitClassDeclaration(delphiParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        SymbolTable classScope = new SymbolTable();

        for (var member : ctx.classBody().children) {
            if (member instanceof delphiParser.VariableDeclarationContext) {
                visitVariableDeclaration((delphiParser.VariableDeclarationContext) member);
            } else if (member instanceof delphiParser.MethodDeclarationContext) {
                visitMethodDeclaration((delphiParser.MethodDeclarationContext) member);
            } else if (member instanceof delphiParser.ConstructorDeclarationContext) {
                visitConstructorDeclaration((delphiParser.ConstructorDeclarationContext) member);
            }
        }

        scope.insert(className, new SymbolInfo(className, classScope, null));
        return null;
    }

    @Override
    public Object visitMethodDeclaration(delphiParser.MethodDeclarationContext ctx) {
        String methodName = ctx.IDENTIFIER().getText();
        SymbolTable classScope = scope;

        classScope.insert(methodName, new SymbolInfo(methodName, (Function<List<Object>>) args -> {
            scope.enterScope();
            visit(ctx.block());
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
        objectScope.copyFrom((SymbolTable) classInfo.value);

        SymbolInfo constructor = objectScope.lookup(className);
        if (constructor != null && constructor.value instanceof Function) {
            ((Function) constructor.value).apply(new ArrayList<>());
        }

        scope.insert(objectName, new SymbolInfo(objectName, objectScope, null));
        return null;
    }

    @Override
    public Object visitConstructorDeclaration(delphiParser.ConstructorDeclarationContext ctx) {
        String className = ctx.getParent().getChild(0).getText();
        SymbolTable classScope = (SymbolTable) scope.lookup(className).value;

        classScope.insert(className, new SymbolInfo(className, (Function<List<Object>>) args -> {
            scope.enterScope();
            visit(ctx.block());
            scope.exitScope();
            return null;
        }, List.of(), Void.class));

        return null;
    }

    @Override
    public Object visitIdentifier(delphiParser.IdentifierContext ctx) {
        return scope.lookup(ctx.getText());
    }
}