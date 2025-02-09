package plp.group;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import plp.group.project.delphiParser.*;
import plp.group.project.delphiVisitor;

public class Interpreter implements delphiVisitor<String> {

    @Override
    public String visit(ParseTree tree) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visitChildren(RuleNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChildren'");
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTerminal'");
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitErrorNode'");
    }

    @Override
    public String visitProgram(ProgramContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProgram'");
    }

    @Override
    public String visitProgramHeading(ProgramHeadingContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProgramHeading'");
    }

    @Override
    public String visitIdentifier(IdentifierContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIdentifier'");
    }

    @Override
    public String visitBlock(BlockContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlock'");
    }

    @Override
    public String visitUsesUnitsPart(UsesUnitsPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUsesUnitsPart'");
    }

    @Override
    public String visitLabelDeclarationPart(LabelDeclarationPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLabelDeclarationPart'");
    }

    @Override
    public String visitLabel(LabelContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLabel'");
    }

    @Override
    public String visitConstantDefinitionPart(ConstantDefinitionPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstantDefinitionPart'");
    }

    @Override
    public String visitConstantDefinition(ConstantDefinitionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstantDefinition'");
    }

    @Override
    public String visitConstantChr(ConstantChrContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstantChr'");
    }

    @Override
    public String visitConstant(ConstantContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstant'");
    }

    @Override
    public String visitUnsignedNumber(UnsignedNumberContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnsignedNumber'");
    }

    @Override
    public String visitUnsignedInteger(UnsignedIntegerContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnsignedInteger'");
    }

    @Override
    public String visitUnsignedReal(UnsignedRealContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnsignedReal'");
    }

    @Override
    public String visitSign(SignContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSign'");
    }

    @Override
    public String visitBool_(Bool_Context ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBool_'");
    }

    @Override
    public String visitString(StringContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitString'");
    }

    @Override
    public String visitTypeDefinitionPart(TypeDefinitionPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTypeDefinitionPart'");
    }

    @Override
    public String visitTypeDefinition(TypeDefinitionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTypeDefinition'");
    }

    @Override
    public String visitFunctionType(FunctionTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionType'");
    }

    @Override
    public String visitProcedureType(ProcedureTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProcedureType'");
    }

    @Override
    public String visitType_(Type_Context ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitType_'");
    }

    @Override
    public String visitSimpleType(SimpleTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSimpleType'");
    }

    @Override
    public String visitScalarType(ScalarTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitScalarType'");
    }

    @Override
    public String visitSubrangeType(SubrangeTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSubrangeType'");
    }

    @Override
    public String visitTypeIdentifier(TypeIdentifierContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTypeIdentifier'");
    }

    @Override
    public String visitStructuredType(StructuredTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStructuredType'");
    }

    @Override
    public String visitUnpackedStructuredType(UnpackedStructuredTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnpackedStructuredType'");
    }

    @Override
    public String visitStringtype(StringtypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStringtype'");
    }

    @Override
    public String visitArrayType(ArrayTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitArrayType'");
    }

    @Override
    public String visitTypeList(TypeListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTypeList'");
    }

    @Override
    public String visitIndexType(IndexTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIndexType'");
    }

    @Override
    public String visitComponentType(ComponentTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitComponentType'");
    }

    @Override
    public String visitRecordType(RecordTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRecordType'");
    }

    @Override
    public String visitFieldList(FieldListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFieldList'");
    }

    @Override
    public String visitFixedPart(FixedPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFixedPart'");
    }

    @Override
    public String visitRecordSection(RecordSectionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRecordSection'");
    }

    @Override
    public String visitVariantPart(VariantPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariantPart'");
    }

    @Override
    public String visitTag(TagContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTag'");
    }

    @Override
    public String visitVariant(VariantContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariant'");
    }

    @Override
    public String visitSetType(SetTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSetType'");
    }

    @Override
    public String visitBaseType(BaseTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBaseType'");
    }

    @Override
    public String visitFileType(FileTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFileType'");
    }

    @Override
    public String visitPointerType(PointerTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPointerType'");
    }

    @Override
    public String visitVariableDeclarationPart(VariableDeclarationPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariableDeclarationPart'");
    }

    @Override
    public String visitVariableDeclaration(VariableDeclarationContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariableDeclaration'");
    }

    @Override
    public String visitProcedureAndFunctionDeclarationPart(ProcedureAndFunctionDeclarationPartContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProcedureAndFunctionDeclarationPart'");
    }

    @Override
    public String visitProcedureOrFunctionDeclaration(ProcedureOrFunctionDeclarationContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProcedureOrFunctionDeclaration'");
    }

    @Override
    public String visitProcedureDeclaration(ProcedureDeclarationContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProcedureDeclaration'");
    }

    @Override
    public String visitFormalParameterList(FormalParameterListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFormalParameterList'");
    }

    @Override
    public String visitFormalParameterSection(FormalParameterSectionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFormalParameterSection'");
    }

    @Override
    public String visitParameterGroup(ParameterGroupContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitParameterGroup'");
    }

    @Override
    public String visitIdentifierList(IdentifierListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIdentifierList'");
    }

    @Override
    public String visitConstList(ConstListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstList'");
    }

    @Override
    public String visitFunctionDeclaration(FunctionDeclarationContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionDeclaration'");
    }

    @Override
    public String visitResultType(ResultTypeContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitResultType'");
    }

    @Override
    public String visitStatement(StatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStatement'");
    }

    @Override
    public String visitUnlabelledStatement(UnlabelledStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnlabelledStatement'");
    }

    @Override
    public String visitSimpleStatement(SimpleStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSimpleStatement'");
    }

    @Override
    public String visitAssignmentStatement(AssignmentStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignmentStatement'");
    }

    @Override
    public String visitVariable(VariableContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariable'");
    }

    @Override
    public String visitExpression(ExpressionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExpression'");
    }

    @Override
    public String visitRelationaloperator(RelationaloperatorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRelationaloperator'");
    }

    @Override
    public String visitSimpleExpression(SimpleExpressionContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSimpleExpression'");
    }

    @Override
    public String visitAdditiveoperator(AdditiveoperatorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAdditiveoperator'");
    }

    @Override
    public String visitTerm(TermContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTerm'");
    }

    @Override
    public String visitMultiplicativeoperator(MultiplicativeoperatorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitMultiplicativeoperator'");
    }

    @Override
    public String visitSignedFactor(SignedFactorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSignedFactor'");
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFactor'");
    }

    @Override
    public String visitUnsignedConstant(UnsignedConstantContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnsignedConstant'");
    }

    @Override
    public String visitFunctionDesignator(FunctionDesignatorContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionDesignator'");
    }

    @Override
    public String visitParameterList(ParameterListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitParameterList'");
    }

    @Override
    public String visitSet_(Set_Context ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSet_'");
    }

    @Override
    public String visitElementList(ElementListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitElementList'");
    }

    @Override
    public String visitElement(ElementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitElement'");
    }

    @Override
    public String visitProcedureStatement(ProcedureStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProcedureStatement'");
    }

    @Override
    public String visitActualParameter(ActualParameterContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitActualParameter'");
    }

    @Override
    public String visitParameterwidth(ParameterwidthContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitParameterwidth'");
    }

    @Override
    public String visitGotoStatement(GotoStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGotoStatement'");
    }

    @Override
    public String visitEmptyStatement_(EmptyStatement_Context ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitEmptyStatement_'");
    }

    @Override
    public String visitEmpty_(Empty_Context ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitEmpty_'");
    }

    @Override
    public String visitStructuredStatement(StructuredStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStructuredStatement'");
    }

    @Override
    public String visitCompoundStatement(CompoundStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCompoundStatement'");
    }

    @Override
    public String visitStatements(StatementsContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStatements'");
    }

    @Override
    public String visitConditionalStatement(ConditionalStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConditionalStatement'");
    }

    @Override
    public String visitIfStatement(IfStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public String visitCaseStatement(CaseStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCaseStatement'");
    }

    @Override
    public String visitCaseListElement(CaseListElementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCaseListElement'");
    }

    @Override
    public String visitRepetetiveStatement(RepetetiveStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRepetetiveStatement'");
    }

    @Override
    public String visitWhileStatement(WhileStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWhileStatement'");
    }

    @Override
    public String visitRepeatStatement(RepeatStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRepeatStatement'");
    }

    @Override
    public String visitForStatement(ForStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForStatement'");
    }

    @Override
    public String visitForList(ForListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForList'");
    }

    @Override
    public String visitInitialValue(InitialValueContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitInitialValue'");
    }

    @Override
    public String visitFinalValue(FinalValueContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFinalValue'");
    }

    @Override
    public String visitWithStatement(WithStatementContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWithStatement'");
    }

    @Override
    public String visitRecordVariableList(RecordVariableListContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitRecordVariableList'");
    }

}
