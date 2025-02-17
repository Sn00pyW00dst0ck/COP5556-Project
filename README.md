# COP5556-Project

This is a pseudo-delphi interpreter written utilizing ANTLR4 and Java. 

## Project Setup 

This project was built using the following tools:
```
Apache Maven 3.9.4 
Java version: 22.0.2, vendor: Oracle Corporation
```

This project should build fine with another vendor's version of Java v22 provided that Apache Maven is properly setup.

Please ensure that these tools are downloaded before building the project. We recommend using the [asdf multiple runtime version manager](https://asdf-vm.com/) if you need to install multiple versions of Java.

In addition, this repository was built utilizing VS Code, and the setup/run instructions are created with this in mind. 
To build and execute utilizing VS Code, please install the [extension pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).


### Building the Project

When within the top level directory, VSCode should identify the Maven Java project. The `Explorer` tab should appear in the vertical toolbar to the left of VSCode. You can expand the `Maven` tab to view all of the available Maven lifecycle commands. Run the `package` lifecycle command to generate the ANTLR4 created classes, which are necessary for execution. This command will also run the unit tests for the project.

Alternatively, build the project from the command line by running the following command within the `./project` directory:
```
mvn clean package
```

VSCode has been setup such that when viewing any of the `.java` files within the `src` directory the run button will be available in the top left corner. This can be used to run the application either in normal or debug mode. 

## Project Overview

### Implemented:
1. Pascal simple types, including operations on these types (comparisons, '+', '-', 'in', etc.).
2. Pascal procedural types (procedure and function) and the ability to define them. 
- There is currently one inconsistency in this implementation in the interpreter (see below).
3. Pascal "built-in" functions for `write`, `writeln`, and `readln` functionality.
4. Conditional statements (if and case).
5. Repetetive statements (while, repeat, and for).
6. Pascal subranges and enumerations.
7. Labels and the `goto` statement.
8. Variables and assignments to variables.
9. Proper scoping on a per block level.
10. Unit testing on both a per-class and interpreter-wide scale. 
11. A simple `echo.pas` program which will echo out integers that the user inputs.
12. Command line interface (with help menu) that allows users to either view the parsed tree in a GUI window, or interpret the given program.
13. A set of `.pas` files for use in unit testing and demonstrating implmenented functionalities. These files can be loaded and interacted with via the CLI. 

### Un-implmented: 
1. Structural types (Pascal records, arrays, sets, and file types), and interpretation of those types.
2. Error messages are not given in a Delphi-like manner. Instead, if interpretation encounters an issue a stack trace of the java exception is shown.


### Known bugs & inconsistencies:
1. When working with integers or reals, the smallest type elligible for the value will always be utilized, regardless of user declared type.
2. The `write`, `writeln`, and `readln` functionality is hard-coded to work with the Java System.out and System.in streams, so no file operations are possible.
3. Functions and procedures are slightly inconsistent with the grammar in how they accept arguments. The interpreter expects the for all the 'by value' arguments first, followed by the 'by reference' values second. 
4. `goto` statement may or may not cause erratic behavior when jumping across scopes.

## References

The following online resources were utilized during the construction of this application.

1. https://docwiki.embarcadero.com/RADStudio/Athens/en/Delphi_Language_Reference
2. https://www.tutorialspoint.com/compile_pascal_online.php
3. https://www.onlinegdb.com/online_pascal_compiler
4. https://ssw.jku.at/General/Staff/ManuelRigger/thesis.pdf
5. https://www.antlr.org/api/
6. https://www.baeldung.com/java-antlr
7. https://www.oracle.com/technical-resources/articles/java/javareflection.html
8. https://docs.oracle.com/en/java/javase/22/
