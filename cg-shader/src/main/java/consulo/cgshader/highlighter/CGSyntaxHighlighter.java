/*
 * Copyright 2013-2016 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.cgshader.highlighter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import consulo.cgshader.lexer.CGLexer;
import consulo.cgshader.lexer.CGTokens;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourMap = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		ourMap.put(CGTokens.LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
		ourMap.put(CGTokens.BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
		ourMap.put(CGTokens.NUMBER_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
		ourMap.put(CGTokens.STRING_LITERAL, DefaultLanguageHighlighterColors.STRING);
		ourMap.put(CGTokens.KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
		ourMap.put(CGTokens.MACRO_KEYWORD, DefaultLanguageHighlighterColors.MACRO_KEYWORD);
		ourMap.put(CGTokens.LPAR, DefaultLanguageHighlighterColors.PARENTHESES);
		ourMap.put(CGTokens.RPAR, DefaultLanguageHighlighterColors.PARENTHESES);
		ourMap.put(CGTokens.LBRACE, DefaultLanguageHighlighterColors.BRACES);
		ourMap.put(CGTokens.RBRACE, DefaultLanguageHighlighterColors.BRACES);
		ourMap.put(CGTokens.LBRACKET, DefaultLanguageHighlighterColors.BRACKETS);
		ourMap.put(CGTokens.RBRACKET, DefaultLanguageHighlighterColors.BRACKETS);
	}

	@Nonnull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new CGLexer();
	}

	@Nonnull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(ourMap.get(tokenType));
	}
}
