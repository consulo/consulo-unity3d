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

import consulo.language.parser.PsiBuilder;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabCommaPairRole extends ShaderLabValueRole
{
	private ShaderLabRole myFirstRole;
	private ShaderLabRole mySecondRole;

	public ShaderLabCommaPairRole(ShaderLabRole firstRole, ShaderLabRole secondRole)
	{
		myFirstRole = firstRole;
		mySecondRole = secondRole;
	}

	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @Nonnull PsiBuilder.Marker mark)
	{
		PsiBuilder.Marker firstMarker = builder.mark();
		if(myFirstRole.parseAndDone(builder, firstMarker) != null)
		{
			if(builder.getTokenType() == ShaderLabTokens.COMMA)
			{
				builder.advanceLexer();

				PsiBuilder.Marker secondMark = builder.mark();
				if(mySecondRole.parseAndDone(builder, secondMark) == null)
				{
					secondMark.error("Expected second value");
				}
			}
			else
			{
				builder.error("Expected comma");
			}
		}
		else
		{
			firstMarker.error("Expected value");
		}

		mark.done(ShaderLabElements.PAIR_VALUE);
		return mark;
	}

	@Nullable
	@Override
	public String getDefaultInsertValue()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(myFirstRole.getDefaultInsertValue());
		builder.append(", ");
		builder.append(mySecondRole.getDefaultInsertValue());
		return builder.toString();
	}

	@Override
	public boolean isMyValue(@Nonnull ShaderLabParserBuilder builder)
	{
		return myFirstRole instanceof ShaderLabValueRole && ((ShaderLabValueRole) myFirstRole).isMyValue(builder);
	}
}
