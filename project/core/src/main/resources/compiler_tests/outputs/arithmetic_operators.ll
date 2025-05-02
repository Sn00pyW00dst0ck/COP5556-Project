; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [4 x i8] c"%d\0A\00"
@llvm.str.1 = private unnamed_addr constant [4 x i8] c"%f\0A\00"

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
%tmp0 = add i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp0)
%tmp1 = fadd double 6.0, 9.5
call i32 (ptr, ...) @printf(ptr @llvm.str.1, double %tmp1)
%tmp2 = sub i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp2)
%tmp3 = fsub double 3.0, 4.5
call i32 (ptr, ...) @printf(ptr @llvm.str.1, double %tmp3)
%tmp4 = mul i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp4)
%tmp5 = fmul double 3.0, 4.5
call i32 (ptr, ...) @printf(ptr @llvm.str.1, double %tmp5)
%tmp6 = sdiv i32 4, 2
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp6)
%tmp7 = fdiv double 6.0, 1.5
call i32 (ptr, ...) @printf(ptr @llvm.str.1, double %tmp7)
%tmp8 = srem i32 4, 2
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp8)
%tmp9 = sub i32 0, 5
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp9)
ret i32 0
}

