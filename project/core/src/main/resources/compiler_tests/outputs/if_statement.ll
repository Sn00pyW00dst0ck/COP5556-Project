; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [18 x i8] c"a is less than 20\00"
@llvm.str.1 = private unnamed_addr constant [22 x i8] c"a is not less than 20\00"
@llvm.str.2 = private unnamed_addr constant [17 x i8] c"value of a is : \00"
@llvm.str.3 = private unnamed_addr constant [4 x i8] c"%s\0A\00"
@llvm.str.4 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"

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
%a = alloca i32
store i32 100, i32* %a
%tmp0 = load i32, i32* %a
%tmp1 = icmp slt i32 %tmp0, 20
br i1 %tmp1, label %label_internal_1, label %label_internal_2
label_internal_1:
call i32 (ptr, ...) @printf(ptr @llvm.str.3, ptr @llvm.str.0)
br label %label_internal_3
label_internal_2:
call i32 (ptr, ...) @printf(ptr @llvm.str.3, ptr @llvm.str.1)
br label %label_internal_3
label_internal_3:
%tmp2 = load i32, i32* %a
call i32 (ptr, ...) @printf(ptr @llvm.str.4, ptr @llvm.str.2, i32 %tmp2)
ret i32 0
}
