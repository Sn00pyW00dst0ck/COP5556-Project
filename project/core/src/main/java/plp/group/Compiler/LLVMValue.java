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
         * Represents a user defined function in LLVM.
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
         * Built in function 'write'.
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
         * Built in function 'writeln'.
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

        /**
         * Built in function 'arctan'.
         */
        public record ArctanFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@atan"; }

            @Override
            public java.lang.String getSignature() { return "double @atan(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @atan(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'cos'.
         */
        public record CosFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@cos"; }

            @Override
            public java.lang.String getSignature() { return "double @cos(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @cos(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'exp'.
         */
        public record ExpFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@exp"; }

            @Override
            public java.lang.String getSignature() { return "double @exp(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @exp(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'ln'.
         */
        public record LogFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@log"; }

            @Override
            public java.lang.String getSignature() { return "double @log(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @log(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'odd'.
         */
        public record OddFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "i1 (i32)"; }

            @Override
            public java.lang.String returnType() { return "i1"; }

            @Override
            public java.lang.String getRef() { return ""; }

            @Override
            public java.lang.String getSignature() { return ""; }

            @Override
            public java.lang.String getDeclare() { return ""; }

            @Override
            public java.lang.String getDefineHeader() { return ""; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                // Below is a way to check odd:
                // %tmp = and i32 %x, 1
                // %res = icmp ne i32 %tmp, 0

                java.lang.String andTmp = context.getNextTmp();
                java.lang.String resultTmp = context.getNextTmp();
                irBuilder.append(andTmp + " = and " + args.get(0).getType() + " " + args.get(0).getRef() + ", 1\n");
                irBuilder.append(resultTmp + " = icmp ne i32 " + andTmp + ", 0\n");
                return Optional.of(new LLVMValue.Register(resultTmp, "i1"));
            }
        };

        /**
         * Built in function 'sin'.
         */
        public record SinFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@sin"; }

            @Override
            public java.lang.String getSignature() { return "double @sin(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @sin(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'sqrt'.
         */
        public record SqrtFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@sqrt"; }

            @Override
            public java.lang.String getSignature() { return "double @sqrt(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @sqrt(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'trunc'.
         */
        public record TruncFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "double (double)"; }

            @Override
            public java.lang.String returnType() { return "double"; }

            @Override
            public java.lang.String getRef() { return "@trunc"; }

            @Override
            public java.lang.String getSignature() { return "double @trunc(double)"; }

            @Override
            public java.lang.String getDeclare() { return "declare " + getSignature(); }

            @Override
            public java.lang.String getDefineHeader() { return "define " + getSignature() + " {"; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                java.lang.String tmp = context.getNextTmp();
                irBuilder.append(tmp + " = call double @trunc(");
                for (int i = 0; i < args.size(); i++) {
                    irBuilder.append(args.get(i).getType() + " " + args.get(i).getRef());
                    if (i < args.size() - 1) irBuilder.append(", ");
                }
                irBuilder.append(")\n");
                return Optional.of(new LLVMValue.Register(tmp, "double"));
            }
        };

        /**
         * Built in function 'round'.
         */
        public record RoundFunction() implements LLVMFunction {
            @Override
            public java.lang.String getType() { return "i32 (double)"; }

            @Override
            public java.lang.String returnType() { return "i32"; }

            @Override
            public java.lang.String getRef() { return ""; }

            @Override
            public java.lang.String getSignature() { return ""; }

            @Override
            public java.lang.String getDeclare() { return ""; }

            @Override
            public java.lang.String getDefineHeader() { return ""; }

            @Override
            public Optional<Register> emitCall(List<LLVMValue> args, CompilerContext context, StringBuilder irBuilder) {
                // Below is a way to round:
                // %add = fadd double %x, 0.5
                // %result = fptosi double %add to i32

                java.lang.String addTmp = context.getNextTmp();
                java.lang.String resultTmp = context.getNextTmp();
                irBuilder.append(addTmp + " = fadd " + args.get(0).getType() + " " + args.get(0).getRef() + ", 0.5\n");
                irBuilder.append(resultTmp + " = fptosi double " + addTmp + " to i32\n");
                return Optional.of(new LLVMValue.Register(resultTmp, "double"));
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
