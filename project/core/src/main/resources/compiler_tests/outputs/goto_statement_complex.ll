; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [6 x i8] c"Start\00"
@llvm.str.1 = private unnamed_addr constant [13 x i8] c"At Label 100\00"
@llvm.str.2 = private unnamed_addr constant [13 x i8] c"At Label 200\00"
@llvm.str.3 = private unnamed_addr constant [13 x i8] c"At Label 300\00"
@llvm.str.4 = private unnamed_addr constant [15 x i8] c"End of program\00"
@llvm.str.5 = private unnamed_addr constant [4 x i8] c"%s\0A\00"

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
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.0)
br label %label_100
label_100:
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.1)
br label %label_300
br label %label_200
label_200:
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.2)
br label %label_400
br label %label_300
label_300:
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.3)
br label %label_200
br label %label_400
label_400:
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.4)
ret i32 0
}

