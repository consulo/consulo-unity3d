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

import static org.mustbe.consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseBracketReference;
import static org.mustbe.consulo.unity3d.shaderlab.lang.parser.ShaderLabParser.parseElementsInBraces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.PsiBuilder;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabColorRole extends ShaderLabValueRole
{
	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark)
	{
		if(builder.getTokenType() == ShaderLabTokens.LPAR)
		{
			PsiBuilder.Marker valueMarker = builder.mark();
			parseElementsInBraces(builder, ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, ShaderLabTokens.INTEGER_LITERAL);
			valueMarker.done(ShaderLabElements.PROPERTY_VALUE);
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

	@Nullable
	@Override
	public String getDefaultInsertValue()
	{
		return "()";
	}

	@Override
	public boolean isMyValue(@NotNull ShaderLabParserBuilder builder)
	{
		return builder.getTokenType() == ShaderLabTokens.LPAR || builder.getTokenType() == ShaderLabTokens.LBRACKET;
	}
}
