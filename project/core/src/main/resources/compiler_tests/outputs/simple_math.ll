; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [9 x i8] c"2 + 3 = \00"
@llvm.str.1 = private unnamed_addr constant [9 x i8] c"2 - 3 = \00"
@llvm.str.2 = private unnamed_addr constant [9 x i8] c"2 * 3 = \00"
@llvm.str.3 = private unnamed_addr constant [9 x i8] c"2 / 3 = \00"
@llvm.str.4 = private unnamed_addr constant [3 x i8] c"%s\00"
@llvm.str.5 = private unnamed_addr constant [4 x i8] c"%f\0A\00"

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
%num1 = alloca double
%num2 = alloca double
%result = alloca double
store double 2.0, double* %num1
store double 3.0, double* %num2
call i32 (ptr, ...) @printf(ptr @llvm.str.4, ptr @llvm.str.0)
%tmp0 = load double, double* %num1
%tmp1 = load double, double* %num2
%tmp2 = fadd double %tmp0, %tmp1
store double %tmp2, double* %result
%tmp3 = load double, double* %result
call i32 (ptr, ...) @printf(ptr @llvm.str.5, double %tmp3)
call i32 (ptr, ...) @printf(ptr @llvm.str.4, ptr @llvm.str.1)
%tmp4 = load double, double* %num1
%tmp5 = load double, double* %num2
%tmp6 = fsub double %tmp4, %tmp5
store double %tmp6, double* %result
%tmp7 = load double, double* %result
call i32 (ptr, ...) @printf(ptr @llvm.str.5, double %tmp7)
call i32 (ptr, ...) @printf(ptr @llvm.str.4, ptr @llvm.str.2)
%tmp8 = load double, double* %num1
%tmp9 = load double, double* %num2
%tmp10 = fmul double %tmp8, %tmp9
store double %tmp10, double* %result
%tmp11 = load double, double* %result
call i32 (ptr, ...) @printf(ptr @llvm.str.5, double %tmp11)
call i32 (ptr, ...) @printf(ptr @llvm.str.4, ptr @llvm.str.3)
%tmp12 = load double, double* %num1
%tmp13 = load double, double* %num2
%tmp14 = fdiv double %tmp12, %tmp13
store double %tmp14, double* %result
%tmp15 = load double, double* %result
call i32 (ptr, ...) @printf(ptr @llvm.str.5, double %tmp15)
ret i32 0
}
