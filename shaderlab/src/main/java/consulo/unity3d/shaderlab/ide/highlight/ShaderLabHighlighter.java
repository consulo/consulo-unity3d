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

package consulo.unity3d.shaderlab.ide.highlight;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import consulo.unity3d.shaderlab.lang.lexer.ShaderLabLexer;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokenSets;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourMap = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		ourMap.put(ShaderLabTokens.LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
		ourMap.put(ShaderLabTokens.BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
		ourMap.put(ShaderLabTokens.INTEGER_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
		ourMap.put(ShaderLabTokens.STRING_LITERAL, DefaultLanguageHighlighterColors.STRING);

		safeMap(ourMap, ShaderLabTokenSets.KEYWORDS, DefaultLanguageHighlighterColors.KEYWORD);
	}

	@Nonnull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new ShaderLabLexer();
	}

	@Nonnull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(ourMap.get(tokenType));
	}
}
