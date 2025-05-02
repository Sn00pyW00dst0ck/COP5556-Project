; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [22 x i8] c"Hello from GetMessage\00"
@llvm.str.1 = private unnamed_addr constant [4 x i8] c"%d\0A\00"
@llvm.str.2 = private unnamed_addr constant [4 x i8] c"%s\0A\00"

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
define i32 @Sum(i32 %a, i32 %b, i32 %c) {
%result = alloca i32
%tmp0 = add i32 %b, %c
%tmp1 = add i32 %a, %tmp0
store i32 %tmp1, i32* %result
%tmp2 = load i32, ptr %result
ret i32 %tmp2
}
define ptr @GetMessage() {
%result = alloca ptr
store ptr @llvm.str.0, ptr %result
%tmp3 = load ptr, ptr %result
ret ptr %tmp3
}

; === Main Function ===
define i32 @main() {
  %tmp4 = call i32 @Sum(i32 67, i32 45, i32 15)
call i32 (ptr, ...) @printf(ptr @llvm.str.1, i32 %tmp4)
  %tmp5 = call ptr @GetMessage()
call i32 (ptr, ...) @printf(ptr @llvm.str.2, ptr %tmp5)
ret i32 0
}
