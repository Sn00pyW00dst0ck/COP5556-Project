package plp.group.Interpreter;

import java.util.List;
import java.util.Optional;

/**
 * Defines all of the built in functions to the language which the interpreter supports.
 * 
 * Naming conventions are as follows. 
 * 
 * The function is added into the scope with a name as the function name followed by a '/' and then the number of parameters it expects. 
 * The value in the scope is a RuntimeValue.Method, with appropriate method signature and definition (name in signature matches name in scope).
 */
public class Environment {
    
    /**
     * Get a scope with all the built-in functions defined, and ONLY those functions defined. 
     * 
     * @return the Scope instance. 
     */
    public static Scope scope() {
        Scope scope = new Scope(Optional.empty());

        // Write and Writeln can take 0, 1, or 2 arguments, so this is more accurate than what we did for P1 with variable args... 
        // Source: https://www.freepascal.org/docs-html/rtl/system/writeln.html

        scope.define("write/1", new RuntimeValue.Method(
            "write/1", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class), 
                Void.class
            ), 
            Environment::write
        ));
        scope.define("write/2", new RuntimeValue.Method(
            "write/2", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class, Object.class), 
                Void.class
            ), 
            Environment::write
        ));
        scope.define("write/3", new RuntimeValue.Method(
            "write/3", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class, Object.class, Object.class), 
                Void.class
            ), 
            Environment::write
        ));

        scope.define("writeln/1", new RuntimeValue.Method(
            "writeln/1", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class), 
                Void.class
            ), 
            Environment::writeln
        ));
        scope.define("writeln/2", new RuntimeValue.Method(
            "writeln/2", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class, Object.class), 
                Void.class
            ), 
            Environment::writeln
        ));
        scope.define("writeln/3", new RuntimeValue.Method(
            "writeln/3", 
            new RuntimeValue.Method.MethodSignature(
                List.of(Object.class, Object.class, Object.class), 
                Void.class
            ), 
            Environment::writeln
        ));

        /*
         * TODO: read and readln here, they will be tough because they have to be variadic I think, unless we want to force only one variable. 
         * Also, references will be a thing to deal with and it will be tough...
         */

        // https://www.freepascal.org/docs-html/rtl/system/break.html
        scope.define("break/0", new RuntimeValue.Method(
            "break/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                Void.class
            ),
            Environment::Break)
        );

        // https://www.freepascal.org/docs-html/rtl/system/continue.html
        scope.define("continue/0", new RuntimeValue.Method(
            "continue/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                Void.class
            ),
            Environment::Continue)
        );

        return scope;
    }

    /**
     * Prints the provided values to the console, one after another, with no ending newline character.
     * 
     * @return a RuntimeValue.Primitive() with value null. 
     */
    private static RuntimeValue write(List<RuntimeValue> arguments) {
        for (RuntimeValue arg : arguments) {
            System.out.print(arg.toString()); // TODO: This will probably need to be updated to print things in more proper formats (mostly decimals and other special types)
        }
        return new RuntimeValue.Primitive(null);
    }

    /**
     * Same as 'write', but finishes with an ending newline character. 
     * 
     * Internally, this function calls 'write' to provide functionality.
     * 
     * @return a RuntimeValue.Primitive() with value null. 
     */
    private static RuntimeValue writeln(List<RuntimeValue> arguments) {
        var result = write(arguments);
        System.out.println();
        return result;
    }

    // TODO: read and readln here...


    /**
     * Throws break exception to signify ending the loop context.
     */
    private static RuntimeValue Break(List<RuntimeValue> arguments) {
        throw new BreakException();
    }

    /**
     * Throws continue exception to signify jump to next loop iteration.
     */
    private static RuntimeValue Continue(List<RuntimeValue> arguments) {
        throw new ContinueException();
    }
}
