; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [14 x i8] c"Hello, world.\00"
@llvm.str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00"

; === Globals ===

; === Declarations ===
declare i32 @printf(ptr noundef, ...)
declare double @atan(double)
declare double @cos(double)
declare double @exp(double)
declare double @log(double)
declare double @sin(double)
declare double @sqrt(double)
declare double @trunc(double)

; === Functions ===

; === Main Function ===
define i32 @main() {
call i32 (ptr, ...) @printf(ptr @llvm.str.1, ptr @llvm.str.0)
ret i32 0
}

