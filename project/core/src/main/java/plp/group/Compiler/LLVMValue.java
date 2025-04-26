package plp.group.Compiler;

import java.util.HashMap;
import java.util.Map;

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
        private static final Map<java.lang.String, java.lang.String> stringConstants = new HashMap<>();
        
        public java.lang.String getType() { return "[" + length + " x i8]"; }
        public java.lang.String getRef() { return "@" + name; }
        
        public static void addString(java.lang.String value, java.lang.String name) {
            stringConstants.put(value, name);
        }

        public java.lang.String getGlobalDefinition() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<java.lang.String, java.lang.String> entry : stringConstants.entrySet()) {
                java.lang.String value = entry.getKey();
                java.lang.String name = entry.getValue();
                int length = value.length() + 1; // +1 for null terminator
                sb.append(name)
                  .append(" = private unnamed_addr constant [")
                  .append(length)
                  .append(" x i8] c\"")
                  .append(escapeString(value))
                  .append("\\00\"\n");
            }  
            return sb.toString();
        }

        /**
         * Escapes special characters for LLVM IR string constants
         */
        private java.lang.String escapeString(java.lang.String s) {
            return s.replace("\\", "\\5C")
                .replace("\n", "\\0A")
                .replace("\t", "\\09")
                .replace("\"", "\\22");
        }
    }
}
