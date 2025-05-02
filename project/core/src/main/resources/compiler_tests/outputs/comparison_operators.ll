; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [6 x i8] c"Hello\00"
@llvm.str.1 = private unnamed_addr constant [6 x i8] c"World\00"
@llvm.str.2 = private unnamed_addr constant [4 x i8] c"%d\0A\00"

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
%tmp0 = icmp sgt i32 4, 3
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp0)
%tmp1 = icmp slt i32 4, 3
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp1)
%tmp2 = icmp ne i32 6, 2
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp2)
%tmp3 = icmp sle i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp3)
%tmp4 = icmp eq i32 3, 3
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp4)
%tmp5 = icmp eq i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp5)
%tmp6 = icmp slt i32 3, 4
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp6)
%tmp7 = icmp slt ptr @llvm.str.0, @llvm.str.1
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp7)
%tmp8 = icmp sle ptr @llvm.str.0, @llvm.str.1
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp8)
%tmp9 = icmp ne ptr @llvm.str.0, @llvm.str.1
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp9)
%tmp10 = icmp eq ptr @llvm.str.0, @llvm.str.1
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp10)
%tmp11 = icmp eq ptr @llvm.str.0, @llvm.str.0
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp11)
%tmp12 = icmp slt i1 1, 0
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp12)
%tmp13 = icmp sle i1 0, 1
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp13)
%tmp14 = icmp slt i8 63, 65
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp14)
%tmp15 = icmp eq i8 63, 65
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp15)
%tmp16 = icmp ne i8 63, 65
call i32 (ptr, ...) @printf(ptr @llvm.str.2, i1 %tmp16)
ret i32 0
}

