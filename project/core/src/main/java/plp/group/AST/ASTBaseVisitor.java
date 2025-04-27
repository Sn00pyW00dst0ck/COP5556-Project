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
import plp.group.AST.AST.Statement.Case;
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
import plp.group.AST.AST.Variable.PostFix.ArrayAccess;
import plp.group.AST.AST.Variable.PostFix.FieldAccess;
import plp.group.AST.AST.Variable.PostFix.MethodCall;
import plp.group.AST.AST.Variable.PostFix.PointerDereference;
import plp.group.AST.AST.Variable.PostFixVariable;
import plp.group.AST.AST.Variable.Simple;

/**
 * The default visits provided by this class all return null values. 
 * 
 * They call visit on all child AST nodes and return null.
 */
public class ASTBaseVisitor<T> extends ASTVisitor<T> {

    @Override
    public T visitProgram(Program program) {
        for (var arg : program.args()) {
            this.visit(arg);
        }
        this.visit(program.block());
        return null;
    }

    @Override
    public T visitBlock(Block block) {
        for (var part : block.declarationParts()) {
            this.visit(part);
        }
        this.visit(block.body());
        return null;
    }

    //#region Variable

    @Override
    public T visitVariableSimple(Simple variable) {
        return null;
    }

    @Override
    public T visitVariableAddress(Address variable) {
        this.visit(variable.variable());
        return null;
    }

    @Override
    public T visitVariablePostFixVariable(PostFixVariable variable) {
        this.visit(variable.base());
        for (var postFix : variable.postFixes()) {
            this.visit(postFix);
        }
        return null;
    }

    @Override
    public T visitVariablePostFixFieldAccess(FieldAccess post) {
        return null;
    };
    
    @Override
    public T visitVariablePostFixArrayAccess(ArrayAccess post) {
        for (var index : post.indices()) {
            this.visit(index);
        }
        return null;
    }
    
    @Override
    public T visitVariablePostFixMethodCall(MethodCall post) {
        for (var arg : post.args()) {
            this.visit(arg.expr());
            for (var width : arg.widths()) {
                this.visit(width);
            }
        }
        return null;
    }
    
    @Override
    public T visitVariablePostFixPointerDereference(PointerDereference post) {
        return null;
    }

    //#endregion Variable

    //#region Types

    @Override
    public T visitTypeSimpleScalar(Scalar type) {
        return null;
    }

    @Override
    public T visitTypeSimpleSubrange(Subrange type) {
        this.visit(type.lower());
        this.visit(type.upper());
        return null;
    }

    @Override
    public T visitTypeSimpleNamed(Named type) {
        return null;
    }

    @Override
    public T visitTypeSimpleString(String type) {
        this.visit(type.maxLength());
        return null;
    }

    @Override
    public T visitTypePacked(Packed type) {
        this.visit(type.type());
        return null;
    }

    @Override
    public T visitTypeUnpackedArray(Array type) {
        for (var elem : type.elementTypes()) {
            this.visit(elem);
        }
        this.visit(type.componentType());
        return null;
    }

    @Override
    public T visitTypeUnpackedSet(Set type) {
        this.visit(type.baseType());
        return null;
    }

    @Override
    public T visitTypeUnpackedRecord(Record type) {
        for (var elem : type.fieldsAndVariants()) {
            this.visit(elem);
        }
        return null;
    }
    
    @Override
    public T visitTypeUnpackedRecordField(Record.FieldListElement.Field type) {
        this.visit(type.type());
        return null;
    }
    
    @Override
    public T visitTypeUnpackedRecordVariantPart(Record.FieldListElement.VariantPart type) {
        this.visit(type.tag());
        for (var variant : type.variants()) {
            this.visit(variant);
        }
        return null;
    }

    @Override
    public T visitTypeUnpackedRecordVariantPartTag(Record.FieldListElement.VariantPart.Tag type) {
        this.visit(type.typeName());
        return null;
    }

    @Override
    public T visitTypeUnpackedRecordVariantPartVariant(Record.FieldListElement.VariantPart.Variant type) {
        for (var constant : type.constants()) {
            this.visit(constant);
        }
        for (var elem : type.fieldsAndVariants()) {
            this.visit(elem);
        }
        return null;
    }

    @Override
    public T visitTypeUnpackedFile(File type) {
        type.type().ifPresent((baseType) -> this.visit(baseType));
        return null;
    }

    @Override
    public T visitTypePointer(Pointer type) {
        this.visit(type.type());
        return null;
    }

    @Override
    public T visitTypeMethod(Method type) {
        for (var parameterGroup : type.parameters()) {
            this.visit(parameterGroup.parameterType());
        }
        type.resultType().ifPresent((resultType) -> { this.visit(resultType); });
        return null;
    }

    @Override
    public T visitTypeClass(Class type) {
        for (var section : type.sections()) {
            for (var member : section.members()) {
                this.visit(member);
            }
        }
        return null;
    }
    
    @Override
    public T visitTypeClassField(Class.VisibilitySection.Member.Field type) {
        this.visit(type.type());
        return null;
    }
    
    @Override
    public T visitTypeClassMethod(Class.VisibilitySection.Member.Method type) {
        this.visit(type.signature());
        return null;
    }
    
    @Override
    public T visitTypeClassConstructor(Class.VisibilitySection.Member.Constructor type) {
        this.visit(type.signature());
        return null;
    }
    
    @Override
    public T visitTypeClassDestructor(Class.VisibilitySection.Member.Destructor type) {
        this.visit(type.signature());
        return null;
    }

    //#endregion Types

    //#region DeclarationPart

    @Override
    public T visitDeclarationPartLabel(Label dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    @Override
    public T visitDeclarationPartConstant(Constant dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    @Override
    public T visitDeclarationPartType(Type dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    @Override
    public T visitDeclarationPartVariable(Variable dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    @Override
    public T visitDeclarationPartCallable(Callable dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    @Override
    public T visitDeclarationPartUsesUnits(UsesUnits dec) {
        for (var declaration : dec.declarations()) {
            this.visit(declaration);
        }
        return null;
    }

    //#endregion DeclarationPart

    //#region Declaration
    
    @Override
    public T visitDeclarationLabel(plp.group.AST.AST.Declaration.Label dec) {
        return null;
    }

    @Override
    public T visitDeclarationConstant(plp.group.AST.AST.Declaration.Constant dec) {
        for (var ident : dec.identifiers()) {
            this.visit(ident);
        }
        for (var value : dec.values()) {
            this.visit(value);
        }
        return null;
    }

    @Override
    public T visitDeclarationType(plp.group.AST.AST.Declaration.Type dec) {
        this.visit(dec.type());
        return null;
    }

    @Override
    public T visitDeclarationVariable(plp.group.AST.AST.Declaration.Variable dec) {
        for (var variable : dec.variables()) {
            this.visit(variable);
        }
        this.visit(dec.type());
        return null;
    }

    @Override
    public T visitDeclarationCallable(plp.group.AST.AST.Declaration.Callable dec) {
        this.visit(dec.method());
        dec.body().ifPresent((body) -> { this.visit(body); });
        return null;
    }

    @Override
    public T visitDeclarationUsesUnits(plp.group.AST.AST.Declaration.UsesUnits dec) {
        return null;
    }

    //#endregion Declaration

    //#region Statements

    @Override
    public T visitStatementLabeled(Labeled stmt) {
        this.visit(stmt.statement());
        return null;
    }

    @Override
    public T visitStatementAssignment(Assignment stmt) {
        this.visit(stmt.variable());
        this.visit(stmt.value());
        return null;
    }

    @Override
    public T visitStatementGoto(Goto stmt) {
        return null;
    }

    @Override
    public T visitStatementVariable(plp.group.AST.AST.Statement.Variable stmt) {
        this.visit(stmt.variable());
        return null;
    }

    @Override
    public T visitStatementCompound(Compound stmt) {
        for (var statement : stmt.statements()) {
            this.visit(statement);
        }
        return null;
    }

    @Override
    public T visitStatementIf(If stmt) {
        this.visit(stmt.condition());
        this.visit(stmt.thenCase());
        this.visit(stmt.elseCase());
        return null;
    }

    @Override
    public T visitStatementCase(Case stmt) {
        this.visit(stmt.expr());
        for (var branch : stmt.branches()) {
            for (var expr : branch.values()) {
                this.visit(expr);
            }
            this.visit(branch.body());
        }
        stmt.elseBranch().ifPresent((branch) -> this.visit(branch));
        return null;
    }

    @Override
    public T visitStatementWhile(While stmt) {
        this.visit(stmt.condition());
        this.visit(stmt.body());
        return null;
    }

    @Override
    public T visitStatementRepeat(Repeat stmt) {
        this.visit(stmt.body());
        this.visit(stmt.condition());
        return null;
    }

    @Override
    public T visitStatementFor(For stmt) {
        this.visit(stmt.variable());
        this.visit(stmt.initialValue());
        this.visit(stmt.finalValue());
        this.visit(stmt.body());
        return null;
    }

    @Override
    public T visitStatementWith(With stmt) {
        for (AST.Variable  variable : stmt.variables()) {
            this.visit(variable);
        }
        this.visit(stmt.statement());
        return null;
    }

    //#endregion Statements

    //#region Expressions

    @Override
    public T visitExpressionBinary(Binary expr) {
        this.visit(expr.lhs());
        this.visit(expr.rhs());
        return null;
    }

    @Override
    public T visitExpressionUnary(Unary expr) {
        this.visit(expr.expression());
        return null;
    }

    @Override
    public T visitExpressionGroup(Group expr) {
        this.visit(expr.expression());
        return null;
    }

    @Override
    public T visitExpressionVariable(plp.group.AST.AST.Expression.Variable expr) {
        this.visit(expr.variable());
        return null;
    }

    //#endregion Expressions

    //#region Literals

    @Override
    public T visitExpressionLiteralInteger(Integer expr) {
        return null;
    }

    @Override
    public T visitExpressionLiteralReal(Real expr) {
        return null;
    }

    @Override
    public T visitExpressionLiteralString(plp.group.AST.AST.Expression.Literal.String expr) {
        return null;
    }

    @Override
    public T visitExpressionLiteralCharacter(Character expr) {
        return null;
    }

    @Override
    public T visitExpressionLiteralBoolean(Boolean expr) {
        return null;
    }

    @Override
    public T visitExpressionLiteralNil(Nil expr) {
        return null;
    }

    //#endregion Literals
}
