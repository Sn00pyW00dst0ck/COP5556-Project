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

public interface ASTVisitor<T> {

    T visitProgram(Program program);

    T visitBlock(Block block);

    T visitVariableSimple(Simple variable);

    T visitVariableAddress(Address variable);

    T visitVariablePostFixVariable(PostFixVariable variable);

    T visitTypeSimpleScalar(Scalar type);

    T visitTypeSimpleSubrange(Subrange type);

    T visitTypeSimpleNamed(Named type);

    T visitTypeSimpleString(String type);

    T visitTypePacked(Packed type);

    T visitTypeUnpackedArray(Array type);

    T visitTypeUnpackedSet(Set type);

    T visitTypeUnpackedRecord(Record type);

    T visitTypeUnpackedFile(File type);

    T visitTypePointer(Pointer type);

    T visitTypeMethod(Method type);

    T visitTypeClass(Class type);

    T visitDeclarationPartLabel(Label dec);

    T visitDeclarationPartConstant(Constant dec);

    T visitDeclarationPartType(Type dec);

    T visitDeclarationPartVariable(Variable dec);

    T visitDeclarationPartCallable(Callable dec);

    T visitDeclarationPartUsesUnits(UsesUnits dec);

    T visitDeclarationLabel(plp.group.AST.AST.Declaration.Label dec);

    T visitDeclarationConstant(plp.group.AST.AST.Declaration.Constant dec);

    T visitDeclarationType(plp.group.AST.AST.Declaration.Type dec);

    T visitDeclarationVariable(plp.group.AST.AST.Declaration.Variable dec);

    T visitDeclarationCallable(plp.group.AST.AST.Declaration.Callable dec);

    T visitDeclarationUsesUnits(plp.group.AST.AST.Declaration.UsesUnits dec);

    T visitStatementLabeled(Labeled stmt);

    T visitStatementAssignment(Assignment stmt);

    T visitStatementGoto(Goto stmt);

    T visitStatementVariable(plp.group.AST.AST.Statement.Variable stmt);

    T visitStatementCompound(Compound stmt);

    T visitStatementIf(If stmt);

    T visitStatementWhile(While stmt);

    T visitStatementRepeat(Repeat stmt);

    T visitStatementFor(For stmt);

    T visitStatementWith(With stmt);

    T visitExpressionBinary(Binary expr);

    T visitExpressionUnary(Unary expr);

    T visitExpressionGroup(Group expr);

    T visitExpressionVariable(plp.group.AST.AST.Expression.Variable expr);

    T visitExpressionLiteralInteger(Integer expr);

    T visitExpressionLiteralReal(Real expr);

    T visitExpressionLiteralString(plp.group.AST.AST.Expression.Literal.String expr);

    T visitExpressionLiteralCharacter(Character expr);

    T visitExpressionLiteralBoolean(Boolean expr);

    T visitExpressionLiteralNil(Nil expr);
}
