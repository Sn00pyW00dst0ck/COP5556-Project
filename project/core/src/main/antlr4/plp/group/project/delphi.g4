/*
BSD License

Copyright (c) 2013, Tom Everett
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Tom Everett nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
Adapted from pascal.g by  Hakki Dogusan, Piet Schoutteten and Marton Papp
*/

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

parser grammar delphi;

options {
    caseInsensitive = true;
    tokenVocab = delphi_lexer;
}

program
    : programHeading (INTERFACE)? block DOT EOF
    ;

programHeading
    : PROGRAM identifier (LPAREN identifierList RPAREN)? SEMI
    | UNIT identifier SEMI
    ;

identifier
    : IDENT
    ;

declarationPart
    : labelDeclarationPart
    | constantDefinitionPart
    | typeDefinitionPart
    | variableDeclarationPart
    | callableImplementationPart
    | usesUnitsPart
    | IMPLEMENTATION
    ;

block
    : declarationPart* compoundStatement
    ;

usesUnitsPart
    : USES identifierList SEMI
    ;

labelDeclarationPart
    : LABEL label (COMMA label)* SEMI
    ;

label
    : unsignedInteger
    ;

constantDefinitionPart
    : CONST (constantDefinition SEMI)+
    ;

constantDefinition
    : identifier EQUAL constant
    ;

constantChr
    : CHR LPAREN unsignedInteger RPAREN
    ;

constant
    : unsignedNumber
    | sign unsignedNumber
    | identifier
    | sign identifier
    | string
    | constantChr
    ;

unsignedNumber
    : unsignedInteger
    | unsignedReal
    ;

unsignedInteger
    : NUM_INT
    ;

unsignedReal
    : NUM_REAL
    ;

sign
    : PLUS
    | MINUS
    ;

bool_
    : TRUE
    | FALSE
    ;

string
    : STRING_LITERAL
    ;

typeDefinitionPart
    : TYPE (typeDefinition SEMI)+
    ;

typeDefinition
    : identifier EQUAL (type_ | functionType | procedureType | classType)
    ;

functionType
    : FUNCTION (formalParameterList)? COLON resultType
    ;

procedureType
    : PROCEDURE (formalParameterList)?
    ;

type_
    : simpleType
    | structuredType
    | pointerType
    ;

simpleType
    : scalarType
    | subrangeType
    | typeIdentifier
    | stringtype
    ;

scalarType
    : LPAREN identifierList RPAREN
    ;

subrangeType
    : constant DOTDOT constant
    ;

typeIdentifier
    : identifier
    | (CHAR | BOOLEAN | INTEGER | REAL | STRING)
    ;

structuredType
    : PACKED unpackedStructuredType
    | unpackedStructuredType
    ;

unpackedStructuredType
    : arrayType
    | recordType
    | setType
    | fileType
    ;

stringtype
    : STRING LBRACK (identifier | unsignedNumber) RBRACK
    ;

arrayType
    : ARRAY LBRACK typeList RBRACK OF componentType
    | ARRAY LBRACK2 typeList RBRACK2 OF componentType
    ;

typeList
    : indexType (COMMA indexType)*
    ;

indexType
    : simpleType
    ;

componentType
    : type_
    ;

recordType
    : RECORD fieldList? END
    ;

fieldList
    : fixedPart (SEMI variantPart)?
    | variantPart
    ;

fixedPart
    : recordSection (SEMI recordSection)*
    ;

recordSection
    : identifierList COLON type_
    ;

variantPart
    : CASE tag OF variant (SEMI variant)*
    ;

tag
    : identifier COLON typeIdentifier
    | typeIdentifier
    ;

variant
    : constList COLON LPAREN fieldList RPAREN
    ;

setType
    : SET OF baseType
    ;

baseType
    : simpleType
    ;

fileType
    : FILE OF type_
    | FILE
    ;

pointerType
    : POINTER typeIdentifier
    ;

classType
    : CLASS (visibilitySection)+ END
    ;

visibilitySection
    : (PRIVATE | PROTECTED | PUBLIC) classMemberDeclaration+
    ;

classMemberDeclaration
    : classFieldDeclaration SEMI
    | classProcedureDeclaration SEMI
    | classFunctionDeclaration SEMI
    | constructorDeclaration SEMI
    | destructorDeclaration SEMI
    ;

classFieldDeclaration
    : identifier COLON type_
    ;

classProcedureDeclaration
    : PROCEDURE identifier (formalParameterList)?
    ;

classFunctionDeclaration
    : FUNCTION identifier (formalParameterList)? COLON resultType
    ;

constructorDeclaration
    : CONSTRUCTOR identifier (formalParameterList)?
    ;

destructorDeclaration
    : DESTRUCTOR identifier
    ;

variableDeclarationPart
    : VAR variableDeclaration (SEMI variableDeclaration)* SEMI
    ;

variableDeclaration
    : identifierList COLON type_
    ;

callableImplementationPart
    : callableImplementation (SEMI callableImplementation)* SEMI
    ;

callableImplementation
    : procedureImplementation
    | functionImplementation
    | constructorImplementation
    | destructorImplementation
    | classProcedureImplementation
    | classFunctionImplementation
    ;

procedureImplementation
    : PROCEDURE identifier (formalParameterList)? SEMI block
    ;

formalParameterList
    : LPAREN formalParameterSection (SEMI formalParameterSection)* RPAREN
    ;

// We could have no parameters, so this is needed...
formalParameterSection
    : parameterGroup
    | VAR parameterGroup
    | FUNCTION parameterGroup
    | PROCEDURE parameterGroup
    |
    ;

parameterGroup
    : identifierList COLON typeIdentifier
    ;

identifierList
    : identifier (COMMA identifier)*
    ;

constList
    : constant (COMMA constant)*
    ;

functionImplementation
    : FUNCTION identifier (formalParameterList)? COLON resultType SEMI block
    ;

resultType
    : typeIdentifier
    ;

constructorImplementation
    : CONSTRUCTOR identifier DOT identifier (formalParameterList)? SEMI block
    ;

destructorImplementation
    : DESTRUCTOR identifier DOT identifier SEMI block
    ;

classProcedureImplementation
    : PROCEDURE identifier DOT identifier (formalParameterList)? SEMI block
    ;

classFunctionImplementation
    : FUNCTION identifier DOT identifier (formalParameterList)? COLON resultType SEMI block
    ;

statement
    : label COLON unlabelledStatement
    | unlabelledStatement
    ;

unlabelledStatement
    : simpleStatement
    | structuredStatement
    ;

simpleStatement
    : assignmentStatement
    | gotoStatement
    | procedureStatement
    | variable
    | emptyStatement_
    ;

assignmentStatement
    : variable ASSIGN expression
    ;

variable
    : (AT identifier | identifier) postFixPart*
    ;

postFixPart
    : DOT identifier
    | DOT functionDesignator
    | LBRACK expression (COMMA expression)* RBRACK
    | LBRACK2 expression (COMMA expression)* RBRACK2
    | POINTER
    ;

expression
    : simpleExpression (relationaloperator expression)?
    ;

relationaloperator
    : EQUAL
    | NOT_EQUAL
    | LT
    | LE
    | GE
    | GT
    | IN
    ;

simpleExpression
    : term (additiveoperator simpleExpression)?
    ;

additiveoperator
    : PLUS
    | MINUS
    | OR
    ;

term
    : signedFactor (multiplicativeoperator term)?
    ;

multiplicativeoperator
    : STAR
    | SLASH
    | DIV
    | MOD
    | AND
    ;

signedFactor
    : (PLUS | MINUS)? factor
    ;

factor
    : variable
    | LPAREN expression RPAREN
    | functionDesignator
    | unsignedConstant
    | set_
    | NOT factor
    | bool_
    ;

unsignedConstant
    : unsignedNumber
    | constantChr
    | string
    | NIL
    ;

functionDesignator
    : identifier LPAREN parameterList RPAREN
    ;

parameterList
    : actualParameter (COMMA actualParameter)*
    |
    ;

set_
    : LBRACK elementList RBRACK
    | LBRACK2 elementList RBRACK2
    ;

elementList
    : element (COMMA element)*
    |
    ;

element
    : expression (DOTDOT expression)?
    ;

procedureStatement
    : identifier (LPAREN parameterList RPAREN)?
    ;

actualParameter
    : expression parameterwidth*
    ;

parameterwidth
    : COLON expression
    ;

gotoStatement
    : GOTO label
    ;

emptyStatement_
    :
    ;

empty_
    :
    /* empty */
    ;

structuredStatement
    : compoundStatement
    | conditionalStatement
    | repetetiveStatement
    | withStatement
    ;

compoundStatement
    : BEGIN statements END
    ;

statements
    : statement (SEMI statement)*
    ;

conditionalStatement
    : ifStatement
    | caseStatement
    ;

ifStatement
    : IF expression THEN statement (: ELSE statement)?
    ;

caseStatement
    : CASE expression OF caseListElement (SEMI caseListElement)* (SEMI ELSE statements)? END
    ;

caseListElement
    : constList COLON statement
    ;

repetetiveStatement
    : whileStatement
    | repeatStatement
    | forStatement
    ;

whileStatement
    : WHILE expression DO statement
    ;

repeatStatement
    : REPEAT statements UNTIL expression
    ;

forStatement
    : FOR identifier ASSIGN forList DO statement
    ;

forList
    : initialValue (TO | DOWNTO) finalValue
    ;

initialValue
    : expression
    ;

finalValue
    : expression
    ;

withStatement
    : WITH recordVariableList DO statement
    ;

recordVariableList
    : variable (COMMA variable)*
    ;