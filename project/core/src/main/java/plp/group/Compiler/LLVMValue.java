package plp.group.Compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import plp.group.AST.AST;

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
     * Represents a register (something like %tmp, %1, etc).
     */
    public record Register(
        java.lang.String name,
        java.lang.String type
    ) implements LLVMValue {
        public java.lang.String getType() { return type; }
        public java.lang.String getRef() { return name; }
    }

    public record Global(
        java.lang.String name,
        java.lang.String type
    ) implements LLVMValue {
        public java.lang.String getType() { return type; }
        public java.lang.String getRef() { return "@" + name; }
    }

    /**
     * Represents a constant value for immediate use (except for strings which are separate).
     */
    public record Immediate(
        java.lang.String value,
        java.lang.String type
    ) implements LLVMValue {
        public java.lang.String getType() { return type; }
        public java.lang.String getRef() { return value; }
    }

    /**
     * Represents a String LLVMValue...
     */
    public record String(
        java.lang.String name,
        java.lang.String value
    ) implements LLVMValue {        
        public java.lang.String getType() { return "i8*"; }
        // NOTE: name should already have "@"
        public java.lang.String getRef() { return name; }

        public java.lang.String getGlobalDefinition() {
            StringBuilder sb = new StringBuilder();

            sb.append(name)
                .append(" = private unnamed_addr constant [")
                .append(value.length() + 1)
                .append(" x i8] c\"")
                .append(this.escapeString(value))
                .append("\\00\"");
            
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

    /**
     * Represents a pointer to another LLVMValue.
     * 
     * TODO: getType and getRef here don't make as much sense, gotta figure something out
     */
    public record Pointer(
        java.lang.String name,
        LLVMValue pointee
    ) implements LLVMValue {
        public java.lang.String getType() { return pointee.getType() + "*"; }
        public java.lang.String getRef() { return name; }

        public java.lang.String getPointeeType() {
            return pointee.getType();
        }        
    }

    /**
     * Represents a function in LLVM
     */
    public record Function(
        java.lang.String name,
        java.lang.String returnType,
        List<java.lang.String> paramTypes,
        Optional<AST.Block> body // no body = extern / built in
    ) implements LLVMValue {
        public java.lang.String getType() { return returnType + " (" + java.lang.String.join(", ", paramTypes) + ")*"; }
        public java.lang.String getRef() { return "@" + name; }

        public java.lang.String getSignature() {
            return returnType + " @" + name + "(" + java.lang.String.join(", ", paramTypes) + ")";
        }
    
        public java.lang.String getDeclare() {
            return "declare " + getSignature();
        }
    
        public java.lang.String getDefineHeader() {
            return "define " + getSignature() + " {";
        }

        public Optional<LLVMValue.Register> emitCall(List<LLVMValue> args, CompilerContext context) {
            // If not void, assign result to a temp
            java.lang.String tmp = "";
            if (!returnType.equals("void")) {
                tmp = context.getNextTmp();
                context.ir.append("  " + tmp + " = ");    
            }

            context.ir.append("call " + returnType + " " + getRef() + "(");
            // Add each argument to the call string.
            for (int i = 0; i < args.size(); i++) {
                context.ir.append(args.get(i).getType() + " " + args.get(i).getRef());
                if (i < args.size() - 1) context.ir.append(", ");
            }
            context.ir.append(")\n");
            
            // if not void, return register result otherwise return optional empty.
            if (!returnType.equals("void")) {
                return Optional.of(new LLVMValue.Register(tmp, returnType));
            } else {
                return Optional.empty();
            }
        }

    }

    /**
     * Represents a struct in LLVM
     * 
     * TODO: figure out what a struct will need to look like in the symbol table...
     */
    public record Struct(
    ) implements LLVMValue {
        public java.lang.String getType() { return ""; }
        public java.lang.String getRef() { return ""; }
    }
}
