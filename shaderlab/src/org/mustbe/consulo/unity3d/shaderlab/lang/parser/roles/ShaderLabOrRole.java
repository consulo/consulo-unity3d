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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;
import com.intellij.lang.PsiBuilder;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabOrRole extends ShaderLabRole
{
	private ShaderLabRole[] myRoles;

	public ShaderLabOrRole(ShaderLabRole... roles)
	{
		myRoles = roles;
	}


	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, @NotNull PsiBuilder.Marker mark)
	{
		for(ShaderLabRole role : myRoles)
		{
			if(role instanceof ShaderLabValueRole && ((ShaderLabValueRole) role).isMyValue(builder))
			{
				return role.parseAndDone(builder, mark);
			}
		}

		doneWithErrorSafe(builder, "Wrong value");
		mark.done(ShaderLabElements.SIMPLE_VALUE);

		return mark;
	}
}
