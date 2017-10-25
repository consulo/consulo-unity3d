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

package consulo.unity3d.shaderlab.lang.parser.roles;

import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.doneError;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.expectWithError;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseBracketReference;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseElementsInBraces;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ThreeState;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParser;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public abstract class ShaderLabRole
{
	public static final ShaderLabRole Properties = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
		{
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


		@NotNull
		private ThreeState parseProperty(PsiBuilder builder)
		{
			IElementType tokenType = builder.getTokenType();

			PsiBuilder.Marker propertyMark = null;
			if(tokenType == ShaderLabTokens.LBRACKET)
			{
				propertyMark = builder.mark();
				builder.advanceLexer();

				if(!ShaderLabParser.parseReference(builder))
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
							case Any:
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

		@Nullable
		public ShaderLabPropertyType parsePropertyType(PsiBuilder builder)
		{
			IElementType tokenType = builder.getTokenType();
			if(tokenType == ShaderLabTokens.IDENTIFIER)
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
	};
	public static final ShaderLabRole Tags = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
		{
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
	};

	public static final ShaderLabRole Cull = new ShaderLabSimpleRole("Off", "Back", "Front");

	public static final ShaderLabRole ZWrite = new ShaderLabOrRole(ShaderLabReferenceRole.INSTANCE, new ShaderLabSimpleRole("On", "Off"));

	public static final ShaderLabRole Lighting = new ShaderLabSimpleRole("Off", "On");

	public static final ShaderLabRole SeparateSpecular = new ShaderLabSimpleRole("Off", "On");

	public static final ShaderLabRole ColorMaterial = new ShaderLabSimpleRole("Emission", "AmbientAndDiffuse");

	public static final ShaderLabRole Mode = new ShaderLabSimpleRole("Off", "Global", "Linear", "Exp", "Exp2");

	public static final ShaderLabRole ZTest = new ShaderLabSimpleRole("Always", "Less", "Greater", "LEqual", "GEqual", "Equal", "NotEqual");

	public static final ShaderLabRole Color = new ShaderLabColorRole();

	public static final ShaderLabRole Fallback = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
		{
			IElementType valueTokenType = builder.getTokenType();
			if(valueTokenType == ShaderLabTokens.IDENTIFIER)
			{
				ShaderLabParser.validateIdentifier(builder, "Off");
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

		@Nullable
		@Override
		public String getDefaultInsertValue()
		{
			return "Off";
		}
	};

	public static final ShaderLabRole UsePass = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
		{
			IElementType valueTokenType = builder.getTokenType();
			if(valueTokenType == ShaderLabTokens.STRING_LITERAL)
			{
				PsiBuilder.Marker refMarker = builder.mark();
				builder.advanceLexer();
				refMarker.done(ShaderLabElements.REFERENCE);
			}
			else
			{
				doneWithErrorSafe(builder, "Wrong value");
			}

			mark.done(ShaderLabElements.SIMPLE_VALUE);
			return mark;
		}
	};

	public static final ShaderLabRole ConstantColor = new ShaderLabColorRole();

	public static final ShaderLabRole Matrix = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
		{
			if(builder.getTokenType() == ShaderLabTokens.LBRACKET)
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
	};

	public static final ShaderLabRole SetTexture = new ShaderLabCompositeRole(ShaderLabElements.SET_TEXTURE, Matrix, ConstantColor)
	{
		@Override
		public void parseBefore(ShaderLabParserBuilder builder)
		{
			if(!ShaderLabParser.parseBracketReference(builder))
			{
				builder.error("Expected reference");
			}
		}
	};

	public static final ShaderLabRole Diffuse = new ShaderLabColorRole();

	public static final ShaderLabRole Ambient = new ShaderLabColorRole();

	public static final ShaderLabRole Shininess = new ShaderLabColorRole();

	public static final ShaderLabRole Specular = new ShaderLabColorRole();

	public static final ShaderLabRole Emission = new ShaderLabColorRole();

	public static final ShaderLabRole Material = new ShaderLabCompositeRole(ShaderLabElements.MATERIAL, Diffuse, Ambient, Shininess, Specular, Emission);

	public static final ShaderLabRole LOD = new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL);

	public static final ShaderLabRole Offset = new ShaderLabCommaPairRole(new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL), new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL));

	public static final ShaderLabRole AlphaTest = new ShaderLabOrRole(new ShaderLabSimpleRole("Off"), new ShaderLabPairRole(new ShaderLabSimpleRole("Always", "Less", "Greater", "LEqual", "GEqual",
			"Equal", "NotEqual", "Never"), new ShaderLabOrRole(new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL), ShaderLabReferenceRole.INSTANCE)))
	{
		@Nullable
		@Override
		public String getDefaultInsertValue()
		{
			return "Off";
		}
	};

	public static final ShaderLabRole Fog = new ShaderLabCompositeRole(ShaderLabElements.FOG, Color, Mode);

	public static final ShaderLabRole Pass = new ShaderLabCompositeRole(ShaderLabElements.PASS, Tags, Color, SetTexture, Lighting, ZWrite, Cull, Fog, ZTest, SeparateSpecular, Material, AlphaTest,
			Offset);

	public static final ShaderLabRole SubShader = new ShaderLabCompositeRole(ShaderLabElements.SUB_SHADER, Pass, Tags, Lighting, ZWrite, Cull, Fog, UsePass, Material, LOD);

	public static final ShaderLabRole Shader = new ShaderLabCompositeRole(ShaderLabElements.SHADER_DEF, Properties, Fallback, SubShader)
	{
		@Override
		public void parseBefore(ShaderLabParserBuilder builder)
		{
			if(!PsiBuilderUtil.expect(builder, ShaderLabTokens.STRING_LITERAL))
			{
				builder.error("Expected name");
			}
		}
	};

	private String myName;

	public ShaderLabRole()
	{
	}

	@NotNull
	public String getName()
	{
		return myName;
	}

	@Nullable
	public String getDefaultInsertValue()
	{
		return null;
	}

	public boolean tryParse(ShaderLabParserBuilder builder)
	{
		if(builder.is(this))
		{
			parseImpl(builder);
			return true;
		}
		return false;
	}

	protected void doneWithErrorSafe(@NotNull ShaderLabParserBuilder builder, @NotNull String error)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == ShaderLabTokens.LBRACE || tokenType == ShaderLabTokens.RBRACE)
		{
			builder.error("Expected value");
		}
		else
		{
			doneError(builder, error);
		}
	}

	public PsiBuilder.Marker parseImpl(ShaderLabParserBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(parseAndDone(builder, mark) == null)
		{
			builder.error("Expected value");
			mark.done(ShaderLabElements.SIMPLE_VALUE);
		}

		return mark;
	}

	public abstract PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark);

	private static Map<String, ShaderLabRole> ourRoles = new HashMap<>();

	@Nullable
	public static ShaderLabRole findRole(String name)
	{
		name = name.toLowerCase(Locale.US);
		return ourRoles.get(name);
	}

	static
	{
		Field[] declaredFields = ShaderLabRole.class.getFields();
		for(Field declaredField : declaredFields)
		{
			if(Modifier.isStatic(declaredField.getModifiers()))
			{
				try
				{
					ShaderLabRole value = (ShaderLabRole) declaredField.get(null);
					value.myName = declaredField.getName();
					ourRoles.put(declaredField.getName().toLowerCase(), value);
				}
				catch(IllegalAccessException e)
				{
					throw new Error(e);
				}
			}
		}
	}
}
