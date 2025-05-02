; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [29 x i8] c"Within the procedure display\00"
@llvm.str.1 = private unnamed_addr constant [14 x i8] c"value of a = \00"
@llvm.str.2 = private unnamed_addr constant [6 x i8] c" b = \00"
@llvm.str.3 = private unnamed_addr constant [10 x i8] c" and c = \00"
@llvm.str.4 = private unnamed_addr constant [39 x i8] c"Within the procedure parameter_display\00"
@llvm.str.5 = private unnamed_addr constant [19 x i8] c"Within the program\00"
@llvm.str.6 = private unnamed_addr constant [4 x i8] c"%s\0A\00"
@llvm.str.7 = private unnamed_addr constant [5 x i8] c"%s%d\00"
@llvm.str.8 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"
@llvm.str.9 = private unnamed_addr constant [4 x i8] c"%s\0A\00"
@llvm.str.10 = private unnamed_addr constant [5 x i8] c"%s%d\00"
@llvm.str.11 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"
@llvm.str.12 = private unnamed_addr constant [4 x i8] c"%s\0A\00"
@llvm.str.13 = private unnamed_addr constant [5 x i8] c"%s%d\00"
@llvm.str.14 = private unnamed_addr constant [6 x i8] c"%s%d\0A\00"

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
define void @parameter_display(i32 %x, i32 %y) {
%a = alloca i32
%b = alloca i32
%c = alloca i32
store i32 %x, i32* %a
store i32 %y, i32* %b
%tmp0 = load i32, i32* %a
%tmp1 = load i32, i32* %b
%tmp2 = add i32 %tmp0, %tmp1
store i32 %tmp2, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.6, ptr @llvm.str.4)
%tmp3 = load i32, i32* %a
call i32 (ptr, ...) @printf(ptr @llvm.str.7, ptr @llvm.str.1, i32 %tmp3)
%tmp4 = load i32, i32* %b
call i32 (ptr, ...) @printf(ptr @llvm.str.7, ptr @llvm.str.2, i32 %tmp4)
%tmp5 = load i32, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.8, ptr @llvm.str.3, i32 %tmp5)
ret void
}
define void @display() {
%a = alloca i32
%b = alloca i32
%c = alloca i32
store i32 10, i32* %a
store i32 20, i32* %b
%tmp6 = load i32, i32* %a
%tmp7 = load i32, i32* %b
%tmp8 = add i32 %tmp6, %tmp7
store i32 %tmp8, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.9, ptr @llvm.str.0)
%tmp9 = load i32, i32* %a
call i32 (ptr, ...) @printf(ptr @llvm.str.10, ptr @llvm.str.1, i32 %tmp9)
%tmp10 = load i32, i32* %b
call i32 (ptr, ...) @printf(ptr @llvm.str.10, ptr @llvm.str.2, i32 %tmp10)
%tmp11 = load i32, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.11, ptr @llvm.str.3, i32 %tmp11)
ret void
}

; === Main Function ===
define i32 @main() {
%a = alloca i32
%b = alloca i32
%c = alloca i32
store i32 100, i32* %a
store i32 200, i32* %b
%tmp12 = load i32, i32* %a
%tmp13 = load i32, i32* %b
%tmp14 = add i32 %tmp12, %tmp13
store i32 %tmp14, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.12, ptr @llvm.str.5)
%tmp15 = load i32, i32* %a
call i32 (ptr, ...) @printf(ptr @llvm.str.13, ptr @llvm.str.1, i32 %tmp15)
%tmp16 = load i32, i32* %b
call i32 (ptr, ...) @printf(ptr @llvm.str.13, ptr @llvm.str.2, i32 %tmp16)
%tmp17 = load i32, i32* %c
call i32 (ptr, ...) @printf(ptr @llvm.str.14, ptr @llvm.str.3, i32 %tmp17)
call void @display()
call void @parameter_display(i32 15, i32 45)
ret i32 0
}

