package plp.group.Interpreter;

import java.util.List;
import java.util.Optional;

import plp.group.Interpreter.ControlFlowExceptions.BreakException;
import plp.group.Interpreter.ControlFlowExceptions.ContinueException;
import plp.group.Interpreter.ControlFlowExceptions.ReturnException;

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

        // Write and Writeln can take 1, 2, or 3 arguments, so this is more accurate than what we did for P1 with variable args... 
        // Source: https://www.freepascal.org/docs-html/rtl/system/writeln.html

        scope.define("write/1", new RuntimeValue.Method(
            "write/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> write(methodScope, 1)
        ));
        scope.define("write/2", new RuntimeValue.Method(
            "write/2",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> write(methodScope, 2)
        ));
        scope.define("write/3", new RuntimeValue.Method(
            "write/3",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param3",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> write(methodScope, 3)
        ));

        scope.define("writeln/0", new RuntimeValue.Method(
            "writeln/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(),
                null
            ),
            (methodScope) -> writeln(methodScope, 0)
        ));
        scope.define("writeln/1", new RuntimeValue.Method(
            "writeln/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> writeln(methodScope, 1)
        ));
        scope.define("writeln/2", new RuntimeValue.Method(
            "writeln/2",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> writeln(methodScope, 2)
        ));
        scope.define("writeln/3", new RuntimeValue.Method(
            "writeln/3",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "param1",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param3",
                        new RuntimeValue.Primitive(new Object()),
                        false
                    )
                ),
                null
            ),
            (methodScope) -> writeln(methodScope, 3)
        ));

        scope.define("Exit/0", new RuntimeValue.Method(
            "Exit/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(),
                null
            ),
            (Scope _) -> { throw new ReturnException(); }
        ));

        scope.define("Break/0", new RuntimeValue.Method(
            "Break/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(),
                null
            ),
            (Scope _) -> { throw new BreakException(); }
        ));

        scope.define("Continue/0", new RuntimeValue.Method(
            "Continue/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(),
                null
            ),
            (Scope _) -> { throw new ContinueException(); }
        ));


        return scope;
    }

    /**
     * Prints the provided values to the console, one after another, with no ending newline character.
     * 
     * @return a RuntimeValue.Primitive() with value null. 
     */
    private static void write(Scope methodScope, int numParams) {
        for (int i = 1; i <= numParams; i++) {
            System.out.print(methodScope.lookup("param" + i).get().getPrintString());
        }
    }

    /**
     * Same as 'write', but finishes with an ending newline character. 
     * 
     * Internally, this function calls 'write' to provide functionality.
     * 
     * @return a RuntimeValue.Primitive() with value null. 
     */
    private static void writeln(Scope methodScope, int numParams) {
        write(methodScope, numParams);
        System.out.println();
    }

    // TODO: read and readln here...

}
