package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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

        // Write and Writeln can take 0, 1, or 2 arguments, so this is more accurate than what we did for P1 with variable args... 
        // Source: https://www.freepascal.org/docs-html/rtl/system/writeln.html

        scope.define("write/1", new RuntimeValue.Method(
            "write/1", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ),
            Environment::write
        ));
        scope.define("write/2", new RuntimeValue.Method(
            "write/2", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ), 
            Environment::write
        ));
        scope.define("write/3", new RuntimeValue.Method(
            "write/3", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ), 
            Environment::write
        ));

        scope.define("writeln/1", new RuntimeValue.Method(
            "writeln/1", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ), 
            Environment::writeln
        ));
        scope.define("writeln/2", new RuntimeValue.Method(
            "writeln/2", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ), 
            Environment::writeln
        ));
        scope.define("writeln/3", new RuntimeValue.Method(
            "writeln/3", 
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object()), new RuntimeValue.Primitive(new Object())),
                new RuntimeValue.Primitive(null)
            ), 
            Environment::writeln
        ));
        
        /*
         * TODO: read and readln here, they will be tough because they have to be variadic I think, unless we want to force only one variable. 
         * Also, references will be a thing to deal with and it will be tough...
         */

        scope.define("Exit/0", new RuntimeValue.Method(
            "Exit/0", 
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                new RuntimeValue.Primitive(null)
            ), 
            Environment::exit)
        );
        
        // https://www.freepascal.org/docs-html/rtl/system/break.html
        scope.define("Break/0", new RuntimeValue.Method(
            "Break/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                new RuntimeValue.Primitive(null)
            ),
            Environment::Break)
        );

        // https://www.freepascal.org/docs-html/rtl/system/continue.html
        scope.define("Continue/0", new RuntimeValue.Method(
            "Continue/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(), 
                new RuntimeValue.Primitive(null)
            ),
            Environment::Continue)
        );

        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html
        scope.define("odd/1", new RuntimeValue.Method(
            "odd/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new BigInteger("0"))), 
                new RuntimeValue.Primitive(Boolean.valueOf("true"))
            ),
            (List<RuntimeValue> arguments) -> {
                BigInteger num = RuntimeValue.requireType(arguments.get(0), BigInteger.class);
                return new RuntimeValue.Primitive(Boolean.valueOf(!num.testBit(0)));
            })
        );

        // https://www.freepascal.org/docs-html/rtl/system/round.html
        scope.define("round/1", new RuntimeValue.Method(
            "round/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new BigDecimal("0.0"))), 
                new RuntimeValue.Primitive(new BigInteger("0"))
            ),
            (List<RuntimeValue> arguments) -> {
                BigDecimal num = RuntimeValue.requireType(arguments.get(0), BigDecimal.class);
                return new RuntimeValue.Primitive(num.setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact());
            })
        );

        // https://www.freepascal.org/docs-html/rtl/system/trunc.html
        scope.define("trunc/1", new RuntimeValue.Method(
            "trunc/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new BigDecimal("0.0"))), 
                new RuntimeValue.Primitive(new BigInteger("0"))
            ),
            (List<RuntimeValue> arguments) -> {
                BigDecimal num = RuntimeValue.requireType(arguments.get(0), BigDecimal.class);
                return new RuntimeValue.Primitive(num.toBigIntegerExact());
            })
        );

        // https://www.freepascal.org/docs-html/rtl/system/sqrt.html
        scope.define("sqrt/1", new RuntimeValue.Method(
            "sqrt/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(new RuntimeValue.Primitive(new BigDecimal("0.0"))), 
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (List<RuntimeValue> arguments) -> {
                BigDecimal num = RuntimeValue.requireType(arguments.get(0), BigDecimal.class);
                return new RuntimeValue.Primitive(num.sqrt(null));
            })
        );

        // TODO: trigonometric functions for BigDecimal in java are very hard, might need a library to do built in sin, cos, pi, etc...

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
     * The 'Exit()' procedure built into delphi will immediately stop execution of a function, or otherwise exit the program. 
     * We throw a Return control flow exception to show this behavior. 
     * 
     * @param arguments should be an empty list
     * @return nothing, always throws a Return exception. 
     */
    private static RuntimeValue exit(List<RuntimeValue> arguments) {
        throw new ReturnException();
    }

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
