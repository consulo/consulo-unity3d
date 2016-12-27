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
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabTokenRole extends ShaderLabValueRole
{
	private IElementType myElementType;

	public ShaderLabTokenRole(IElementType elementType)
	{
		myElementType = elementType;
	}


	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
	{
		if(builder.getTokenType() == myElementType)
		{
			builder.advanceLexer();
			mark.done(ShaderLabElements.SIMPLE_VALUE);
			return mark;
		}
		else
		{
			return null;
		}
	}

	@Nullable
	@Override
	public String getDefaultInsertValue()
	{
		if(myElementType == ShaderLabTokens.INTEGER_LITERAL)
		{
			return "0";
		}
		return super.getDefaultInsertValue();
	}

	@Override
	public boolean isMyValue(@NotNull ShaderLabParserBuilder builder)
	{
		return builder.getTokenType() == myElementType;
	}
}
