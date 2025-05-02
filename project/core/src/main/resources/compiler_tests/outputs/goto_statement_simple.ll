; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [4 x i8] c"%d\0A\00"

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
%num = alloca i32
store i32 10, i32* %num
br label %label_1
label_1:
%tmp0 = load i32, i32* %num
%tmp1 = icmp sgt i32 %tmp0, 0
br i1 %tmp1, label %label_internal_1, label %label_internal_2
label_internal_1:
%tmp2 = load i32, i32* %num
%tmp3 = sub i32 %tmp2, 1
store i32 %tmp3, i32* %num
br label %label_1
br label %label_internal_3
label_internal_2:
%tmp4 = load i32, i32* %num
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp4)
br label %label_internal_3
label_internal_3:
ret i32 0
}

