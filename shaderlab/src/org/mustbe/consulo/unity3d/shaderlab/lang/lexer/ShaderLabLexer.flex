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
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

SINGLE_VERBATIM_CHAR=[^\"]
QUOTE_ESC_SEQ=\"\"
VERBATIM_STRING_CHAR={SINGLE_VERBATIM_CHAR}|{QUOTE_ESC_SEQ}
VERBATIM_STRING_LITERAL=@\"{VERBATIM_STRING_CHAR}*\"
INTERPOLATION_STRING_LITERAL=\$\"{VERBATIM_STRING_CHAR}*\"

IDENTIFIER=@?[:jletter:] [:jletterdigit:]*

DIGIT = [0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]

INTEGER_LITERAL = {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*

FLOAT_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Ff] | {DIGITS} [Ff]
DOUBLE_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Dd]? | {DIGITS} [Dd]
DECIMAL_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Mm] | {DIGITS} [Mm]
DEC_FP_LITERAL = {DIGITS} {DEC_EXPONENT} | {DEC_SIGNIFICAND} {DEC_EXPONENT}?
DEC_SIGNIFICAND = "." {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*
DEC_EXPONENT = [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL = {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND = 0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT = [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*

%%

<YYINITIAL>
{
	{SINGLE_LINE_COMMENT}      { return ShaderLabTokens.LINE_COMMENT; }

	"Shader"                   { return ShaderLabTokens.SHADER_KEYWORD; }

	"Properties"               { return ShaderLabTokens.PROPERTIES_KEYWORD; }

	"CGPROGRAM"                { yybegin(SHADERSCRIPT); return ShaderLabTokens.CGPROGRAM_KEYWORD; }

	"ENDCG"                    { return ShaderLabTokens.ENDCG_KEYWORD; }

	"{"                        { return ShaderLabTokens.LBRACE; }

	"}"                        { return ShaderLabTokens.RBRACE; }

	{INTEGER_LITERAL}          { return ShaderLabTokens.INTEGER_LITERAL; }

	{STRING_LITERAL}           { return ShaderLabTokens.STRING_LITERAL; }

	{IDENTIFIER}               { return ShaderLabTokens.IDENTIFIER; }

	{WHITE_SPACE}              { return ShaderLabTokens.WHITE_SPACE; }

	.                          { return ShaderLabTokens.BAD_CHARACTER; }
}

<SHADERSCRIPT>
{
	"ENDCG"                    { yybegin(YYINITIAL); yypushback(5); }

	[^]                        { return ShaderLabTokens.SHADERSCRIPT; }
}
