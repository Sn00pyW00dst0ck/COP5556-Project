; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [11 x i8] c" mod 2 = 0\00"
@llvm.str.1 = private unnamed_addr constant [11 x i8] c" mod 2 = 1\00"
@llvm.str.2 = private unnamed_addr constant [19 x i8] c" is either 17 or 0\00"
@llvm.str.3 = private unnamed_addr constant [6 x i8] c"%d%s\0A\00"

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
%number = alloca i32
store i32 17, i32* %number
%tmp0 = load i32, i32* %number
%tmp1 = srem i32 %tmp0, 2
switch i32 %tmp1, label %label_internal_3[
i32 0, label %label_internal_1
i32 1, label %label_internal_2
]
label_internal_1:
%tmp2 = load i32, i32* %number
call i32 (ptr, ...) @printf(ptr @llvm.str.3, i32 %tmp2, ptr @llvm.str.0)
br label %label_internal_4
label_internal_2:
%tmp3 = load i32, i32* %number
call i32 (ptr, ...) @printf(ptr @llvm.str.3, i32 %tmp3, ptr @llvm.str.1)
br label %label_internal_4
label_internal_3:
br label %label_internal_4
label_internal_4:
%tmp4 = load i32, i32* %number
switch i32 %tmp4, label %label_internal_6[
i32 0, label %label_internal_5
i32 17, label %label_internal_5
]
label_internal_5:
%tmp5 = load i32, i32* %number
call i32 (ptr, ...) @printf(ptr @llvm.str.3, i32 %tmp5, ptr @llvm.str.2)
br label %label_internal_7
label_internal_6:
br label %label_internal_7
label_internal_7:
ret i32 0
}
