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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokenSets;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ThreeState;

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
			doneError(builder, "Unexpected token");
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
				if(parseShaderInner(builder) != null)
				{
					continue;
				}

				if(builder.getTokenType() == ShaderLabTokens.LBRACE)
				{
					count++;
				}

				if(builder.getTokenType() == ShaderLabTokens.RBRACE)
				{
					if(count == 0)
					{
						break;
					}

					count--;
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

	private static PsiBuilder.Marker parseShaderInner(@NotNull PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == ShaderLabTokens.PROPERTIES_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();

			builder.advanceLexer();

			if(expectWithError(builder, ShaderLabTokens.LBRACE, "'{' expected"))
			{
				int balanceCount = 0;

				loop:
				while(!builder.eof())
				{
					ThreeState threeState = parseProperty(builder);
					switch(threeState)
					{
						case YES:
							if(balanceCount == 0)
							{
								break loop;
							}
							balanceCount--;
							doneError(builder, "Unexpected token");
							break;
						case UNSURE:
							if(builder.getTokenType() == ShaderLabTokens.LBRACE)
							{
								balanceCount++;
							}

							doneError(builder, "Unexpected token");
							break;
					}
				}

				expectWithError(builder, ShaderLabTokens.RBRACE, "'}' expected");
			}

			mark.done(ShaderLabElements.PROPERTY_LIST);
			return mark;
		}
		else if(tokenType == ShaderLabTokens.FALLBACK_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();

			builder.advanceLexer();

			IElementType valueTokenType = builder.getTokenType();
			if(valueTokenType == ShaderLabTokens.IDENTIFIER)
			{
				String tokenText = builder.getTokenText();
				assert tokenText != null;
				if(tokenText.equalsIgnoreCase("off"))
				{
					builder.advanceLexer();
				}
				else
				{
					doneError(builder, "Wrong value");
				}
			}
			else if(valueTokenType == ShaderLabTokens.STRING_LITERAL)
			{
				PsiBuilder.Marker refMarker = builder.mark();
				builder.advanceLexer();
				refMarker.done(ShaderLabElements.REFERENCE);
			}

			mark.done(ShaderLabElements.SIMPLE_VALUE);
			return mark;
		}
		else if(tokenType == ShaderLabTokens.SUBSHADER_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();

			builder.advanceLexer();

			if(PsiBuilderUtil.expect(builder, ShaderLabTokens.LBRACE))
			{
				//TODO [VISTALL] hack until full syntax parse
				int count = 0;
				while(!builder.eof())
				{
					if(parseSubShaderInner(builder) != null)
					{
						continue;
					}

					if(builder.getTokenType() == ShaderLabTokens.LBRACE)
					{
						count++;
					}

					if(builder.getTokenType() == ShaderLabTokens.RBRACE)
					{
						if(count == 0)
						{
							break;
						}

						count--;
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

			mark.done(ShaderLabElements.SUB_SHADER);
			return mark;
		}
		return null;
	}

	private static PsiBuilder.Marker parseSubShaderInner(@NotNull PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == ShaderLabTokens.PASS_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();

			builder.advanceLexer();

			if(PsiBuilderUtil.expect(builder, ShaderLabTokens.LBRACE))
			{
				//TODO [VISTALL] hack until full syntax parse
				int count = 0;
				while(!builder.eof())
				{
					if(parsePassInner(builder) != null)
					{
						continue;
					}

					if(builder.getTokenType() == ShaderLabTokens.LBRACE)
					{
						count++;
					}

					if(builder.getTokenType() == ShaderLabTokens.RBRACE)
					{
						if(count == 0)
						{
							break;
						}

						count--;
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

			mark.done(ShaderLabElements.PASS);
			return mark;
		}
		else if(tokenType == ShaderLabTokens.TAGS_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			if(expectWithError(builder, ShaderLabTokens.LBRACE, "'{' expected"))
			{
				while(!builder.eof())
				{
					if(builder.getTokenType() == ShaderLabTokens.STRING_LITERAL)
					{
						PsiBuilder.Marker optionMarker = builder.mark();
						builder.advanceLexer();
						if(expectWithError(builder, ShaderLabTokens.EQ, "'=' expected"))
						{
							expectWithError(builder, ShaderLabTokens.STRING_LITERAL, "Expected value");
						}
						optionMarker.done(ShaderLabElements.TAG);
					}
					else
					{
						break;
					}
				}
				expectWithError(builder, ShaderLabTokens.RBRACE, "'}' expected");
			}
			mark.done(ShaderLabElements.TAG_LIST);
			return mark;
		}
		return null;
	}

	private static PsiBuilder.Marker parsePassInner(@NotNull PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == ShaderLabTokens.COLOR_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			if(builder.getTokenType() == ShaderLabTokens.LPAR)
			{
				parseElementsInBraces(builder, ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, ShaderLabTokens.INTEGER_LITERAL);
			}
			else if(builder.getTokenType() == ShaderLabTokens.LBRACKET)
			{
				parseBracketReference(builder);
			}
			else
			{
				builder.error("Expected value");
			}
			mark.done(ShaderLabElements.SIMPLE_VALUE);
			return mark;
		}
		return null;
	}

	@NotNull
	private static ThreeState parseProperty(PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();

		PsiBuilder.Marker propertyMark = null;
		if(tokenType == ShaderLabTokens.LBRACKET)
		{
			propertyMark = builder.mark();
			builder.advanceLexer();

			if(!parseReference(builder))
			{
				builder.error("Expected identifier");
			}

			parseElementsInBraces(builder, ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, null);

			expectWithError(builder, ShaderLabTokens.RBRACKET, "']' expected");

			propertyMark.done(ShaderLabElements.PROPERTY_ATTRIBUTE);

			propertyMark = propertyMark.precede();

			tokenType = builder.getTokenType();
		}

		if(tokenType == ShaderLabTokens.IDENTIFIER)
		{
			PsiBuilder.Marker mark = propertyMark == null ? builder.mark() : propertyMark;

			builder.advanceLexer();

			ShaderLabPropertyType shaderLabPropertyType = null;
			if(expectWithError(builder, ShaderLabTokens.LPAR, "'(' expected"))
			{
				expectWithError(builder, ShaderLabTokens.STRING_LITERAL, "Name expected");
				expectWithError(builder, ShaderLabTokens.COMMA, "Comma expected");

				shaderLabPropertyType = parsePropertyType(builder);

				expectWithError(builder, ShaderLabTokens.RPAR, "')' expected");
			}

			if(expectWithError(builder, ShaderLabTokens.EQ, "'=' expected"))
			{
				if(shaderLabPropertyType != null)
				{
					PsiBuilder.Marker valueMark = builder.mark();
					switch(shaderLabPropertyType)
					{
						case Float:
						case Int:
						case Range:
							expectWithError(builder, ShaderLabTokens.INTEGER_LITERAL, "Value expected");
							break;
						case Color:
						case Vector:
							parseElementsInBraces(builder, ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, ShaderLabTokens.INTEGER_LITERAL);
							break;
						case Cube:
						case _2D:
						case _3D:
							expectWithError(builder, ShaderLabTokens.STRING_LITERAL, "Name expected");
							if(expectWithError(builder, ShaderLabTokens.LBRACE, "'{' expected"))
							{
								while(!builder.eof())
								{
									if(builder.getTokenType() == ShaderLabTokens.IDENTIFIER)
									{
										PsiBuilder.Marker optionMarker = builder.mark();
										builder.advanceLexer();
										expectWithError(builder, ShaderLabTokens.IDENTIFIER, "Expected value");
										optionMarker.done(ShaderLabElements.PROPERTY_OPTION);
									}
									else
									{
										break;
									}
								}
								expectWithError(builder, ShaderLabTokens.RBRACE, "'}' expected");
							}
							break;
					}
					valueMark.done(ShaderLabElements.PROPERTY_VALUE);
				}
				else
				{
					builder.error("Wrong property type");
				}
			}

			mark.done(ShaderLabElements.PROPERTY);
			return ThreeState.NO;
		}
		else if(tokenType == ShaderLabTokens.RBRACE)
		{
			return ThreeState.YES;
		}

		if(propertyMark != null)
		{
			propertyMark.error("Expected identifier");
		}
		return ThreeState.UNSURE;
	}

	private static boolean parseBracketReference(PsiBuilder builder)
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

	private static boolean parseReference(PsiBuilder builder)
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

	@Nullable
	private static ShaderLabPropertyType parsePropertyType(PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(ShaderLabTokenSets.TYPE_KEYWORDS.contains(tokenType))
		{
			PsiBuilder.Marker mark = builder.mark();

			String tokenText = builder.getTokenText();
			assert tokenText != null;
			ShaderLabPropertyType shaderLabPropertyType = ShaderLabPropertyType.find(tokenText);

			builder.advanceLexer();

			parseElementsInBraces(builder, ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, ShaderLabTokens.INTEGER_LITERAL);

			mark.done(ShaderLabElements.PROPERTY_TYPE);

			return shaderLabPropertyType;
		}
		else if(builder.getTokenType() == ShaderLabTokens.RPAR || builder.getTokenType() == ShaderLabTokens.EQ)
		{
			return null;
		}
		else
		{
			doneError(builder, "Type expected");
			return null;
		}
	}

	private static void parseElementsInBraces(@NotNull PsiBuilder builder, IElementType open, IElementType close, IElementType valid)
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

	private static void doneError(PsiBuilder builder, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();
		mark.error(message);
	}

	private static boolean expectWithError(PsiBuilder builder, IElementType elementType, @NotNull String message)
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
