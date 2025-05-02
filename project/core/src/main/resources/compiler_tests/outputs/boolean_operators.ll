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
%tmp0 = and i1 0, 0
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp0)
%tmp1 = and i1 1, 0
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp1)
%tmp2 = and i1 0, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp2)
%tmp3 = and i1 1, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp3)
%tmp4 = or i1 0, 0
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp4)
%tmp5 = or i1 1, 0
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp5)
%tmp6 = or i1 0, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp6)
%tmp7 = or i1 1, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp7)
%tmp8 = xor i1 1, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp8)
%tmp9 = xor i1 0, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp9)
ret i32 0
}

