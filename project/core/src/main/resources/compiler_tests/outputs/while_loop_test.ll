; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [23 x i8] c"While loop iteration: \00"
@llvm.str.1 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"

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
%i = alloca i32
store i32 1, i32* %i
br label %label_internal_1
label_internal_1:
%tmp0 = load i32, i32* %i
%tmp1 = icmp sle i32 %tmp0, 5
br i1 %tmp1, label %label_internal_2, label %label_internal_3
label_internal_2:
%tmp2 = load i32, i32* %i
call i32 (ptr, ...) @printf(ptr @llvm.str.1, ptr @llvm.str.0, i32 %tmp2)
%tmp3 = load i32, i32* %i
%tmp4 = add i32 %tmp3, 1
store i32 %tmp4, i32* %i
br label %label_internal_1
label_internal_3:
ret i32 0
}
