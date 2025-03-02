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

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderUtil;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoleHolder;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabKeyTokens;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabParser implements PsiParser
{
	static
	{
		ShaderLabRoleHolder.build();
	}

	@Nonnull
	@Override
	public ASTNode parse(@Nonnull IElementType root, @Nonnull PsiBuilder b, @Nonnull LanguageVersion languageVersion)
	{
		ShaderLabParserBuilder builder = new ShaderLabParserBuilder(b);

		PsiBuilder.Marker mark = builder.mark();

		if(!ShaderLabRoles.Shader.tryParse(builder))
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

	public static boolean isTokenTextEqualTo(@Nonnull PsiBuilder builder, String value)
	{
		assert builder.getTokenType() == ShaderLabTokens.IDENTIFIER;
		String tokenText = builder.getTokenText();
		return Objects.equals(tokenText, value);
	}

	public static boolean validateIdentifier(@Nonnull PsiBuilder builder, String... values)
	{
		assert builder.getTokenType() == ShaderLabTokens.IDENTIFIER;
		String tokenText = builder.getTokenText();
		boolean found = false;
		for(String value : values)
		{
			if(value.equalsIgnoreCase(tokenText))
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

	public static void parseElementsInBraces(@Nonnull PsiBuilder builder, IElementType open, IElementType close, IElementType valid)
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

	public static boolean expectWithError(PsiBuilder builder, IElementType elementType, @Nonnull String message)
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
