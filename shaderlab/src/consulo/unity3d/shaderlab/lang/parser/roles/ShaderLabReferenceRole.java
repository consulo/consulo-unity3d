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

package consulo.unity3d.shaderlab.lang.parser.roles;

import org.jetbrains.annotations.NotNull;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParser;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import com.intellij.lang.PsiBuilder;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabReferenceRole extends ShaderLabValueRole
{
	public static final ShaderLabReferenceRole INSTANCE = new ShaderLabReferenceRole();

	@Override
	public boolean isMyValue(@NotNull ShaderLabParserBuilder builder)
	{
		return builder.getTokenType() == ShaderLabTokens.LBRACKET;
	}

	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark)
	{
		ShaderLabParser.parseBracketReference(builder);
		mark.done(ShaderLabElements.SIMPLE_VALUE);
		return mark;
	}
}
