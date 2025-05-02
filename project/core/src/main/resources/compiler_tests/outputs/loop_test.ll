; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [30 x i8] c"Sum after FOR loop (1 to 5): \00"
@llvm.str.1 = private unnamed_addr constant [32 x i8] c"Sum after WHILE loop (1 to 5): \00"
@llvm.str.2 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"

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
%sum = alloca i32
store i32 0, i32* %sum
store i32 1, i32* %i
br label %label_internal_1
label_internal_1:
%tmp0 = load i32, i32* %i
%tmp1 = icmp sle i32 %tmp0, 5
br i1 %tmp1, label %label_internal_2, label %label_internal_3
label_internal_2:
%tmp2 = load i32, i32* %sum
%tmp3 = load i32, i32* %i
%tmp4 = add i32 %tmp2, %tmp3
store i32 %tmp4, i32* %sum
%tmp5 = add i32 %tmp0, 1
store i32 %tmp5, i32* %i
br label %label_internal_1
label_internal_3:
%tmp6 = load i32, i32* %sum
call i32 (ptr, ...) @printf(ptr @llvm.str.2, ptr @llvm.str.0, i32 %tmp6)
store i32 1, i32* %i
store i32 0, i32* %sum
br label %label_internal_4
label_internal_4:
%tmp7 = load i32, i32* %i
%tmp8 = icmp sle i32 %tmp7, 5
br i1 %tmp8, label %label_internal_5, label %label_internal_6
label_internal_5:
%tmp9 = load i32, i32* %sum
%tmp10 = load i32, i32* %i
%tmp11 = add i32 %tmp9, %tmp10
store i32 %tmp11, i32* %sum
%tmp12 = load i32, i32* %i
%tmp13 = add i32 %tmp12, 1
store i32 %tmp13, i32* %i
br label %label_internal_4
label_internal_6:
%tmp14 = load i32, i32* %sum
call i32 (ptr, ...) @printf(ptr @llvm.str.2, ptr @llvm.str.1, i32 %tmp14)
ret i32 0
}

