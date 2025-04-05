package plp.group.Interpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;

import ch.obermuhlner.math.big.BigDecimalMath;
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
                        false,
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
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false,
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
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param3",
                        new RuntimeValue.Primitive(new Object()),
                        false,
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
                        false,
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
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false,
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
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param2",
                        new RuntimeValue.Primitive(new Object()),
                        false,
                        false
                    ),
                    new RuntimeValue.Method.MethodParameter(
                        "param3",
                        new RuntimeValue.Primitive(new Object()),
                        false,
                        false
                    )
                ),
                null
            ),
            (methodScope) -> writeln(methodScope, 3)
        ));

        scope.define("read/X", new RuntimeValue.Method(
            "read/X",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "Arguments", 
                        new RuntimeValue.Primitive(new Object()), // arguments is an array because variadic, but this is the type of the things in the array...
                        true,
                        true
                    )
                ),
                null
            ),
            Environment::read
        ));
        scope.define("readln/X", new RuntimeValue.Method(
            "readln/X",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "Arguments", 
                        new RuntimeValue.Primitive(new Object()), // arguments is an array because variadic, but this is the type of the things in the array...
                        true,
                        true
                    )
                ),
                null
            ),
            Environment::readln
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

        // Following added based on this: https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html
        // The only ones from that table not added are ones with multiple signatures.
        
        // https://www.freepascal.org/docs-html/rtl/system/arctan.html
        scope.define("arctan/1", new RuntimeValue.Method(
            "arctan/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ), 
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.atan(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );
        
        scope.define("chr/1", new RuntimeValue.Method(
            "chr/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "c",
                        new RuntimeValue.Primitive(new BigInteger("0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(Character.valueOf('a'))
            ),
            (Scope methodScope) -> {
                BigInteger num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("c").get(), RuntimeValue.Variable.class), BigInteger.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive((char) num.intValue());
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );
        
        // https://www.freepascal.org/docs-html/rtl/system/cos.html
        scope.define("cos/1", new RuntimeValue.Method(
            "cos/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "theta",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("theta").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.cos(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );
        
        // https://www.freepascal.org/docs-html/rtl/system/exp.html
        scope.define("exp/1", new RuntimeValue.Method(
            "exp/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),            
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.exp(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

        // https://www.freepascal.org/docs-html/rtl/system/ln.html
        scope.define("ln/1", new RuntimeValue.Method(
            "ln/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),            
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.log(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );
        
        // https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html
        scope.define("odd/1", new RuntimeValue.Method(
            "odd/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigInteger("0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(Boolean.valueOf(true))
            ),
            (Scope methodScope) -> {
                BigInteger num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigInteger.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(Boolean.valueOf(!num.testBit(0)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

        // https://www.freepascal.org/docs-html/rtl/system/pi.html
        scope.define("pi/0", new RuntimeValue.Method(
            "pi/0",
            new RuntimeValue.Method.MethodSignature(
                List.of(),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (Scope methodScope) -> {
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.pi(new MathContext(20)).setScale(20));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

        
        // https://www.freepascal.org/docs-html/rtl/system/round.html
        scope.define("round/1", new RuntimeValue.Method(
            "round/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigInteger("0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(num.setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact());
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

        
        // https://www.freepascal.org/docs-html/rtl/system/sin.html
        scope.define("sin/1", new RuntimeValue.Method(
            "sin/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "theta",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("theta").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.sin(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

        
        // https://www.freepascal.org/docs-html/rtl/system/sqrt.html
        scope.define("sqrt/1", new RuntimeValue.Method(
            "sqrt/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigDecimal("0.0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(BigDecimalMath.sqrt(num, new MathContext(20)));
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );
        
        // https://www.freepascal.org/docs-html/rtl/system/trunc.html
        scope.define("trunc/1", new RuntimeValue.Method(
            "trunc/1",
            new RuntimeValue.Method.MethodSignature(
                List.of(
                    new RuntimeValue.Method.MethodParameter(
                        "x",
                        new RuntimeValue.Primitive(new BigDecimal("0.0")),
                        false,
                        false
                    )
                ),
                new RuntimeValue.Primitive(new BigInteger("0"))
            ),
            (Scope methodScope) -> {
                BigDecimal num = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("x").get(), RuntimeValue.Variable.class), BigDecimal.class);
                RuntimeValue.Primitive result = new RuntimeValue.Primitive(num.setScale(0, RoundingMode.DOWN).toBigIntegerExact());
                RuntimeValue.requireType(methodScope.lookup("result").get(), RuntimeValue.Variable.class).setValue(result);
            })
        );

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

    // IMPORTANT! Below reader needs to be set before executing any code with read or readln... 
    private static LineReader reader;
    public static void setReader(LineReader reader) { Environment.reader = reader; }
    private static List<String> tokenBuffer = new ArrayList<>();

    /**
     * Helper method which prompts to read a full line and converts it into tokens if the tokenBuffer is empty.
     */
    private static void ensureTokenBuffer() {
        if (tokenBuffer.isEmpty()) {
            String line = reader.readLine("");
            tokenBuffer.addAll(List.of(line.trim().split("\\s+")));
        }
    }

    /**
     * Place tokens one at a time into the variables given, error if not possible.
     * @param methodScope the scope available to the function call.
     */
    private static void read(Scope methodScope) {
        ensureTokenBuffer();
        
        RuntimeValue.Array arguments = RuntimeValue.requireType(RuntimeValue.requireType(methodScope.lookup("Arguments").get(), RuntimeValue.Variable.class).value(), RuntimeValue.Array.class);

        for (int i = 0; i < arguments.size(); i++) {
            String token = tokenBuffer.remove(0);
            // TODO: smarter type parsing for types below! 
            RuntimeValue.requireType(arguments.get(i), RuntimeValue.Reference.class).setValue(new RuntimeValue.Primitive(new BigInteger(token)));
        }
    }

    /**
     * Like read, but gurantees empty buffer at end of call.
     * @param methodScope the scope available to the function call.
     */
    private static void readln(Scope methodScope) {
        read(methodScope);
        tokenBuffer.clear();
    }

}
