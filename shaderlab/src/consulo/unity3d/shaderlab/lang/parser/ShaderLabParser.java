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

package consulo.unity3d.shaderlab.lang.parser;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import consulo.lang.LanguageVersion;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabKeyTokens;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabParser implements PsiParser
{
	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder b, @NotNull LanguageVersion languageVersion)
	{
		ShaderLabParserBuilder builder = new ShaderLabParserBuilder(b);
		//b.setDebugMode(true);

		PsiBuilder.Marker mark = builder.mark();

		if(!ShaderLabRole.Shader.tryParse(builder))
		{
			builder.error("Shader expected");
		}

		while(!builder.eof())
		{
			doneError(builder, "Unexpected token");
		}
		mark.done(root);
		return builder.getTreeBuilt();
	}


	public static boolean validateIdentifier(@NotNull PsiBuilder builder, String... values)
	{
		assert builder.getTokenType() == ShaderLabTokens.IDENTIFIER;
		String tokenText = builder.getTokenText();
		boolean found = false;
		for(String s : values)
		{
			if(s.equalsIgnoreCase(tokenText))
			{
				found = true;
				break;
			}
		}

		if(found)
		{
			builder.remapCurrentToken(ShaderLabKeyTokens.VALUE_KEYWORD);
			builder.advanceLexer();
			return true;
		}
		else
		{
			doneError(builder, "Wrong value");
			return false;
		}
	}

	public static boolean parseBracketReference(PsiBuilder builder)
	{
		if(expectWithError(builder, ShaderLabTokens.LBRACKET, "'[' expected"))
		{
			if(!parseReference(builder))
			{
				builder.error("Expected reference");
			}
			expectWithError(builder, ShaderLabTokens.RBRACKET, "']' expected");
			return true;
		}
		return false;
	}

	public static boolean parseReference(PsiBuilder builder)
	{
		if(builder.getTokenType() == ShaderLabTokens.IDENTIFIER)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			mark.done(ShaderLabElements.REFERENCE);
			return true;
		}
		return false;
	}

	public static void parseElementsInBraces(@NotNull PsiBuilder builder, IElementType open, IElementType close, IElementType valid)
	{
		if(builder.getTokenType() == open)
		{
			builder.advanceLexer();

			while(!builder.eof())
			{
				IElementType tokenType = builder.getTokenType();
				if(tokenType == close)
				{
					break;
				}

				if(valid != null)
				{
					if(tokenType == valid)
					{
						builder.advanceLexer();
					}
					else
					{
						doneError(builder, "Unexpected token");
					}
				}
				else
				{
					builder.advanceLexer();
				}

				if(builder.getTokenType() == ShaderLabTokens.COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() != close)
				{
					doneError(builder, "Unexpected token");
				}
			}
			expectWithError(builder, close, "Unexpected token");
		}
	}

	public static void doneError(PsiBuilder builder, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();
		mark.error(message);
	}

	public static boolean expectWithError(PsiBuilder builder, IElementType elementType, @NotNull String message)
	{
		if(PsiBuilderUtil.expect(builder, elementType))
		{
			return true;
		}
		else
		{
			builder.error(message);
			return false;
		}
	}
}
