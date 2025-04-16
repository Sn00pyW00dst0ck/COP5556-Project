package plp.group.AST;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphi;
import plp.group.project.delphiBaseVisitor;

/**
 * Walks over the delphi parse tree and creates an AST.
 */
public class ASTBuilder extends delphiBaseVisitor<Object> {

    @Override
    public AST.Program visitProgram(delphi.ProgramContext ctx) {
        String programName = ctx.programHeading().identifier().getText();
        ArrayList<AST.Variable.Simple> programArguments = new ArrayList<AST.Variable.Simple>();
        if (ctx.programHeading().identifierList() != null) {
            for (delphi.IdentifierContext parameter : ctx.programHeading().identifierList().identifier()) {
                programArguments.add(new AST.Variable.Simple(parameter.getText()));
            }
        }
        return new AST.Program(programName, programArguments, this.visitBlock(ctx.block()));
    }

    @Override
    public AST.Block visitBlock(delphi.BlockContext ctx) {
        AST.Statement.Compound block = this.visitCompoundStatement(ctx.compoundStatement());
        ArrayList<AST.DeclarationPart> declarations = new ArrayList<AST.DeclarationPart>();
        for (delphi.DeclarationPartContext declaration : ctx.declarationPart()) {
            declarations.add(this.visitDeclarationPart(declaration));
        }
        return new AST.Block(declarations, block);
    }

    //#region Declarations

    @Override
    public AST.DeclarationPart visitDeclarationPart(delphi.DeclarationPartContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.LabelDeclarationPartContext labelDeclarationCtx -> this.visitLabelDeclarationPart(labelDeclarationCtx);
            case delphi.ConstantDefinitionPartContext constantDefinitionCtx -> this.visitConstantDefinitionPart(constantDefinitionCtx);
            case delphi.TypeDefinitionPartContext typeDefinitionCtx -> this.visitTypeDefinitionPart(typeDefinitionCtx);
            case delphi.VariableDeclarationPartContext variableDeclarationCtx -> this.visitVariableDeclarationPart(variableDeclarationCtx);
            case delphi.CallableImplementationPartContext callableImpementationCtx -> this.visitCallableImplementationPart(callableImpementationCtx);
            case delphi.UsesUnitsPartContext usesUnitsCtx -> this.visitUsesUnitsPart(usesUnitsCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.DeclarationPart.Label visitLabelDeclarationPart(delphi.LabelDeclarationPartContext ctx) {
        ArrayList<String> labels = new ArrayList<String>();
        for (delphi.LabelContext label : ctx.label()) {
            labels.add(label.getText());
        }
        return new AST.DeclarationPart.Label(List.of(new AST.Declaration.Label(labels)));
    }

    @Override
    public AST.DeclarationPart.Constant visitConstantDefinitionPart(delphi.ConstantDefinitionPartContext ctx) {
        ArrayList<AST.Variable.Simple> identifiers = new ArrayList<AST.Variable.Simple>();
        ArrayList<AST.Expression> values = new ArrayList<AST.Expression>();
        for (delphi.ConstantDefinitionContext definition : ctx.constantDefinition()) {
            identifiers.add(new AST.Variable.Simple(definition.identifier().IDENT().getText()));
            values.add(this.visitConstant(definition.constant()));
        }
        return new AST.DeclarationPart.Constant(List.of(new AST.Declaration.Constant(identifiers, values)));
    }

    @Override
    public AST.DeclarationPart.Type visitTypeDefinitionPart(delphi.TypeDefinitionPartContext ctx) {
        ArrayList<AST.Declaration.Type> typeDefs = new ArrayList<AST.Declaration.Type>();
        for (delphi.TypeDefinitionContext definition : ctx.typeDefinition()) {
            switch (definition.getChild(definition.getChildCount() - 1)) {
                case delphi.Type_Context typeCtx -> typeDefs.add(new AST.Declaration.Type(definition.identifier().IDENT().getText(), this.visitType_(typeCtx)));
                case delphi.ProcedureTypeContext procedureTypeCtx -> typeDefs.add(new AST.Declaration.Type(definition.identifier().IDENT().getText(), this.visitProcedureType(procedureTypeCtx)));
                case delphi.FunctionTypeContext functionTypeCtx -> typeDefs.add(new AST.Declaration.Type(definition.identifier().IDENT().getText(), this.visitFunctionType(functionTypeCtx)));
                case delphi.ClassTypeContext classTypeCtx -> typeDefs.add(new AST.Declaration.Type(definition.identifier().IDENT().getText(), this.visitClassType(classTypeCtx)));
                default -> throw new RuntimeException("");
            }
        }
        return new AST.DeclarationPart.Type(typeDefs);
    }

    @Override
    public AST.DeclarationPart.Variable visitVariableDeclarationPart(delphi.VariableDeclarationPartContext ctx) {
        ArrayList<AST.Declaration.Variable> declarations = new ArrayList<AST.Declaration.Variable>();
        for (delphi.VariableDeclarationContext declaration : ctx.variableDeclaration()) {
            ArrayList<AST.Variable.Simple> variables = new ArrayList<AST.Variable.Simple>();
            for (delphi.IdentifierContext ident : declaration.identifierList().identifier()) {
                variables.add(new AST.Variable.Simple(ident.IDENT().getText()));
            }
            declarations.add(new AST.Declaration.Variable(variables, this.visitType_(declaration.type_())));
        }
        return new AST.DeclarationPart.Variable(declarations);
    }

    @Override
    public AST.DeclarationPart.Callable visitCallableImplementationPart(delphi.CallableImplementationPartContext ctx) {
        ArrayList<AST.Declaration.Callable> callableImplementations = new ArrayList<AST.Declaration.Callable>();
        for (delphi.CallableImplementationContext implementation : ctx.callableImplementation()) {
            callableImplementations.add(this.visitCallableImplementation(implementation));
        }
        return new AST.DeclarationPart.Callable(callableImplementations);
    }

    @Override
    public AST.DeclarationPart.UsesUnits visitUsesUnitsPart(delphi.UsesUnitsPartContext ctx) {
        ArrayList<String> units = new ArrayList<String>();
        for (delphi.IdentifierContext ident : ctx.identifierList().identifier()) {
            units.add(ident.IDENT().getText());
        }
        return new AST.DeclarationPart.UsesUnits(List.of(new AST.Declaration.UsesUnits(units)));
    }

    //#endregion Declarations

    //#region Types

    @Override
    public AST.Type.Method visitProcedureType(delphi.ProcedureTypeContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }
        return new AST.Type.Method(parameterGroups, Optional.empty());
    }

    @Override
    public AST.Type.Method visitFunctionType(delphi.FunctionTypeContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }
        AST.Type.Simple.Named resultType = this.visitTypeIdentifier(ctx.resultType().typeIdentifier());
        return new AST.Type.Method(parameterGroups, Optional.of(resultType));
    }

    @Override
    public AST.Type.Method.ParameterGroup visitFormalParameterSection(delphi.FormalParameterSectionContext ctx) {
        ArrayList<String> parameterNames = new ArrayList<String>();
        for (delphi.IdentifierContext parameter : ctx.parameterGroup().identifierList().identifier()) {
            parameterNames.add(parameter.IDENT().getText());
        }
        AST.Type.Simple.Named parameterType = this.visitTypeIdentifier(ctx.parameterGroup().typeIdentifier());
        AST.Type.Method.ParameterGroup.GroupType groupType = 
            (ctx.VAR() != null) ? AST.Type.Method.ParameterGroup.GroupType.REFERENCE :
            (ctx.PROCEDURE() != null) ? AST.Type.Method.ParameterGroup.GroupType.PROCEDURE :
            (ctx.FUNCTION() != null) ? AST.Type.Method.ParameterGroup.GroupType.FUNCTION :
            AST.Type.Method.ParameterGroup.GroupType.VALUE;
        return new AST.Type.Method.ParameterGroup(parameterNames, parameterType, groupType);
    }

    @Override
    public AST.Type.Class visitClassType(delphi.ClassTypeContext ctx) {
        ArrayList<AST.Type.Class.VisibilitySection> sections = new ArrayList<AST.Type.Class.VisibilitySection>();
        for (delphi.VisibilitySectionContext sectionCtx : ctx.visibilitySection()) {
            AST.Type.Class.VisibilitySection.Visibility visibilityLevel = (
                sectionCtx.PRIVATE() != null ? AST.Type.Class.VisibilitySection.Visibility.PRIVATE :
                sectionCtx.PROTECTED() != null ? AST.Type.Class.VisibilitySection.Visibility.PROTECTED :
                sectionCtx.PUBLIC() != null ? AST.Type.Class.VisibilitySection.Visibility.PUBLIC :
                null
            );

            // Parse all the members in the section...
            ArrayList<AST.Type.Class.VisibilitySection.Member> members = new ArrayList<AST.Type.Class.VisibilitySection.Member>();
            for (delphi.ClassMemberDeclarationContext memberCtx : sectionCtx.classMemberDeclaration()) {
                members.add(switch (memberCtx.getChild(0)) {
                    case delphi.ClassFieldDeclarationContext fieldCtx -> this.visitClassFieldDeclaration(fieldCtx);
                    case delphi.ClassProcedureDeclarationContext procedureCtx -> this.visitClassProcedureDeclaration(procedureCtx);
                    case delphi.ClassFunctionDeclarationContext functionCtx -> this.visitClassFunctionDeclaration(functionCtx);
                    case delphi.ConstructorDeclarationContext constructorCtx -> this.visitConstructorDeclaration(constructorCtx);
                    case delphi.DestructorDeclarationContext destructorCtx -> this.visitDestructorDeclaration(destructorCtx);
                    default -> throw new RuntimeException("");
                });
            }

            sections.add(new AST.Type.Class.VisibilitySection(
                visibilityLevel,
                members
            ));
        }
        return new AST.Type.Class(sections);
    }

    @Override
    public AST.Type.Class.VisibilitySection.Member.Field visitClassFieldDeclaration(delphi.ClassFieldDeclarationContext ctx) {
        return new AST.Type.Class.VisibilitySection.Member.Field(
            ctx.identifier().IDENT().getText(), 
            this.visitType_(ctx.type_())
        );
    }

    @Override
    public AST.Type.Class.VisibilitySection.Member.Method visitClassProcedureDeclaration(delphi.ClassProcedureDeclarationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }

        return new AST.Type.Class.VisibilitySection.Member.Method(
            ctx.identifier().IDENT().getText(),    
            parameterGroups,
            Optional.empty()
        );
    }

    @Override
    public AST.Type.Class.VisibilitySection.Member.Method visitClassFunctionDeclaration(delphi.ClassFunctionDeclarationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }
        AST.Type.Simple.Named resultType = this.visitTypeIdentifier(ctx.resultType().typeIdentifier());

        return new AST.Type.Class.VisibilitySection.Member.Method(
            ctx.identifier().IDENT().getText(),    
            parameterGroups,
            Optional.of(resultType)
        );
    }

    @Override
    public AST.Type.Class.VisibilitySection.Member.Constructor visitConstructorDeclaration(delphi.ConstructorDeclarationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }

        return new AST.Type.Class.VisibilitySection.Member.Constructor(
            ctx.identifier().IDENT().getText(),
            new AST.Type.Method(
                parameterGroups, 
                Optional.of(new AST.Type.Simple.Named("this"))
            )
        );
    }
    
    @Override
    public AST.Type.Class.VisibilitySection.Member.Destructor visitDestructorDeclaration(delphi.DestructorDeclarationContext ctx) {
        return new AST.Type.Class.VisibilitySection.Member.Destructor(
            ctx.identifier().IDENT().getText(),
            new AST.Type.Method(
                List.of(), 
                Optional.empty()
            )
        );
    }

    @Override
    public AST.Type visitType_(delphi.Type_Context ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.SimpleTypeContext simpleTypeCtx -> this.visitSimpleType(simpleTypeCtx);
            case delphi.StructuredTypeContext structuredTypeCtx -> this.visitStructuredType(structuredTypeCtx);
            case delphi.PointerTypeContext pointerTypeCtx -> this.visitPointerType(pointerTypeCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Type.Simple visitSimpleType(delphi.SimpleTypeContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.ScalarTypeContext scalarTypeCtx-> this.visitScalarType(scalarTypeCtx);
            case delphi.SubrangeTypeContext subrangeTypeCtx-> this.visitSubrangeType(subrangeTypeCtx);
            case delphi.TypeIdentifierContext identifierTypeCtx -> this.visitTypeIdentifier(identifierTypeCtx);
            case delphi.StringtypeContext stringTypeCtx -> this.visitStringtype(stringTypeCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Type.Simple.Scalar visitScalarType(delphi.ScalarTypeContext ctx) {
        ArrayList<String> identifiers = new ArrayList<String>();
        for (delphi.IdentifierContext ident : ctx.identifierList().identifier()) {
            identifiers.add(ident.IDENT().getText());
        }
        return new AST.Type.Simple.Scalar(identifiers);
    }

    @Override
    public AST.Type.Simple.Subrange visitSubrangeType(delphi.SubrangeTypeContext ctx) {
        return new AST.Type.Simple.Subrange(this.visitConstant(ctx.constant(0)), this.visitConstant(ctx.constant(1)));
    }

    @Override
    public AST.Type.Simple.Named visitTypeIdentifier(delphi.TypeIdentifierContext ctx) {
        return switch(ctx.getChild(0)) {
            case TerminalNode t when(t.getSymbol().getType() == delphi.CHAR) -> new AST.Type.Simple.Named("Character");
            case TerminalNode t when(t.getSymbol().getType() == delphi.STRING) -> new AST.Type.Simple.Named("String");
            case TerminalNode t when(t.getSymbol().getType() == delphi.BOOLEAN) -> new AST.Type.Simple.Named("Boolean");
            case TerminalNode t when(t.getSymbol().getType() == delphi.INTEGER) -> new AST.Type.Simple.Named("Integer");
            case TerminalNode t when(t.getSymbol().getType() == delphi.REAL) -> new AST.Type.Simple.Named("Real");
            case delphi.IdentifierContext identifier -> new AST.Type.Simple.Named(identifier.IDENT().getText());
            default -> throw new RuntimeException("");
        };
    }
    
    @Override
    public AST.Type.Simple.String visitStringtype(delphi.StringtypeContext ctx) {
        return switch (ctx.getChild(2)) {
            case delphi.IdentifierContext identifier -> new AST.Type.Simple.String(new AST.Expression.Variable(new AST.Variable.Simple(identifier.IDENT().getText())));
            case delphi.UnsignedNumberContext numberCtx -> {
                yield switch (numberCtx.getChild(0)) {
                    case delphi.UnsignedIntegerContext unsignedIntegerCtx -> new AST.Type.Simple.String(this.visitUnsignedInteger(unsignedIntegerCtx));
                    case delphi.UnsignedRealContext unsignedRealCtx -> new AST.Type.Simple.String(this.visitUnsignedReal(unsignedRealCtx));
                    default -> throw new RuntimeException("");
                };
            }
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Type visitStructuredType(delphi.StructuredTypeContext ctx) {
        AST.Type.Unpacked type = this.visitUnpackedStructuredType(ctx.unpackedStructuredType());
        if (ctx.PACKED() != null) {
            return new AST.Type.Packed(type);
        } 
        return type;
    }

    @Override
    public AST.Type.Unpacked visitUnpackedStructuredType(delphi.UnpackedStructuredTypeContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.ArrayTypeContext arrayTypeCtx -> this.visitArrayType(arrayTypeCtx);
            case delphi.SetTypeContext setTypeCtx -> this.visitSetType(setTypeCtx);
            case delphi.RecordTypeContext recordTypeCtx -> this.visitRecordType(recordTypeCtx);
            case delphi.FileTypeContext fileTypeCtx -> this.visitFileType(fileTypeCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Type.Unpacked.Array visitArrayType(delphi.ArrayTypeContext ctx) {
        ArrayList<AST.Type.Simple> elementTypes = new ArrayList<AST.Type.Simple>();
        for (delphi.IndexTypeContext indexType : ctx.typeList().indexType()) {
            elementTypes.add(this.visitSimpleType(indexType.simpleType()));
        }
        AST.Type componentType = this.visitType_(ctx.componentType().type_());
        return new AST.Type.Unpacked.Array(elementTypes, componentType);
    }

    @Override
    public AST.Type.Unpacked.Record visitRecordType(delphi.RecordTypeContext ctx) {
        List<AST.Type.Unpacked.Record.FieldListElement> elements = ctx.fieldList() != null ? this.visitFieldList(ctx.fieldList()) : List.of();
        return new AST.Type.Unpacked.Record(elements);
    }

    // I normally don't return List<> from visitors, but here its unavoidable..
    @Override
    public List<AST.Type.Unpacked.Record.FieldListElement> visitFieldList(delphi.FieldListContext ctx) {
        ArrayList<AST.Type.Unpacked.Record.FieldListElement> fieldList = new ArrayList<AST.Type.Unpacked.Record.FieldListElement>();
        
        // Handle the fixed part
        if (ctx.fixedPart() != null) {
            for (delphi.RecordSectionContext recordSection : ctx.fixedPart().recordSection()) {
                fieldList.add(this.visitRecordSection(recordSection));
            }
        }

        // Handle the variant part
        if (ctx.variantPart() != null) {
            fieldList.add(this.visitVariantPart(ctx.variantPart()));
        }

        return fieldList;
    }

    @Override
    public AST.Type.Unpacked.Record.FieldListElement.Field visitRecordSection(delphi.RecordSectionContext ctx) {
        ArrayList<String> fieldNames = new ArrayList<String>();
        for (delphi.IdentifierContext identifier : ctx.identifierList().identifier()) {
            fieldNames.add(identifier.IDENT().getText());
        }

        return new AST.Type.Unpacked.Record.FieldListElement.Field(
            fieldNames,
            this.visitType_(ctx.type_())
        );
    }

    @Override
    public AST.Type.Unpacked.Record.FieldListElement.VariantPart visitVariantPart(delphi.VariantPartContext ctx) {
        AST.Type.Unpacked.Record.FieldListElement.VariantPart.Tag tag = new AST.Type.Unpacked.Record.FieldListElement.VariantPart.Tag(
            Optional.ofNullable(
                // If identifier is there, get it, otherwise null will make this Optional.empty()
                ctx.tag().identifier() != null ? ctx.tag().identifier().IDENT().getText() : null
            ),
            this.visitTypeIdentifier(ctx.tag().typeIdentifier())
        );

        ArrayList<AST.Type.Unpacked.Record.FieldListElement.VariantPart.Variant> variants = new ArrayList<AST.Type.Unpacked.Record.FieldListElement.VariantPart.Variant>();
        for (delphi.VariantContext variantCtx : ctx.variant()) {
            ArrayList<AST.Expression> constants = new ArrayList<AST.Expression>();
            for (delphi.ConstantContext constant : variantCtx.constList().constant()) {
                constants.add(this.visitConstant(constant));
            }

            variants.add(new AST.Type.Unpacked.Record.FieldListElement.VariantPart.Variant(
                constants, 
                this.visitFieldList(variantCtx.fieldList())
            ));
        }

        return new AST.Type.Unpacked.Record.FieldListElement.VariantPart(
            tag,
            variants
        );
    }

    @Override
    public AST.Type.Unpacked.Set visitSetType(delphi.SetTypeContext ctx) {
        return new AST.Type.Unpacked.Set(this.visitSimpleType(ctx.baseType().simpleType()));
    }

    @Override
    public AST.Type.Unpacked.File visitFileType(delphi.FileTypeContext ctx) {
        Optional<AST.Type> fileType = Optional.empty();
        if (ctx.type_() != null) {
            fileType = Optional.of(this.visitType_(ctx.type_()));
        }
        return new AST.Type.Unpacked.File(fileType);
    }

    @Override
    public AST.Type.Pointer visitPointerType(delphi.PointerTypeContext ctx) {
        return new AST.Type.Pointer(this.visitTypeIdentifier(ctx.typeIdentifier()));
    }

    //#endregion Types
    
    //#region Implementations

    @Override
    public AST.Declaration.Callable visitCallableImplementation(delphi.CallableImplementationContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.ProcedureImplementationContext procedureImplementationCtx -> this.visitProcedureImplementation(procedureImplementationCtx);
            case delphi.FunctionImplementationContext functionImplementationCtx -> this.visitFunctionImplementation(functionImplementationCtx);
            case delphi.ConstructorImplementationContext constructorImplementationCtx -> this.visitConstructorImplementation(constructorImplementationCtx);
            case delphi.DestructorImplementationContext destructorImplementationCtx -> this.visitDestructorImplementation(destructorImplementationCtx);
            case delphi.ClassProcedureImplementationContext classProcedureImplementationCtx -> this.visitClassProcedureImplementation(classProcedureImplementationCtx);
            case delphi.ClassFunctionImplementationContext classFunctionImplementationCtx -> this.visitClassFunctionImplementation(classFunctionImplementationCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Declaration.Callable visitProcedureImplementation(delphi.ProcedureImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }

        return new AST.Declaration.Callable(
            ctx.identifier().IDENT().getText(), 
            new AST.Type.Method(parameterGroups, Optional.empty()),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    @Override
    public AST.Declaration.Callable visitFunctionImplementation(delphi.FunctionImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }
        AST.Type.Simple.Named resultType = this.visitTypeIdentifier(ctx.resultType().typeIdentifier());

        return new AST.Declaration.Callable(
            ctx.identifier().IDENT().getText(), 
            new AST.Type.Method(parameterGroups, Optional.of(resultType)),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    @Override
    public AST.Declaration.Callable visitConstructorImplementation(delphi.ConstructorImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }    
        }
        AST.Type.Simple.Named resultType = new AST.Type.Simple.Named(ctx.identifier(0).IDENT().getText());

        return new AST.Declaration.Callable(
            ctx.identifier(0).IDENT().getText() + "." + ctx.identifier(1).IDENT().getText(),
            new AST.Type.Method(parameterGroups, Optional.of(resultType)),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    @Override
    public AST.Declaration.Callable visitDestructorImplementation(delphi.DestructorImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();

        return new AST.Declaration.Callable(
            ctx.identifier(0).IDENT().getText() + "." + ctx.identifier(1).IDENT().getText(),
            new AST.Type.Method(parameterGroups, Optional.empty()),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    @Override
    public AST.Declaration.Callable visitClassProcedureImplementation(delphi.ClassProcedureImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }

        return new AST.Declaration.Callable(
            ctx.identifier(0).IDENT().getText() + "." + ctx.identifier(1).IDENT().getText(),
            new AST.Type.Method(parameterGroups, Optional.empty()),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    @Override
    public AST.Declaration.Callable visitClassFunctionImplementation(delphi.ClassFunctionImplementationContext ctx) {
        ArrayList<AST.Type.Method.ParameterGroup> parameterGroups = new ArrayList<AST.Type.Method.ParameterGroup>();
        if (ctx.formalParameterList() != null) {
            for (delphi.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    parameterGroups.add(this.visitFormalParameterSection(section));
                }
            }
        }
        AST.Type.Simple.Named resultType = this.visitTypeIdentifier(ctx.resultType().typeIdentifier());

        return new AST.Declaration.Callable(
            ctx.identifier(0).IDENT().getText() + "." + ctx.identifier(1).IDENT().getText(),
            new AST.Type.Method(parameterGroups, Optional.of(resultType)),
            Optional.of(this.visitBlock(ctx.block()))
        );
    }

    //#endregion Implementations

    //#region Statements

    @Override
    public AST.Statement visitStatement(delphi.StatementContext ctx) {
        AST.Statement stmt = (AST.Statement) this.visit(ctx.unlabelledStatement());
        if (ctx.label() != null) {
            stmt = new AST.Statement.Labeled(ctx.label().getText(), stmt);
        }
        return stmt;
    }

    @Override
    public AST.Statement visitSimpleStatement(delphi.SimpleStatementContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.AssignmentStatementContext statementCtx -> this.visitAssignmentStatement(statementCtx);
            case delphi.GotoStatementContext statementCtx -> this.visitGotoStatement(statementCtx);
            case delphi.ProcedureStatementContext statementCtx -> this.visitProcedureStatement(statementCtx);
            // encountering a variable in this context is special
            case delphi.VariableContext variableCtx -> new AST.Statement.Variable((AST.Variable.PostFixVariable) this.visitVariable(variableCtx));
            case delphi.EmptyStatement_Context _ -> null;
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Statement.Assignment visitAssignmentStatement(delphi.AssignmentStatementContext ctx) {
        return new AST.Statement.Assignment(new AST.Expression.Variable(this.visitVariable(ctx.variable())), this.visitExpression(ctx.expression()));
    }

    @Override
    public AST.Statement.Goto visitGotoStatement(delphi.GotoStatementContext ctx) {
        return new AST.Statement.Goto(ctx.label().getText());
    }

    @Override
    public AST.Statement.Variable visitProcedureStatement(delphi.ProcedureStatementContext ctx) {
        AST.Variable.Simple base = new AST.Variable.Simple(ctx.identifier().IDENT().getText());

        // Turn parameters into arguments...
        ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument> parameters = new ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument>();
        for (delphi.ActualParameterContext parameter : ctx.parameterList().actualParameter()) {
            ArrayList<AST.Expression> widths = new ArrayList<AST.Expression>();
            for (delphi.ParameterwidthContext widthCtx : parameter.parameterwidth()) {
                widths.add(this.visitExpression(widthCtx.expression()));
            }

            parameters.add(new AST.Variable.PostFixVariable.PostFix.MethodCall.Argument(
                this.visitExpression(parameter.expression()),
                widths
            ));
        }
        
        return new AST.Statement.Variable(
            new AST.Variable.PostFixVariable(base, List.of(new AST.Variable.PostFixVariable.PostFix.MethodCall(parameters)))
        );
    }

    @Override
    public AST.Statement.Compound visitCompoundStatement(delphi.CompoundStatementContext ctx) {
        ArrayList<AST.Statement> statements = new ArrayList<AST.Statement>();
        for (delphi.StatementContext statement : ctx.statements().statement()) {
            AST.Statement stmt = this.visitStatement(statement);
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return new AST.Statement.Compound(statements);
    }

    @Override
    public AST.Statement.If visitIfStatement(delphi.IfStatementContext ctx) {
        AST.Expression condition = this.visitExpression(ctx.expression());
        AST.Statement thenBlock = this.visitStatement(ctx.statement(0));
        AST.Statement elseBlock = null;
        if (ctx.statement(1) != null) {
            elseBlock = this.visitStatement(ctx.statement(1));
        }
        return new AST.Statement.If(condition, thenBlock, elseBlock);
    }

    @Override
    public AST.Statement.Case visitCaseStatement(delphi.CaseStatementContext ctx) {
        AST.Expression condition = this.visitExpression(ctx.expression());

        // Parse each case
        ArrayList<AST.Statement.Case.CaseElement> cases = new ArrayList<AST.Statement.Case.CaseElement>();
        for (delphi.CaseListElementContext caseListElement : ctx.caseListElement()) {
            ArrayList<AST.Expression> values = new ArrayList<AST.Expression>();
            for (delphi.ConstantContext constant : caseListElement.constList().constant()) {
                values.add(this.visitConstant(constant));
            }
            cases.add(new AST.Statement.Case.CaseElement(values, this.visitStatement(caseListElement.statement())));
        }

        // Parse the else block statements
        ArrayList<AST.Statement> elseBranchStatements = new ArrayList<AST.Statement>();
        if (ctx.statements() != null) {
            for (delphi.StatementContext statement : ctx.statements().statement()) {
                elseBranchStatements.add(this.visitStatement(statement));
            }
        }

        return new AST.Statement.Case(condition, null, Optional.of(new AST.Statement.Compound(elseBranchStatements)));
    }

    @Override
    public AST.Statement.While visitWhileStatement(delphi.WhileStatementContext ctx) {
        return new AST.Statement.While(this.visitExpression(ctx.expression()), this.visitStatement(ctx.statement()));
    }

    @Override
    public AST.Statement.Repeat visitRepeatStatement(delphi.RepeatStatementContext ctx) {
        ArrayList<AST.Statement> statements = new ArrayList<AST.Statement>();
        for (delphi.StatementContext statement : ctx.statements().statement()) {
            AST.Statement stmt = this.visitStatement(statement);
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return new AST.Statement.Repeat(this.visitExpression(ctx.expression()), new AST.Statement.Compound(statements));
    }

    @Override
    public AST.Statement.For visitForStatement(delphi.ForStatementContext ctx) {
        AST.Variable variable = new AST.Variable.Simple(ctx.identifier().getText());
        AST.Expression initialValue = this.visitExpression(ctx.forList().initialValue().expression());
        AST.Statement.For.LoopType type = (ctx.forList().TO() != null) ? AST.Statement.For.LoopType.TO : AST.Statement.For.LoopType.DOWNTO;
        AST.Expression finalValue = this.visitExpression(ctx.forList().initialValue().expression());
        AST.Statement body = this.visitStatement(ctx.statement());
        return new AST.Statement.For(variable, initialValue, type, finalValue, body);
    }

    @Override
    public AST.Statement.With visitWithStatement(delphi.WithStatementContext ctx) {
        ArrayList<AST.Variable> variables = new ArrayList<AST.Variable>();
        for (delphi.VariableContext variable : ctx.recordVariableList().variable()) {
            variables.add(this.visitVariable(variable));
        }
        return new AST.Statement.With(variables, this.visitStatement(ctx.statement()));
    }

    //#endregion Statements

    //#region Expressions

    @Override
    public AST.Expression visitExpression(delphi.ExpressionContext ctx) {
        AST.Expression lhs = this.visitSimpleExpression(ctx.simpleExpression());
        if (ctx.relationaloperator() == null) { return lhs; }
        return new AST.Expression.Binary(ctx.relationaloperator().getChild(0).getText(), lhs, this.visitExpression(ctx.expression())); 
    }

    @Override
    public AST.Expression visitSimpleExpression(delphi.SimpleExpressionContext ctx) {
        AST.Expression lhs = this.visitTerm(ctx.term());
        if (ctx.additiveoperator() == null) { return lhs; }
        return new AST.Expression.Binary(ctx.additiveoperator().getChild(0).getText(), lhs, this.visitSimpleExpression(ctx.simpleExpression())); 
    }

    @Override
    public AST.Expression visitTerm(delphi.TermContext ctx) {
        AST.Expression lhs = this.visitSignedFactor(ctx.signedFactor());
        if (ctx.multiplicativeoperator() == null) { return lhs; }
        return new AST.Expression.Binary(ctx.multiplicativeoperator().getChild(0).getText(), lhs, this.visitTerm(ctx.term())); 
    }

    @Override
    public AST.Expression visitSignedFactor(delphi.SignedFactorContext ctx) {
        return switch (ctx.getChild(0)) {
            case TerminalNode t when t.getSymbol().getType() == delphi.PLUS -> visitFactor(ctx.factor());
            case TerminalNode t when t.getSymbol().getType() == delphi.MINUS -> new AST.Expression.Unary(ctx.getChild(0).getText(), this.visitFactor(ctx.factor()));
            case delphi.FactorContext factorCtx -> visitFactor(factorCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Expression visitFactor(delphi.FactorContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.VariableContext variableCtx -> new AST.Expression.Variable(this.visitVariable(variableCtx));
            case TerminalNode t when t.getSymbol().getType() == delphi.LPAREN -> new AST.Expression.Group(this.visitExpression(ctx.expression()));
            case delphi.FunctionDesignatorContext functionCtx -> this.visitFunctionDesignator(functionCtx);
            case delphi.UnsignedConstantContext unsignedCtx -> (AST.Expression) this.visit(unsignedCtx); // TODO: NIL??
            case TerminalNode t when t.getSymbol().getType() == delphi.NOT -> new AST.Expression.Unary(ctx.getChild(0).getText(), this.visitFactor(ctx.factor()));
            case delphi.Bool_Context boolCtx -> this.visitBool_(boolCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Variable visitVariable(delphi.VariableContext ctx) {
        AST.Variable.Simple base = new AST.Variable.Simple(ctx.identifier().IDENT().getText());
        AST.Variable result = base;

        if (ctx.postFixPart() != null) {
            ArrayList<AST.Variable.PostFixVariable.PostFix> postFixes = new ArrayList<AST.Variable.PostFixVariable.PostFix>();
            for (delphi.PostFixPartContext postFixPartCtx : ctx.postFixPart()) {
                postFixes.add(switch (postFixPartCtx.getChild(0)) {
                    case TerminalNode t when t.getSymbol().getType() == delphi.DOT -> {
                        yield switch (postFixPartCtx.getChild(1)) {
                            case delphi.IdentifierContext identCtx -> new AST.Variable.PostFixVariable.PostFix.FieldAccess(identCtx.IDENT().getText());
                            case delphi.FunctionDesignatorContext functionDesignatorContext -> {
                                // Add the method name
                                postFixes.add(new AST.Variable.PostFixVariable.PostFix.FieldAccess(functionDesignatorContext.identifier().IDENT().getText()));

                                // Turn parameters into arguments...
                                ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument> parameters = new ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument>();
                                for (delphi.ActualParameterContext parameter : functionDesignatorContext.parameterList().actualParameter()) {
                                    ArrayList<AST.Expression> widths = new ArrayList<AST.Expression>();
                                    for (delphi.ParameterwidthContext widthCtx : parameter.parameterwidth()) {
                                        widths.add(this.visitExpression(widthCtx.expression()));
                                    }
                                
                                    parameters.add(new AST.Variable.PostFixVariable.PostFix.MethodCall.Argument(
                                        this.visitExpression(parameter.expression()),
                                        widths
                                    ));
                                }

                                yield new AST.Variable.PostFixVariable.PostFix.MethodCall(parameters); 
                            }
                            default -> throw new RuntimeException("");
                        };
                    }
                    case TerminalNode t when t.getSymbol().getType() == delphi.LBRACK -> {
                        ArrayList<AST.Expression> indices = new ArrayList<AST.Expression>();
                        for (delphi.ExpressionContext expression : postFixPartCtx.expression()) {
                            indices.add(this.visitExpression(expression));
                        }
                        yield new AST.Variable.PostFixVariable.PostFix.ArrayAccess(indices);
                    }
                    case TerminalNode t when t.getSymbol().getType() == delphi.LBRACK2 -> {
                        ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument> parameters = new ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument>();
                        for (delphi.ExpressionContext expression : postFixPartCtx.expression()) {
                            parameters.add(new AST.Variable.PostFixVariable.PostFix.MethodCall.Argument(
                                this.visitExpression(expression),
                                List.of()
                            ));
                        }
                        yield new AST.Variable.PostFixVariable.PostFix.MethodCall(parameters);
                    }
                    case TerminalNode t when t.getSymbol().getType() == delphi.POINTER -> new AST.Variable.PostFixVariable.PostFix.PointerDereference();
                    default -> throw new RuntimeException("");
                });
            }
            result = new AST.Variable.PostFixVariable(base, postFixes);
        }

        if (ctx.AT() != null) {
            result = new AST.Variable.Address(result);
        }

        return result;
    }

    @Override
    public AST.Expression.Variable visitFunctionDesignator(delphi.FunctionDesignatorContext ctx) {
        AST.Variable.Simple base = new AST.Variable.Simple(ctx.identifier().IDENT().getText());
        
        // Turn parameters into arguments...
        ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument> parameters = new ArrayList<AST.Variable.PostFixVariable.PostFix.MethodCall.Argument>();
        for (delphi.ActualParameterContext parameter : ctx.parameterList().actualParameter()) {
            ArrayList<AST.Expression> widths = new ArrayList<AST.Expression>();
            for (delphi.ParameterwidthContext widthCtx : parameter.parameterwidth()) {
                widths.add(this.visitExpression(widthCtx.expression()));
            }

            parameters.add(new AST.Variable.PostFixVariable.PostFix.MethodCall.Argument(
                this.visitExpression(parameter.expression()),
                widths
            ));
        }

        // Return the Variable Expression
        return new AST.Expression.Variable(
            new AST.Variable.PostFixVariable(base, List.of(new AST.Variable.PostFixVariable.PostFix.MethodCall(parameters)))
        );
    }

    //#endregion Expressions

    //#region Literals / Constants

    @Override
    public AST.Expression visitConstant(delphi.ConstantContext ctx) {
        return switch (ctx.getChild(0)) {
            case delphi.SignContext signCtx -> {
                AST.Expression value = switch (ctx.getChild(1)) {
                    case delphi.IdentifierContext identifierCtx -> new AST.Expression.Variable(new AST.Variable.Simple(identifierCtx.IDENT().getText()));
                    case delphi.UnsignedIntegerContext unsignedIntegerCtx -> this.visitUnsignedInteger(unsignedIntegerCtx);
                    case delphi.UnsignedRealContext unsignedRealCtx -> this.visitUnsignedReal(unsignedRealCtx);
                    default -> throw new RuntimeException("");
                };
                yield new AST.Expression.Unary(signCtx.getChild(0).getText(), value);
            }
            case delphi.UnsignedNumberContext numberCtx -> {
                yield switch (numberCtx.getChild(0)) {
                    case delphi.UnsignedIntegerContext unsignedIntegerCtx -> this.visitUnsignedInteger(unsignedIntegerCtx);
                    case delphi.UnsignedRealContext unsignedRealCtx -> this.visitUnsignedReal(unsignedRealCtx);
                    default -> throw new RuntimeException("");
                };
            }
            case delphi.IdentifierContext identifierCtx -> new AST.Expression.Variable(new AST.Variable.Simple(identifierCtx.IDENT().getText()));
            case delphi.StringContext stringCtx -> this.visitString(stringCtx);
            case delphi.ConstantChrContext charCtx -> this.visitConstantChr(charCtx);
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public AST.Expression.Literal.Integer visitUnsignedInteger(delphi.UnsignedIntegerContext ctx) {
        return new AST.Expression.Literal.Integer(new BigInteger(ctx.NUM_INT().toString()));
    }

    @Override
    public AST.Expression.Literal.Real visitUnsignedReal(delphi.UnsignedRealContext ctx) {
        return new AST.Expression.Literal.Real(new BigDecimal(ctx.NUM_REAL().toString()));
    }

    @Override
    public AST.Expression.Literal.String visitString(delphi.StringContext ctx) {
        String literal = ctx.STRING_LITERAL().getText();
        return new AST.Expression.Literal.String(literal.substring(1, literal.length() - 1));
    }

    @Override
    public AST.Expression.Literal.Character visitConstantChr(delphi.ConstantChrContext ctx) {
        int charCode = Integer.parseInt(ctx.unsignedInteger().NUM_INT().getText());
        return new AST.Expression.Literal.Character(Character.valueOf((char) charCode));
    }

    @Override
    public AST.Expression.Literal.Boolean visitBool_(delphi.Bool_Context ctx) {
        return new AST.Expression.Literal.Boolean(Boolean.valueOf(ctx.getText().toLowerCase()));
    }

    //#endregion Literals / Constants

}
