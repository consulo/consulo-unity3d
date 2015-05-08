package org.mustbe.consulo.unity3d.shaderlab.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

%%

%public
%class _ShaderLabLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state SHADERSCRIPT

DIGIT=[0-9]
WHITE_SPACE=[ \n\r\t\f]+
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
MULTI_LINE_STYLE_COMMENT=("/*"{COMMENT_TAIL})|"/*"

STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

IDENTIFIER=[:jletter:] [:jletterdigit:]* | {INTEGER_LITERAL} [:jletter:]

DIGIT = [0-9]
DIGITS = {DIGIT}*

INTEGER_PREFIX = "+" | "-"
INTEGER_LITERAL = {INTEGER_PREFIX}? ({DIGITS} | {DIGITS} "." {DIGITS})

%%

<YYINITIAL>
{
	{SINGLE_LINE_COMMENT}      { return ShaderLabTokens.LINE_COMMENT; }

	{MULTI_LINE_STYLE_COMMENT} { return ShaderLabTokens.LINE_COMMENT; }

	"Shader"                   { return ShaderLabTokens.SHADER_KEYWORD; }

	"Properties"               { return ShaderLabTokens.PROPERTIES_KEYWORD; }

	"Fallback"                 { return ShaderLabTokens.FALLBACK_KEYWORD; }

	"SubShader"                { return ShaderLabTokens.SUBSHADER_KEYWORD; }

	"CGINCLUDE"                { yybegin(SHADERSCRIPT); return ShaderLabTokens.CGINCLUDE_KEYWORD; }

	"CGPROGRAM"                { yybegin(SHADERSCRIPT); return ShaderLabTokens.CGPROGRAM_KEYWORD; }

	"ENDCG"                    { return ShaderLabTokens.ENDCG_KEYWORD; }

	"{"                        { return ShaderLabTokens.LBRACE; }

	"}"                        { return ShaderLabTokens.RBRACE; }

	"("                        { return ShaderLabTokens.LPAR; }

	")"                        { return ShaderLabTokens.RPAR; }

	"["                        { return ShaderLabTokens.LBRACKET; }

	"]"                        { return ShaderLabTokens.RBRACKET; }

	","                        { return ShaderLabTokens.COMMA; }

	"="                        { return ShaderLabTokens.EQ; }

	{IDENTIFIER}               { return ShaderLabTokens.IDENTIFIER; }

	{INTEGER_LITERAL}          { return ShaderLabTokens.INTEGER_LITERAL; }

	{STRING_LITERAL}           { return ShaderLabTokens.STRING_LITERAL; }

	{WHITE_SPACE}              { return ShaderLabTokens.WHITE_SPACE; }

	.                          { return ShaderLabTokens.BAD_CHARACTER; }
}

<SHADERSCRIPT>
{
	"ENDCG"                    { yybegin(YYINITIAL); yypushback(5); }

	[^]                        { return ShaderLabTokens.SHADERSCRIPT; }
}
