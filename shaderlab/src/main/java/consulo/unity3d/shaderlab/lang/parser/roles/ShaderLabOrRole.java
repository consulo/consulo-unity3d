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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.PsiBuilder;
import com.intellij.util.ArrayUtilRt;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabElements;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabOrRole extends ShaderLabValueRole
{
	private String myDefaultInsertValue;
	private ShaderLabRole[] myRoles;

	public ShaderLabOrRole(ShaderLabRole... roles)
	{
		this(null, roles);
	}

	public ShaderLabOrRole(String defaultInsertValue, ShaderLabRole... roles)
	{
		myDefaultInsertValue = defaultInsertValue;
		myRoles = roles;
	}

	@NotNull
	@Override
	public String[] getValues()
	{
		List<String> result = new ArrayList<>();
		for(ShaderLabRole role : myRoles)
		{
			Collections.addAll(result, role.getValues());
		}
		return ArrayUtilRt.toStringArray(result);
	}

	@Nullable
	@Override
	public String getDefaultInsertValue()
	{
		String[] values = getValues();
		if(values.length > 0)
		{
			return values[0];
		}
		return myDefaultInsertValue;
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

	@Override
	public boolean isMyValue(@NotNull ShaderLabParserBuilder builder)
	{
		for(ShaderLabRole role : myRoles)
		{
			if(role instanceof ShaderLabValueRole && ((ShaderLabValueRole) role).isMyValue(builder))
			{
				return true;
			}
		}
		return false;
	}
}
