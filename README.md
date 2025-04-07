# COP5556-Project

![Maven CI/CD](https://github.com/Sn00pyW00dst0ck/COP5556-Project/actions/workflows/build.yml/badge.svg)

This is a pseudo-delphi interpreter written utilizing ANTLR4 and Java. 

By [Gabriel Aldous](https://github.com/Sn00pyW00dst0ck) (70444594) & [Rishika Sharma](https://github.com/rishika64) (32772571)

## Project Setup 

> [!IMPORTANT]  
> This project was built using the following tools:
> ```
> Apache Maven 3.9.4 
> Java version: 23.0.0, vendor: Oracle Corporation
> ```

This project should build fine with another vendor's version of Java v23 provided that Apache Maven is properly setup.

Please ensure that these tools are downloaded before building the project. We recommend using the [asdf multiple runtime version manager](https://asdf-vm.com/) if you need to install multiple versions of Java.

In addition, this repository was built utilizing VS Code, and the setup/run instructions are created with this in mind. 
To build and execute utilizing VS Code, please install the [extension pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).

The following libraries are utilized:
1. [ANTLR4](https://www.antlr.org/) - to perform program file lexing and parsing.
2. [JLine](https://jline.org/) - to simplify reading input from the user within the CLI program, and to provide advanced features like command history via arrow keys.
3. [Big-Math](https://eobermuhlner.github.io/big-math/) - to support trigonometric, and other advanced mathematical operations on **BigDecimal** data type, which Java does not natively support. 

### Building the Project

When within the top level directory, VSCode should identify the Maven Java project. The `Explorer` tab should appear in the vertical toolbar to the left of VSCode. You can expand the `Maven` tab to view all of the available Maven lifecycle commands. Run the `package` lifecycle command to generate the ANTLR4 created classes, which are necessary for execution. This command will also run the unit tests for the project.

Alternatively, build the project from the command line by running the following command within the `./project` directory:
```
> cd project
> mvn clean package
```

VSCode has been setup such that when viewing any of the `.java` files within the `src` directory the run button will be available in the top left corner. This can be used to run the application either in normal or debug mode. 

### Using Command Line

Run the interpreter by executing:
```
java -jar target/cop5556-project.jar <options>
```

## Running Test Cases

This project includes unit tests to verify the functionality of the interpreter. For running Tests with Maven:
```
mvn test
```

Test cases are located in `src/test/java/plp/group/`.

## Project Overview

Below is the layout of the project. 

```
COP5556-Project/
├── .github/                     # CI/CD workflows and templates
├── .vscode/                     # VS Code workspace settings
├── project/
│   ├── pom.xml                  # Maven build file
│   ├── src/
│   │   ├── main/
│   │   │   ├── antlr4/           # ANTLR grammar files
│   │   │   ├── java/plp/group/   # Java source code
│   │   │   │   ├── Interpreter/  # Interpreter logic
│   │   │   │   ├── Optimizer/    # Optimizer logic
│   │   │   │   ├── App.java      # Command-line interface
│   │   │   ├── resources/        # Sample test Pascal programs
│   │   ├── test/                 # Unit tests
```

### Interpreter Logic

The approach taken for the **Interpreter** within P1 had some good elements, but the overly complex and difficult to maintain type system held back the project. The previous approach utilized inheritance and a unique class for each delphi type, which was not sustainable and clashed with parts of the language implementation (notably classes and objecct instantiation). 

This time, a different approach is utilized. We have abstracted *most* things which are calculated at program execution into a **RuntimeValue** interface, which has many classes implementing it. This abstraction allows for the creation of a `requireType` helper method which can properly cast a **RuntimeValue** into any specified Java class (or throw proper exceptions when not possible). This setup significantly simplifies the process of type checking for interpreter operations, and moves logic for operators out of individual classes and into the Interpreter itself, resulting in a more unified codebase. 

Furthermore, we have reworked the **Scope** class to utilize the **RuntimeValue** abstraction and to make it less likely to accidentally create unnecessary scopes. Additionally, an **Environment** class was created which provides an initial scope with implementations for many of the Pascal built in functions, including *write*, *writeln*, *read*, *readln*, and trigonometric/mathematical functions. 

### Optimizer Logic

The **Optimizer** class is new to this project and did not exist in the previous iteration. The **Optimizer** is a visitor which walks over the **ANTLR4** generated parse tree and performs a simple version of constant propagation. It does this by walking the tree and transforming the tree back into a string representation of the program, but interrupting the process during the evaluation of expressions so that any expressions formed of purely constant values may be pre-calculated, and the calculated outcome written instead. 

The re-written program string can then be re-parsed by **ANTLR4** and then ran utilizing the **Interpreter**. Since the **Interpreter** and **Optimizer** are completely independent, the CLI interface utilized to run the program can show parse trees when optimized or unoptimized, and allow interpretation when optimized or not optimized via command line argument flags.

The **Optimizer** is tested to ensure that it does not invalidate any valid program parse trees, and to ensure that calcualtions performed are correct.

## What Is Implemented

The following items have been implemented within this version of the project: 

1. Updated `delphi.g4` and `delphi_lexer.g4` grammar files to properly parse class definitions and usages. 
2. Implemented a more robust command line interface for utilizing the **Interpreter** and **Optimizer**. 
3. Implemented an **Optimizer** capable of performing a simplified variation of constant propogation.
    - Operations with constant literal values are evaluated as much as possible.
    - Calculations involving more complex items such as variables, procedure and function calls, and other complex structures are NOT evaluated. 
4. Implemented the following built in procedures and functions of the Pascal programming language. They may be redefined by the programmer, as is consistent with the Pascal programming language:
    - *write*
    - *writeln*
    - *read* - NOTE: currently only works for reading integers
    - *readln* - NOTE: currently only works for reading integers
    - *Exit*
    - *Continue*
    - *Break*
    - *arctan*
    - *chr*
    - *cos*
    - *exp*
    - *ln*
    - *odd*
    - *pi*
    - *round*
    - *sin*
    - *sqrt*
    - *trunc*
5. Implemented the ability to interpret expressions. 
6. Implemented the ability to interpret the following types of statements:
    - while loops
    - for loops
    - procedure call statements
    - return statement
7. Implemented the ability for users to define custom procedures and functions. 
    - Parameters may be passed by value or by reference. 
    - Parameters passed by reference are correctly modified outside of the function call.
    - Currently, parameters defined to be `PROCEDURE` or `FUNCTION` type are unsupported. 
8. 
9. 
10. 
11. Unit tests for nearly all of the above mentioned features.

## Known Bugs & Limitations

> [!CAUTION]
> The following items have been identified as known bugs and limitations within this version of the project:

1. 
2. 
3. 
4. 
5. 

## References

The following online resources were utilized during the construction of this application.

1. https://docwiki.embarcadero.com/RADStudio/Athens/en/Delphi_Language_Reference
2. https://www.tutorialspoint.com/compile_pascal_online.php
3. https://www.onlinegdb.com/online_pascal_compiler
4. https://ssw.jku.at/General/Staff/ManuelRigger/thesis.pdf
5. https://www.antlr.org/api/
6. https://www.baeldung.com/java-antlr
7. https://www.oracle.com/technical-resources/articles/java/javareflection.html
8. https://docs.oracle.com/en/java/javase/23/
9. https://eobermuhlner.github.io/big-math/
10. https://jline.org/
11. https://www.freepascal.org/docs-html/rtl/system/writeln.html
12. https://www.dcs.ed.ac.uk/home/SUNWspro/3.0/pascal/lang_ref/ref_builtin.doc.html
13. https://www.freepascal.org/docs-html/rtl/system/index-5.html
