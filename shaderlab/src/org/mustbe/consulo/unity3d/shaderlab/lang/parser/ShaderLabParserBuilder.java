/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.shaderlab.lang.parser;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabKeyTokens;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.impl.PsiBuilderAdapter;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabParserBuilder extends PsiBuilderAdapter
{
	private static final Map<IElementType, String> ourKeywords = new HashMap<IElementType, String>();

	static
	{
		for(IElementType keyword : ShaderLabKeyTokens.ALL.getTypes())
		{
			String s = keyword.toString();
			s = s.replace("_KEYWORD", "");
			s = s.replace("_", "");
			ourKeywords.put(keyword, s);
		}
	}

	public ShaderLabParserBuilder(PsiBuilder delegate)
	{
		super(delegate);
	}

	public boolean is(@NotNull IElementType softTokenType)
	{
		IElementType tokenType = getTokenType();
		String tokenText = getTokenText();
		if(tokenType == null || tokenText == null)
		{
			return softTokenType == tokenType;
		}

		if(tokenType == ShaderLabTokens.IDENTIFIER)
		{
			String key = ourKeywords.get(softTokenType);
			if(key != null && key.equalsIgnoreCase(tokenText))
			{
				remapCurrentToken(softTokenType);
				return true;
			}
		}
		return tokenType == softTokenType;
	}
}
