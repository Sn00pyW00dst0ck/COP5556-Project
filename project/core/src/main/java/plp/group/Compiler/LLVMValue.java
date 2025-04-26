package plp.group.Compiler;

public interface LLVMValue {
    /**
     * Returns the type in LLVM
     */
    java.lang.String getType();

    /**
     * Returns the LLVM identifier / literal
     */
    java.lang.String getRef();

    // TODO: representations for register, function, struct, etc...

    /**
     * Represents a String LLVMValue...
     */
    public record String(
        java.lang.String name,
        int length
    ) implements LLVMValue {
        public java.lang.String getType() { return "[" + length + " x i8]"; }
        public java.lang.String getRef() { return "@" + name; }
    }
}
