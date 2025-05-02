; === Type Declarations ===

; === String Constants ===
@llvm.str.0 = private unnamed_addr constant [16 x i8] c"While Statement\00"
@llvm.str.1 = private unnamed_addr constant [2 x i8] c" \00"
@llvm.str.2 = private unnamed_addr constant [18 x i8] c"Repeat Statement:\00"
@llvm.str.3 = private unnamed_addr constant [28 x i8] c"For Statement (to variant):\00"
@llvm.str.4 = private unnamed_addr constant [33 x i8] c"For Statement (down to variant):\00"
@llvm.str.5 = private unnamed_addr constant [4 x i8] c"%s\0A\00"
@llvm.str.6 = private unnamed_addr constant [5 x i8] c"%d%s\00"
@llvm.str.7 = private unnamed_addr constant [2 x i8] c"\0A\00"

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
%sqrNum = alloca i32
%i = alloca i32
store i32 1, i32* %num
%tmp0 = load i32, i32* %num
%tmp1 = load i32, i32* %num
%tmp2 = mul i32 %tmp0, %tmp1
store i32 %tmp2, i32* %sqrNum
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.0)
br label %label_internal_1
label_internal_1:
%tmp3 = load i32, i32* %sqrNum
%tmp4 = icmp sle i32 %tmp3, 100
br i1 %tmp4, label %label_internal_2, label %label_internal_3
label_internal_2:
%tmp5 = load i32, i32* %sqrNum
call i32 (ptr, ...) @printf(ptr @llvm.str.6, i32 %tmp5, ptr @llvm.str.1)
%tmp6 = load i32, i32* %num
%tmp7 = add i32 %tmp6, 1
store i32 %tmp7, i32* %num
%tmp8 = load i32, i32* %num
%tmp9 = load i32, i32* %num
%tmp10 = mul i32 %tmp8, %tmp9
store i32 %tmp10, i32* %sqrNum
br label %label_internal_1
label_internal_3:
call i32 (ptr, ...) @printf(ptr @llvm.str.7)
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.2)
store i32 1, i32* %num
%tmp11 = load i32, i32* %num
%tmp12 = load i32, i32* %num
%tmp13 = mul i32 %tmp11, %tmp12
store i32 %tmp13, i32* %sqrNum
br label %label_internal_4
label_internal_4:
%tmp14 = load i32, i32* %sqrNum
call i32 (ptr, ...) @printf(ptr @llvm.str.6, i32 %tmp14, ptr @llvm.str.1)
%tmp15 = load i32, i32* %num
%tmp16 = add i32 %tmp15, 1
store i32 %tmp16, i32* %num
%tmp17 = load i32, i32* %num
%tmp18 = load i32, i32* %num
%tmp19 = mul i32 %tmp17, %tmp18
store i32 %tmp19, i32* %sqrNum
br label %label_internal_5
label_internal_5:
%tmp20 = load i32, i32* %sqrNum
%tmp21 = icmp sgt i32 %tmp20, 100
br i1 %tmp21, label %label_internal_6, label %label_internal_4
label_internal_6:
call i32 (ptr, ...) @printf(ptr @llvm.str.7)
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.3)
store i32 1, i32* %i
br label %label_internal_7
label_internal_7:
%tmp22 = load i32, i32* %i
%tmp23 = icmp sle i32 %tmp22, 10
br i1 %tmp23, label %label_internal_8, label %label_internal_9
label_internal_8:
%tmp24 = load i32, i32* %i
call i32 (ptr, ...) @printf(ptr @llvm.str.6, i32 %tmp24, ptr @llvm.str.1)
%tmp25 = add i32 %tmp22, 1
store i32 %tmp25, i32* %i
br label %label_internal_7
label_internal_9:
call i32 (ptr, ...) @printf(ptr @llvm.str.7)
call i32 (ptr, ...) @printf(ptr @llvm.str.5, ptr @llvm.str.4)
store i32 10, i32* %i
br label %label_internal_10
label_internal_10:
%tmp26 = load i32, i32* %i
%tmp27 = icmp sge i32 %tmp26, 1
br i1 %tmp27, label %label_internal_11, label %label_internal_12
label_internal_11:
%tmp28 = load i32, i32* %i
call i32 (ptr, ...) @printf(ptr @llvm.str.6, i32 %tmp28, ptr @llvm.str.1)
%tmp29 = sub i32 %tmp26, 1
store i32 %tmp29, i32* %i
br label %label_internal_10
label_internal_12:
call i32 (ptr, ...) @printf(ptr @llvm.str.7)
ret i32 0
}
