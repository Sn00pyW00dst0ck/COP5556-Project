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
%tmp0 = mul i32 3, 4
%tmp1 = sub i32 %tmp0, 2
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp1)
%tmp2 = sdiv i32 4, 2
%tmp3 = mul i32 4, %tmp2
%tmp4 = sub i32 0, 5
%tmp5 = sub i32 %tmp3, %tmp4
%tmp6 = add i32 3, %tmp5
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp6)
%tmp7 = xor i1 0, 1
%tmp8 = and i1 %tmp7, 1
%tmp9 = or i1 %tmp8, 0
%tmp10 = or i1 0, %tmp9
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i1 %tmp10)
%tmp11 = add i32 3, 4
%tmp12 = sdiv i32 6, 3
%tmp13 = sdiv i32 4, %tmp12
%tmp14 = mul i32 %tmp11, %tmp13
call i32 (ptr, ...) @printf(ptr @llvm.str.0, i32 %tmp14)
%tmp15 = fsub double 4.5, 5.0
%tmp16 = fmul double %tmp15, 2.5
%tmp17 = fadd double 3.0, %tmp16
%tmp18 = fadd double %tmp17, 5.1
call i32 (ptr, ...) @printf(ptr @llvm.str.1, double %tmp18)
ret i32 0
}
