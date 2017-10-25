package consulo.unity3d.shaderlab.lang.parser.roles;

import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.doneError;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.expectWithError;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseBracketReference;
import static consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseElementsInBraces;

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
 * @since 25-Oct-17
 */
public interface ShaderLabRoles
{
	ShaderLabRole Properties = new ShaderLabRole()
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
	ShaderLabRole Tags = new ShaderLabRole()
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

	ShaderLabRole Cull = new ShaderLabSimpleRole("Off", "Back", "Front");

	ShaderLabRole ZWrite = new ShaderLabOrRole(ShaderLabReferenceRole.INSTANCE, new ShaderLabSimpleRole("On", "Off"));

	ShaderLabRole Lighting = new ShaderLabSimpleRole("Off", "On");

	ShaderLabRole SeparateSpecular = new ShaderLabSimpleRole("Off", "On");

	ShaderLabRole ColorMaterial = new ShaderLabSimpleRole("Emission", "AmbientAndDiffuse");

	ShaderLabRole Mode = new ShaderLabSimpleRole("Off", "Global", "Linear", "Exp", "Exp2");

	ShaderLabRole ZTest = new ShaderLabSimpleRole("Off", "Always", "Less", "Greater", "LEqual", "GEqual", "Equal", "NotEqual");

	ShaderLabRole Color = new ShaderLabColorRole();

	ShaderLabRole Fallback = new ShaderLabRole()
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

	ShaderLabRole Name = new ShaderLabTokenRole(ShaderLabTokens.STRING_LITERAL);

	ShaderLabRole UsePass = new ShaderLabRole()
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

	ShaderLabRole ConstantColor = new ShaderLabColorRole();

	ShaderLabRole Matrix = new ShaderLabRole()
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

	ShaderLabRole SetTexture = new ShaderLabCompositeRole(ShaderLabElements.SET_TEXTURE, Matrix, ConstantColor)
	{
		@Override
		public void parseBefore(ShaderLabParserBuilder builder)
		{
			if(!parseBracketReference(builder))
			{
				builder.error("Expected reference");
			}
		}
	};

	ShaderLabRole Diffuse = new ShaderLabColorRole();

	ShaderLabRole Ambient = new ShaderLabColorRole();

	ShaderLabRole Shininess = new ShaderLabColorRole();

	ShaderLabRole Specular = new ShaderLabColorRole();

	ShaderLabRole Emission = new ShaderLabColorRole();

	ShaderLabRole Material = new ShaderLabCompositeRole(ShaderLabElements.MATERIAL, Diffuse, Ambient, Shininess, Specular, Emission);

	ShaderLabRole LOD = new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL);

	ShaderLabRole Offset = new ShaderLabCommaPairRole(new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL), new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL));

	ShaderLabRole CustomEditor = new ShaderLabRole()
	{
		@Override
		public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark)
		{

			if(builder.getTokenType() == ShaderLabTokens.STRING_LITERAL)
			{
				PsiBuilder.Marker refMarker = builder.mark();
				expectWithError(builder, ShaderLabTokens.STRING_LITERAL, "Editor type expected");
				refMarker.done(ShaderLabElements.REFERENCE);
			}
			else
			{
				doneWithErrorSafe(builder, "Editor type expected");
			}

			mark.done(ShaderLabElements.SIMPLE_VALUE);
			return mark;
		}
	};

	ShaderLabRole AlphaTest = new ShaderLabOrRole(new ShaderLabSimpleRole("Off"), new ShaderLabPairRole(new ShaderLabSimpleRole("Always", "Less", "Greater", "LEqual", "GEqual", "Equal", "NotEqual",
			"Never"), new ShaderLabOrRole(new ShaderLabTokenRole(ShaderLabTokens.INTEGER_LITERAL), ShaderLabReferenceRole.INSTANCE)))
	{
		@Nullable
		@Override
		public String getDefaultInsertValue()
		{
			return "Off";
		}
	};

	ShaderLabRole Fog = new ShaderLabCompositeRole(ShaderLabElements.FOG, Color, Mode);

	ShaderLabRole Pass = new ShaderLabCompositeRole(ShaderLabElements.PASS, Name, Tags, Color, SetTexture, Lighting, ZWrite, Cull, Fog, ZTest, SeparateSpecular, Material, AlphaTest, Offset);

	ShaderLabRole SubShader = new ShaderLabCompositeRole(ShaderLabElements.SUB_SHADER, Pass, Tags, Lighting, Offset, ZWrite, Cull, Fog, ZTest, UsePass, Material, LOD);

	ShaderLabRole Shader = new ShaderLabCompositeRole(ShaderLabElements.SHADER_DEF, Properties, Fallback, CustomEditor, SubShader)
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
}
