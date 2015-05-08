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

package org.mustbe.consulo.unity3d.shaderlab.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public enum ShaderLabPropertyType
{
	Float,
	Range,
	Color,
	Vector,
	Int,
	Cube,
	_2D,
	_3D;

	@NotNull
	public String getPresentableName()
	{
		String name = name();
		if(name.charAt(0) == '_')
		{
			name = name.substring(1, name.length());
		}
		return name;
	}

	@Nullable
	public static ShaderLabPropertyType find(@NotNull String value)
	{
		for(ShaderLabPropertyType shaderLabPropertyType : values())
		{
			String name = shaderLabPropertyType.getPresentableName();

			if(value.equalsIgnoreCase(name))
			{
				return shaderLabPropertyType;
			}
		}
		return null;
	}
}
