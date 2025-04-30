package plp.group.Compiler;

public class IRBuilder {
    
    public final StringBuilder globals = new StringBuilder();
    public final StringBuilder typeDefs = new StringBuilder();
    public final StringBuilder declarations = new StringBuilder();
    public final StringBuilder functionDefs = new StringBuilder();
    public final StringBuilder mainFunction = new StringBuilder();
    public final StringBuilder stringConstants = new StringBuilder();

    //#region Methods to append various things to the IR

    public void appendGlobal(String line) {
        globals.append(line).append("\n");
    }

    public void appendTypeDef(String line) {
        typeDefs.append(line).append("\n");
    }

    public void appendDeclaration(String line) {
        declarations.append(line).append("\n");
    }

    public void appendFunctionDef(String line) {
        functionDefs.append(line).append("\n");
    }

    public void appendToMain(String line) {
        mainFunction.append(line).append("\n");
    }

    public void appendStringConstant(String line) {
        stringConstants.append(line).append("\n");
    }

    //#endregion Methods to append various things to the IR

    /**
     * Output the full IR 
     * @return string of the full IR.
     */
    public String build() {
        StringBuilder finalIR = new StringBuilder();

        finalIR.append("; === Type Declarations ===\n");
        finalIR.append(typeDefs);
        finalIR.append("\n");

        finalIR.append("; === String Constants ===\n");
        finalIR.append(stringConstants);
        finalIR.append("\n");

        finalIR.append("; === Globals ===\n");
        finalIR.append(globals);
        finalIR.append("\n");

        finalIR.append("; === Declarations ===\n");
        finalIR.append(declarations);
        finalIR.append("\n");

        finalIR.append("; === Functions ===\n");
        finalIR.append(functionDefs);
        finalIR.append("\n");

        finalIR.append("; === Main Function ===\n");
        finalIR.append("define i32 @main() {\n");
        finalIR.append(mainFunction);
        finalIR.append("ret i32 0\n");
        finalIR.append("}\n");

        return finalIR.toString();
    }
}
