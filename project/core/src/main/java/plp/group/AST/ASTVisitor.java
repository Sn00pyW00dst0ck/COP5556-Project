package plp.group.AST;

import plp.group.AST.AST.Block;
import plp.group.AST.AST.DeclarationPart.Callable;
import plp.group.AST.AST.DeclarationPart.Constant;
import plp.group.AST.AST.DeclarationPart.Label;
import plp.group.AST.AST.DeclarationPart.Type;
import plp.group.AST.AST.DeclarationPart.UsesUnits;
import plp.group.AST.AST.DeclarationPart.Variable;
import plp.group.AST.AST.Expression.Binary;
import plp.group.AST.AST.Expression.Group;
import plp.group.AST.AST.Expression.Literal.Boolean;
import plp.group.AST.AST.Expression.Literal.Character;
import plp.group.AST.AST.Expression.Literal.Integer;
import plp.group.AST.AST.Expression.Literal.Nil;
import plp.group.AST.AST.Expression.Literal.Real;
import plp.group.AST.AST.Expression.Unary;
import plp.group.AST.AST.Program;
import plp.group.AST.AST.Statement.Assignment;
import plp.group.AST.AST.Statement.Compound;
import plp.group.AST.AST.Statement.For;
import plp.group.AST.AST.Statement.Goto;
import plp.group.AST.AST.Statement.If;
import plp.group.AST.AST.Statement.Labeled;
import plp.group.AST.AST.Statement.Repeat;
import plp.group.AST.AST.Statement.While;
import plp.group.AST.AST.Statement.With;
import plp.group.AST.AST.Type.Class;
import plp.group.AST.AST.Type.Method;
import plp.group.AST.AST.Type.Packed;
import plp.group.AST.AST.Type.Pointer;
import plp.group.AST.AST.Type.Simple.Named;
import plp.group.AST.AST.Type.Simple.Scalar;
import plp.group.AST.AST.Type.Simple.String;
import plp.group.AST.AST.Type.Simple.Subrange;
import plp.group.AST.AST.Type.Unpacked.Array;
import plp.group.AST.AST.Type.Unpacked.File;
import plp.group.AST.AST.Type.Unpacked.Record;
import plp.group.AST.AST.Type.Unpacked.Set;
import plp.group.AST.AST.Variable.Address;
import plp.group.AST.AST.Variable.PostFixVariable;
import plp.group.AST.AST.Variable.Simple;
import plp.group.AST.AST.Variable.PostFix.ArrayAccess;
import plp.group.AST.AST.Variable.PostFix.FieldAccess;
import plp.group.AST.AST.Variable.PostFix.MethodCall;
import plp.group.AST.AST.Variable.PostFix.PointerDereference;

public abstract class ASTVisitor<T> {

	public T visit(AST ast) { return ast.accept(this); }

    public abstract T visitProgram(Program program);

    public abstract T visitBlock(Block block);

    public abstract T visitVariableSimple(Simple variable);

    public abstract T visitVariableAddress(Address variable);

    public abstract T visitVariablePostFixVariable(PostFixVariable variable);

    public abstract T visitVariablePostFixFieldAccess(FieldAccess post);

    public abstract T visitVariablePostFixArrayAccess(ArrayAccess post);
    
    public abstract T visitVariablePostFixMethodCall(MethodCall post);

    public abstract T visitVariablePostFixPointerDereference(PointerDereference post);

    public abstract T visitTypeSimpleScalar(Scalar type);

    public abstract T visitTypeSimpleSubrange(Subrange type);

    public abstract T visitTypeSimpleNamed(Named type);

    public abstract T visitTypeSimpleString(String type);

    public abstract T visitTypePacked(Packed type);

    public abstract T visitTypeUnpackedArray(Array type);

    public abstract T visitTypeUnpackedSet(Set type);

    public abstract T visitTypeUnpackedRecord(Record type);

    public abstract T visitTypeUnpackedRecordField(Record.FieldListElement.Field type);

    public abstract T visitTypeUnpackedRecordVariantPart(Record.FieldListElement.VariantPart type);

    public abstract T visitTypeUnpackedRecordVariantPartTag(Record.FieldListElement.VariantPart.Tag type);

    public abstract T visitTypeUnpackedRecordVariantPartVariant(Record.FieldListElement.VariantPart.Variant type);

    public abstract T visitTypeUnpackedFile(File type);

    public abstract T visitTypePointer(Pointer type);

    public abstract T visitTypeMethod(Method type);

    public abstract T visitTypeClass(Class type);

    public abstract T visitTypeClassField(Class.VisibilitySection.Member.Field type);

    public abstract T visitTypeClassMethod(Class.VisibilitySection.Member.Method type);
    
    public abstract T visitTypeClassConstructor(Class.VisibilitySection.Member.Constructor type);
    
    public abstract T visitTypeClassDestructor(Class.VisibilitySection.Member.Destructor type);

    public abstract T visitDeclarationPartLabel(Label dec);

    public abstract T visitDeclarationPartConstant(Constant dec);

    public abstract T visitDeclarationPartType(Type dec);

    public abstract T visitDeclarationPartVariable(Variable dec);

    public abstract T visitDeclarationPartCallable(Callable dec);

    public abstract T visitDeclarationPartUsesUnits(UsesUnits dec);

    public abstract T visitDeclarationLabel(plp.group.AST.AST.Declaration.Label dec);

    public abstract T visitDeclarationConstant(plp.group.AST.AST.Declaration.Constant dec);

    public abstract T visitDeclarationType(plp.group.AST.AST.Declaration.Type dec);

    public abstract T visitDeclarationVariable(plp.group.AST.AST.Declaration.Variable dec);

    public abstract T visitDeclarationCallable(plp.group.AST.AST.Declaration.Callable dec);

    public abstract T visitDeclarationUsesUnits(plp.group.AST.AST.Declaration.UsesUnits dec);

    public abstract T visitStatementLabeled(Labeled stmt);

    public abstract T visitStatementAssignment(Assignment stmt);

    public abstract T visitStatementGoto(Goto stmt);

    public abstract T visitStatementVariable(plp.group.AST.AST.Statement.Variable stmt);

    public abstract T visitStatementCompound(Compound stmt);

    public abstract T visitStatementIf(If stmt);

    public abstract T visitStatementWhile(While stmt);

    public abstract T visitStatementRepeat(Repeat stmt);

    public abstract T visitStatementFor(For stmt);

    public abstract T visitStatementWith(With stmt);

    public abstract T visitExpressionBinary(Binary expr);

    public abstract T visitExpressionUnary(Unary expr);

    public abstract T visitExpressionGroup(Group expr);

    public abstract T visitExpressionVariable(plp.group.AST.AST.Expression.Variable expr);

    public abstract T visitExpressionLiteralInteger(Integer expr);

    public abstract T visitExpressionLiteralReal(Real expr);

    public abstract T visitExpressionLiteralString(plp.group.AST.AST.Expression.Literal.String expr);

    public abstract T visitExpressionLiteralCharacter(Character expr);

    public abstract T visitExpressionLiteralBoolean(Boolean expr);

    public abstract T visitExpressionLiteralNil(Nil expr);
}
