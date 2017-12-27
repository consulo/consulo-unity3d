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

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.psi.tree.IElementType;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParser;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabCompositeRole extends ShaderLabRole
{
	@NotNull
	private IElementType myDoneElement;
	private ShaderLabRole[] myRoles;

	public ShaderLabCompositeRole(@NotNull IElementType doneElement, ShaderLabRole... roles)
	{
		myDoneElement = doneElement;
		myRoles = roles;
	}

	public ShaderLabRole[] getRoles()
	{
		return myRoles;
	}

	public void parseBefore(ShaderLabParserBuilder builder)
	{
	}

	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
	{
		parseBefore(builder);

		if(PsiBuilderUtil.expect(builder, ShaderLabTokens.LBRACE))
		{
			//TODO [VISTALL] hack until full syntax parse
			int count = 0;
			loop:
			while(!builder.eof())
			{
				for(ShaderLabRole role : myRoles)
				{
					if(role.tryParse(builder))
					{
						continue loop;
					}
				}

				IElementType tokenType = builder.getTokenType();
				if(tokenType == ShaderLabTokens.LBRACE)
				{
					count++;
				}

				if(tokenType == ShaderLabTokens.RBRACE)
				{
					if(count == 0)
					{
						break;
					}

					count--;
				}

				if(tokenType == ShaderLabTokens.CGINCLUDE_KEYWORD || tokenType == ShaderLabTokens.CGPROGRAM_KEYWORD)
				{
					PsiBuilder.Marker marker = builder.mark();
					builder.advanceLexer();
					if(builder.getTokenType() == ShaderLabTokens.SHADERSCRIPT)
					{
						builder.advanceLexer();
					}
					ShaderLabParser.expectWithError(builder, ShaderLabTokens.ENDCG_KEYWORD, "Expected 'ENDCG'");
					marker.done(ShaderLabElements.CG_SHADER);
				}
				else
				{
					builder.advanceLexer();
				}
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
		mark.done(myDoneElement);
		return mark;
	}
}
