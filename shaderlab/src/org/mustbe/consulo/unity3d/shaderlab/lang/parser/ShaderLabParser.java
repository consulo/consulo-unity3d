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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabParser implements PsiParser
{
	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(!parseShader(builder))
		{
			builder.error("Shader expected");
		}

		while(!builder.eof())
		{
			PsiBuilder.Marker m = builder.mark();
			builder.advanceLexer();
			m.error("Unexpected token");
		}
		mark.done(root);
		return builder.getTreeBuilt();
	}

	public static boolean parseShader(PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType != ShaderLabTokens.SHADER_KEYWORD)
		{
			return false;
		}

		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(!PsiBuilderUtil.expect(builder, ShaderLabTokens.STRING_LITERAL))
		{
			builder.error("Expected name");
		}

		if(PsiBuilderUtil.expect(builder, ShaderLabTokens.LBRACE))
		{
			//TODO [VISTALL] hack until full syntax parse
			int count = 0;
			while(!builder.eof())
			{
				if(builder.getTokenType() == ShaderLabTokens.LBRACE)
				{
					count ++;
				}

				if(builder.getTokenType() == ShaderLabTokens.RBRACE)
				{
					if(count == 0)
					{
						break;
					}

					count --;
				}
				builder.advanceLexer();
			}

			if(!PsiBuilderUtil.expect(builder, ShaderLabTokens.RBRACE))
			{
				builder.error("'}' expected");
			}
		}
		else
		{
			builder.error("'{' expected");
		}

		mark.done(ShaderLabElements.SHADER_DEF);
		return true;
	}
}
