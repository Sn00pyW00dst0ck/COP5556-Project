package plp.group.AST;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


/**
 * Interface representing the AST, defines all AST nodes.
 */
public sealed interface AST {

    /**
     * Root node of AST, the whole program.
     */
    public record Program(
        java.lang.String name,
        List<AST.Variable.Simple> args,
        AST.Statement.Block block
    ) implements AST {};

    /*
     * Block is an actual new block, not a compound statement.
     */
    public record Block(
        List<AST.DeclarationPart> declarationParts,
        AST.Statement.Compound body
    ) implements AST {};

    /**
     * Variable does a LOT in the AST! This is because it has a postfix part.
     */
    public sealed interface Variable extends AST {
        public record Simple(
            java.lang.String name
        ) implements Variable {};

        public record Address(
            AST.Variable variable
        ) implements Variable {};

        public record PostFixVariable(
            AST.Variable.Simple base,
            List<PostFix> postFixes
        ) implements Variable {};

        public sealed interface PostFix extends Variable {
            public record FieldAccess(
                java.lang.String name
            ) implements PostFix {};

            public record MethodCall(
                List<MethodCall.Argument> args
            ) implements PostFix {
                public record Argument(
                    AST.Expression expr,
                    List<AST.Expression> widths
                ) {};
            };

            public record ArrayAccess(
                List<Expression> indices
            ) implements PostFix {};

            public record PointerDereference() implements PostFix {};
        }
    }

    /**
     * Representation of types within the AST!
     */
    public sealed interface Type extends AST {
        public sealed interface Simple extends Type {
            public record Scalar(
                List<java.lang.String> values
            ) implements Simple {};
    
            public record Subrange(
                AST.Expression lower,
                AST.Expression upper
            ) implements Simple {};
    
            public record Named(
                java.lang.String name
            ) implements Simple {};
    
            public record String(
                AST.Expression maxLength
            ) implements Simple {};
        }

        public record Packed(
            AST.Type.Unpacked type
        ) implements Type {};

        public sealed interface Unpacked extends Type {
            public record Array(
                List<AST.Type.Simple> elementTypes,
                AST.Type componentType
            ) implements Unpacked {};
    
            public record Record(
                List<FieldListElement> fieldsAndVariants
            ) implements Unpacked {
                public sealed interface FieldListElement extends AST {
                    public record Field(
                        List<java.lang.String> names,
                        AST.Type type
                    ) implements FieldListElement {};

                    public record VariantPart(
                        Tag tag,
                        List<Variant> variants
                    ) implements FieldListElement {
                        public record Tag(
                            Optional<java.lang.String> name,
                            AST.Type.Simple.Named typeName
                        ) implements AST {};
    
                        public record Variant(
                            List<AST.Expression> constants,
                            List<FieldListElement> fieldsAndVariants
                        ) implements AST {};
                    };
                }
            };

            public record Set(
                AST.Type.Simple baseType
            ) implements Unpacked {};
    
            public record File(
                Optional<AST.Type> type
            ) implements Unpacked {};
        }

        public record Method(
            List<Method.ParameterGroup> parameters,
            Optional<AST.Type> resultType
        ) implements Type {
            public record ParameterGroup(
                List<java.lang.String> parameters,
                AST.Type parameterType,
                GroupType groupType
            ) {
                public enum GroupType { VALUE, REFERENCE, PROCEDURE, FUNCTION };
            };
        };

        public record Pointer(
            AST.Type.Simple.Named type
        ) implements Type {};

        public record Class(
            // NOTE: class doesn't have a name because this appears in a Type Definition, which stores name to Type
            List<Class.VisibilitySection> sections
        ) implements Type {
            public record VisibilitySection(
                Class.VisibilitySection.Visibility visibility,
                List<Class.VisibilitySection.Member> members
            ) {
                public enum Visibility { PUBLIC, PRIVATE, PROTECTED };

                public sealed interface Member extends AST {
                    public record Field(
                        java.lang.String name,
                        AST.Type type
                    ) implements Member {};

                    /**
                     * Note, the signature does NOT include implicit 'this'...
                     */
                    public record Method(
                        java.lang.String name,
                        AST.Type.Method signature
                    ) implements Member {};
                    
                    /**
                     * Note, the signature of Constructor and Destructor do *NOT* include the implicit 'this'.
                     */
                    public record Constructor(
                        java.lang.String name,
                        AST.Type.Method signature
                    ) implements Member {}
            
                    /**
                     * Note, the signature of Constructor and Destructor do *NOT* include the implicit 'this'.
                     */
                    public record Destructor(
                        java.lang.String name,
                        AST.Type.Method signature
                    ) implements Member {}
                }
            };
        };
    }

    /**
     * All the declaration parts in the AST
     */
    public sealed interface DeclarationPart extends AST {
        record Label(
            List<AST.Declaration.Label> declarations
        ) implements DeclarationPart {};

        record Constant(
            List<AST.Declaration.Constant> declarations
        ) implements DeclarationPart {};

        record Type(
            List<AST.Declaration.Type> declarations
        ) implements DeclarationPart {};

        record Variable(
            List<AST.Declaration.Variable> declarations
        ) implements DeclarationPart {};

        record Callable(
            List<AST.Declaration.Callable> declarations
        ) implements DeclarationPart {};

        record UsesUnits(
            List<AST.Declaration.UsesUnits> declarations
        ) implements DeclarationPart {};
    };

    /**
     * All declarations in the AST.
     */
    public sealed interface Declaration extends AST {
        record Label(
            List<java.lang.String> labels
        ) implements Declaration {};

        record Constant(
            List<AST.Variable.Simple> identifiers,
            List<AST.Expression> values
        ) implements Declaration {};

        record Type(
            String name,
            AST.Type type
        ) implements Declaration {}

        record Variable(
            List<AST.Variable.Simple> variables,
            AST.Type type
        ) implements Declaration {};

        record Callable(
            String name,
            AST.Type.Method method,
            Optional<AST.Block> body
        ) implements Declaration {}

        record UsesUnits(
            List<java.lang.String> unitNames
        ) implements Declaration {};
    }

    /**
     * All statements in the AST
     */
    public sealed interface Statement extends AST {
        public record Compound(
            List<Statement> statements
        ) implements Statement {}

        public record Labeled(
            java.lang.String label,
            AST.Statement statement
        ) implements Statement {};

        public record Assignment(
            AST.Expression.Variable variable,
            AST.Expression value
        ) implements Statement {};

        public record Goto(
            java.lang.String label
        ) implements Statement {};

        // A variable as a statement is something like class.method() which has post fix parts
        public record Variable(
            AST.Variable.PostFixVariable variable
        ) implements Statement {};

        public record If(
            AST.Expression condition,
            AST.Statement thenCase,
            Optional<AST.Statement> elseCase
        ) implements Statement {};

        public record Case(
            AST.Expression expr,
            List<AST.Statement.Case.CaseElement> branches,
            Optional<AST.Statement.Compound> elseBranch
        ) implements Statement {
            public record CaseElement(
                List<AST.Expression> values,
                AST.Statement body
            ) {};
        }

        public record While(
            AST.Expression condition,
            AST.Statement body
        ) implements Statement {};

        public record Repeat(
            AST.Expression condition,
            AST.Statement body
        ) implements Statement {};

        public record For(
            AST.Variable variable,
            AST.Expression initialValue,
            LoopType type,
            AST.Expression finalValue,
            AST.Statement body
        ) implements Statement {
            public enum LoopType { TO, DOWNTO };
        };

        public record With(
            List<AST.Variable> variables,
            AST.Statement statement
        ) implements Statement {};
    };

    /**
     * All expressions in the AST
     */
    public sealed interface Expression extends AST {
        public record Binary(
            java.lang.String operator,
            AST.Expression lhs,
            AST.Expression rhs
        ) implements Expression {};

        public record Unary(
            java.lang.String operator,
            AST.Expression expression
        ) implements Expression {};

        public record Group(
            AST.Expression expression
        ) implements Expression {};

        public record Variable(
            AST.Variable variable
        ) implements Expression {};

        public sealed interface Literal extends Expression {
            public record Integer(
                BigInteger value
            ) implements Literal {};
    
            public record Real(
                BigDecimal value
            ) implements Literal {};
    
            // NOTE: be careful!! Make sure you reference AST.Expression.String or else you get java.lang.String
            public record String(
                java.lang.String value
            ) implements Literal {};
    
            // NOTE: be careful!! Make sure you reference AST.Expression.Character or else you get java.lang.Character
            public record Character(
                java.lang.Character value
            ) implements Literal {};
    
            // NOTE: be careful!! Make sure you reference AST.Expression.Boolean or else you get java.lang.Boolean
            public record Boolean(
                java.lang.Boolean value
            ) implements Literal {};
    
            public record Nil() implements Literal {};
        };
    };

    /**
     * Used by the visitor to determine which function call to do next.
     * @param <T>
     * @param visitor
     * @return
     */
    default <T> T accept(ASTVisitor<T> visitor) {
        return switch (this) {
            case AST.Program program -> visitor.visitProgram(program);
            case AST.Block block -> visitor.visitBlock(block);

            case AST.Variable.Simple variable -> visitor.visitVariableSimple(variable);
            case AST.Variable.Address variable -> visitor.visitVariableAddress(variable);
            case AST.Variable.PostFixVariable variable -> visitor.visitVariablePostFixVariable(variable);
            case AST.Variable.PostFix.FieldAccess post -> visitor.visitVariablePostFixFieldAccess(post);
            case AST.Variable.PostFix.ArrayAccess post -> visitor.visitVariablePostFixArrayAccess(post);
            case AST.Variable.PostFix.MethodCall post -> visitor.visitVariablePostFixMethodCall(post);
            case AST.Variable.PostFix.PointerDereference post -> visitor.visitVariablePostFixPointerDereference(post);

            case AST.Type.Simple.Scalar type -> visitor.visitTypeSimpleScalar(type);
            case AST.Type.Simple.Subrange type -> visitor.visitTypeSimpleSubrange(type);
            case AST.Type.Simple.Named type -> visitor.visitTypeSimpleNamed(type);
            case AST.Type.Simple.String type -> visitor.visitTypeSimpleString(type);
            case AST.Type.Packed type -> visitor.visitTypePacked(type);
            case AST.Type.Unpacked.Array type -> visitor.visitTypeUnpackedArray(type);
            case AST.Type.Unpacked.Set type -> visitor.visitTypeUnpackedSet(type);
            case AST.Type.Unpacked.Record type -> visitor.visitTypeUnpackedRecord(type);
            case AST.Type.Unpacked.Record.FieldListElement.Field type -> visitor.visitTypeUnpackedRecordField(type);
            case AST.Type.Unpacked.Record.FieldListElement.VariantPart type -> visitor.visitTypeUnpackedRecordVariantPart(type);
            case AST.Type.Unpacked.Record.FieldListElement.VariantPart.Tag type -> visitor.visitTypeUnpackedRecordVariantPartTag(type);
            case AST.Type.Unpacked.Record.FieldListElement.VariantPart.Variant type -> visitor.visitTypeUnpackedRecordVariantPartVariant(type);
            case AST.Type.Unpacked.File type -> visitor.visitTypeUnpackedFile(type);
            case AST.Type.Pointer type -> visitor.visitTypePointer(type);
            case AST.Type.Method type -> visitor.visitTypeMethod(type);
            case AST.Type.Class type -> visitor.visitTypeClass(type);
            case AST.Type.Class.VisibilitySection.Member.Field type -> visitor.visitTypeClassField(type);
            case AST.Type.Class.VisibilitySection.Member.Method type -> visitor.visitTypeClassMethod(type);
            case AST.Type.Class.VisibilitySection.Member.Constructor type -> visitor.visitTypeClassConstructor(type);
            case AST.Type.Class.VisibilitySection.Member.Destructor type -> visitor.visitTypeClassDestructor(type);

            case AST.DeclarationPart.Label dec -> visitor.visitDeclarationPartLabel(dec);
            case AST.DeclarationPart.Constant dec -> visitor.visitDeclarationPartConstant(dec);
            case AST.DeclarationPart.Type dec -> visitor.visitDeclarationPartType(dec);
            case AST.DeclarationPart.Variable dec -> visitor.visitDeclarationPartVariable(dec);
            case AST.DeclarationPart.Callable dec -> visitor.visitDeclarationPartCallable(dec);
            case AST.DeclarationPart.UsesUnits dec -> visitor.visitDeclarationPartUsesUnits(dec);

            case AST.Declaration.Label dec -> visitor.visitDeclarationLabel(dec);
            case AST.Declaration.Constant dec -> visitor.visitDeclarationConstant(dec);
            case AST.Declaration.Type dec -> visitor.visitDeclarationType(dec);
            case AST.Declaration.Variable dec -> visitor.visitDeclarationVariable(dec);
            case AST.Declaration.Callable dec -> visitor.visitDeclarationCallable(dec);
            case AST.Declaration.UsesUnits dec -> visitor.visitDeclarationUsesUnits(dec);

            case AST.Statement.Labeled stmt -> visitor.visitStatementLabeled(stmt);
            case AST.Statement.Assignment stmt -> visitor.visitStatementAssignment(stmt);
            case AST.Statement.Goto stmt -> visitor.visitStatementGoto(stmt);
            case AST.Statement.Variable stmt -> visitor.visitStatementVariable(stmt);
            case AST.Statement.Compound stmt -> visitor.visitStatementCompound(stmt);
            case AST.Statement.If stmt -> visitor.visitStatementIf(stmt);
            case AST.Statement.Case stmt -> visitor.visitStatementCase(stmt);
            case AST.Statement.While stmt -> visitor.visitStatementWhile(stmt);
            case AST.Statement.Repeat stmt -> visitor.visitStatementRepeat(stmt);
            case AST.Statement.For stmt -> visitor.visitStatementFor(stmt);
            case AST.Statement.With stmt -> visitor.visitStatementWith(stmt);

            case AST.Expression.Binary expr -> visitor.visitExpressionBinary(expr);
            case AST.Expression.Unary expr -> visitor.visitExpressionUnary(expr);
            case AST.Expression.Group expr -> visitor.visitExpressionGroup(expr);
            case AST.Expression.Variable expr -> visitor.visitExpressionVariable(expr);
            case AST.Expression.Literal.Integer expr -> visitor.visitExpressionLiteralInteger(expr);
            case AST.Expression.Literal.Real expr -> visitor.visitExpressionLiteralReal(expr);
            case AST.Expression.Literal.String expr -> visitor.visitExpressionLiteralString(expr);
            case AST.Expression.Literal.Character expr -> visitor.visitExpressionLiteralCharacter(expr);
            case AST.Expression.Literal.Boolean expr -> visitor.visitExpressionLiteralBoolean(expr);
            case AST.Expression.Literal.Nil expr -> visitor.visitExpressionLiteralNil(expr);

            default -> throw new RuntimeException("Unexpected AST Node Type Encountered During Visit!");
        };
    }
}
