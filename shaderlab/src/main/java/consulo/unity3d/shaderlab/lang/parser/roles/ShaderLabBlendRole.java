/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.shaderlab.lang.parser.roles;

import consulo.language.parser.PsiBuilder;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParser;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 25-Oct-17
 */
public class ShaderLabBlendRole extends ShaderLabRole
{
	private static final String[] ourFactors = new String[]{
			"One",
			"Zero",
			"SrcColor",
			"SrcAlpha",
			"DstColor",
			"DstAlpha",
			"OneMinusSrcColor",
			"OneMinusSrcAlpha",
			"OneMinusDstColor",
			"OneMinusDstAlpha",
	};

	private static final String ourOffKey = "Off";

	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark)
	{
		boolean off = false;
		if(builder.getTokenType() == ShaderLabTokens.IDENTIFIER)
		{
			if(ShaderLabParser.isTokenTextEqualTo(builder, ourOffKey))
			{
				ShaderLabParser.validateIdentifier(builder, ourOffKey);
				off = true;
			}
		}

		if(!off)
		{
			parseValue(builder);

			parseValue(builder);
		}

		mark.done(ShaderLabElements.BLEND);
		return mark;
	}

	private static void parseValue(PsiBuilder builder)
	{
		if(builder.getTokenType() == ShaderLabTokens.LBRACKET)
		{
			ShaderLabParser.parseBracketReference(builder);
		}
		else if(builder.getTokenType() == ShaderLabTokens.IDENTIFIER)
		{
			ShaderLabParser.validateIdentifier(builder, ourFactors);
		}
		else
		{
			builder.error("Unexpected token");
		}
	}

	@Nullable
	@Override
	public String getDefaultInsertValue()
	{
		return ourOffKey;
	}
}
