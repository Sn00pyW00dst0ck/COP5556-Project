package plp.group.Compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        public java.lang.String getType() { return "ptr"; }
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
     * Represents function in LLVM IR, has a common set of methods...
     */
    public interface LLVMFunction extends LLVMValue {

        public java.lang.String returnType();
        public java.lang.String getDeclare();
        public java.lang.String getDefineHeader();
        public java.lang.String getSignature();
        public Optional<LLVMValue.Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder);
    
        /**
         * Represents a user defined function in LLVM
         */
        public record UserFunction(
            java.lang.String name,
            java.lang.String returnType,
            List<java.lang.String> paramTypes,
            Optional<AST.Block> body // no body = extern / built in
        ) implements LLVMFunction {
            @Override
            public java.lang.String getType() { return returnType + " (" + java.lang.String.join(", ", paramTypes) + ")*"; }
            
            @Override
            public java.lang.String returnType() { return returnType; }

            @Override
            public java.lang.String getRef() { return "@" + name; }
    
            @Override
            public java.lang.String getSignature() { return returnType + " @" + name + "(" + java.lang.String.join(", ", paramTypes) + ")"; }
        
            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }
        
            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }
    
            @Override
            public Optional<LLVMValue.Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                // If not void, assign result to a temp
                java.lang.String tmp = "";
                if (!returnType.equals("void")) {
                    tmp = context.getNextTmp();
                    irBuilder.append("  " + tmp + " = ");    
                }
    
                irBuilder.append("call " + returnType + " " + getRef() + "(");
                // Add each argument to the call string.
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                
                // if not void, return register result otherwise return optional empty.
                if (!returnType.equals("void")) {
                    return Optional.of(new LLVMValue.Register(tmp, returnType));
                } else {
                    return Optional.empty();
                }
            }
        }

        /**
         * Built in function 'write'
         */
        public record WriteFunction() implements LLVMFunction {

            @Override
            public java.lang.String getType() { return "void (...)"; }

            @Override
            public java.lang.String returnType() { return "void"; }

            @Override
            public java.lang.String getRef() { return "@write"; }

            @Override
            public java.lang.String getSignature() { return "void @write(...)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                // Construct a format string for the printf to use...
                StringBuilder fmt = new StringBuilder();
                List<LLVMValue> printfArgs = new ArrayList<>();
                for (LLVMValue arg : args) {
                    fmt.append(switch (arg.getType()) {
                        case "i32" -> "%d";
                        case "double" -> "%f";
                        case "i1" -> "%d";
                        case "i8" -> "%c";
                        case "i8*" -> "%s";
                        case "ptr" -> "%s";
                        default -> throw new RuntimeException("Unsupported type in write(): " + arg.getType());
                    });
                    printfArgs.add(arg);
                }

                // If this format string is unseen, then add it to the symbol table 
                var strings = context.symbolTable.getEntriesOfType(LLVMValue.String.class, false);
                LLVMValue.String fmtString;
                if (!strings.keySet().contains("'" + fmt.toString() + "'")) {
                    fmtString = new LLVMValue.String(context.getNextString(), fmt.toString());
                    context.symbolTable.define("'" + fmt.toString() + "'", fmtString);
                    context.ir.appendStringConstant(fmtString.getGlobalDefinition());
                } else {
                    fmtString = (LLVMValue.String) strings.get("'" + fmt.toString() + "'");
                }

                // Call printf with the args...
                irBuilder.append("call i32 (ptr, ...) @printf(");
                irBuilder.append(fmtString.getType() + " " + fmtString.getRef());
                if (args.size() > 0) {
                    irBuilder.append(", ");
                }
                // Add each argument to the call string.
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");

                return Optional.empty();
            }
        };

        /**
         * Built in function 'writeln'
         */
        public record WritelnFunction() implements LLVMFunction {

            @Override
            public java.lang.String getType() { return "void (...)"; }

            @Override
            public java.lang.String returnType() { return "void"; }

            @Override
            public java.lang.String getRef() { return "@write"; }

            @Override
            public java.lang.String getSignature() { return "void @write(...)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                // Construct a format string for the printf to use...
                StringBuilder fmt = new StringBuilder();
                List<LLVMValue> printfArgs = new ArrayList<>();
                for (LLVMValue arg : args) {
                    fmt.append(switch (arg.getType()) {
                        case "i32" -> "%d";
                        case "double" -> "%f";
                        case "i1" -> "%d";
                        case "i8" -> "%c";
                        case "i8*" -> "%s";
                        case "ptr" -> "%s";
                        default -> throw new RuntimeException("Unsupported type in write(): " + arg.getType());
                    });
                    printfArgs.add(arg);
                }
                fmt.append("\n");

                // If this format string is unseen, then add it to the symbol table 
                var strings = context.symbolTable.getEntriesOfType(LLVMValue.String.class, false);
                LLVMValue.String fmtString;
                if (!strings.keySet().contains("'" + fmt.toString() + "'")) {
                    fmtString = new LLVMValue.String(context.getNextString(), fmt.toString());
                    context.symbolTable.define("'" + fmt.toString() + "'", fmtString);
                    context.ir.appendStringConstant(fmtString.getGlobalDefinition());
                } else {
                    fmtString = (LLVMValue.String) strings.get("'" + fmt.toString() + "'");
                }

                // Call printf with the args...
                irBuilder.append("call i32 (ptr, ...) @printf(");
                irBuilder.append(fmtString.getType() + " " + fmtString.getRef());
                if (args.size() > 0) {
                    irBuilder.append(", ");
                }
                // Add each argument to the call string.
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");

                return Optional.empty();
            }
        };

        // TODO: built in functions for writeln, read, readln, etc...
    };

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
