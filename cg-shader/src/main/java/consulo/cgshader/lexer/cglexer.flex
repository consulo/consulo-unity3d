package consulo.cgshader.lexer;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.lexer.LexerBase;
%%

%public
%unicode
%class CGLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE_CHAR=[\ \n\r\t\f]

IDENTIFIER=[:jletter:] [:jletterdigit:]*

C_STYLE_COMMENT=("/*"[^"*"]{COMMENT_TAIL})|"/*"
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
END_OF_LINE_COMMENT="/""/"[^\r\n]*

DIGIT = [0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]

INTEGER_LITERAL = {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL}
LONG_LITERAL = {INTEGER_LITERAL} [Ll]
HEX_INTEGER_LITERAL = 0 [Xx] {HEX_DIGIT_OR_UNDERSCORE}*
BIN_INTEGER_LITERAL = 0 [Bb] {DIGIT_OR_UNDERSCORE}*

FLOAT_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Ff] | {DIGITS} [Ff]
DOUBLE_LITERAL = ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Dd]? | {DIGITS} [Dd]
DEC_FP_LITERAL = {DIGITS} {DEC_EXPONENT} | {DEC_SIGNIFICAND} {DEC_EXPONENT}?
DEC_SIGNIFICAND = "." {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*
DEC_EXPONENT = [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL = {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND = 0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT = [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*

CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

%%

<YYINITIAL> {WHITE_SPACE_CHAR}+ { return WHITE_SPACE; }

<YYINITIAL> {C_STYLE_COMMENT} { return CGTokens.LINE_COMMENT; }
<YYINITIAL> {END_OF_LINE_COMMENT} { return CGTokens.BLOCK_COMMENT; }

<YYINITIAL> {LONG_LITERAL} { return CGTokens.NUMBER_LITERAL; }
<YYINITIAL> {INTEGER_LITERAL} { return CGTokens.NUMBER_LITERAL; }
<YYINITIAL> {FLOAT_LITERAL} { return CGTokens.NUMBER_LITERAL; }
<YYINITIAL> {DOUBLE_LITERAL} { return CGTokens.NUMBER_LITERAL; }
<YYINITIAL> {CHARACTER_LITERAL} { return CGTokens.STRING_LITERAL; }
<YYINITIAL> {STRING_LITERAL} { return CGTokens.STRING_LITERAL; }

<YYINITIAL> "("   { return CGTokens.LPAR; }
<YYINITIAL> ")"   { return CGTokens.RPAR; }
<YYINITIAL> "{"   { return CGTokens.LBRACE; }
<YYINITIAL> "}"   { return CGTokens.RBRACE; }
<YYINITIAL> "["   { return CGTokens.LBRACKET; }
<YYINITIAL> "]"   { return CGTokens.RBRACKET; }


<YYINITIAL> "#" {WHITE_SPACE_CHAR}* {IDENTIFIER}
{
  return CGTokens.MACRO_KEYWORD;
}

<YYINITIAL> {IDENTIFIER}
{
 CharSequence text = yytext();
 if(consulo.cgshader.lexer.CGKeywords.KEYWORDS.contains(text.toString())) // toString is bad
 {
   return CGTokens.KEYWORD;
 }

 return CGTokens.TEXT;
}

<YYINITIAL> [^]
{
 return CGTokens.TEXT;
}
