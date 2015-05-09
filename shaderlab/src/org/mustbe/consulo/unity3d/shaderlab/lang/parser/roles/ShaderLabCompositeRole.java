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

package org.mustbe.consulo.unity3d.shaderlab.lang.parser.roles;

import org.mustbe.consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabCompositeRole extends ShaderLabRole
{
	private IElementType myDoneElement;
	private ShaderLabRole[] myRoles;

	public ShaderLabCompositeRole(IElementType elementType, IElementType doneElement, ShaderLabRole... roles)
	{
		super(elementType);
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
	public void parseImpl(ShaderLabParserBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

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
		mark.done(myDoneElement);
	}
}
