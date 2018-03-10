/*
 * Copyright 2013-2017 consulo.io
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 25-Oct-17
 */
public class ShaderLabRoleHolder
{
	private static final Map<String, ShaderLabRole> ourRoles = new HashMap<>();

	@Nullable
	public static ShaderLabRole findRole(String name)
	{
		name = name.toLowerCase(Locale.US);
		return ourRoles.get(name);
	}

	public static void build()
	{
		Field[] declaredFields = ShaderLabRoles.class.getDeclaredFields();
		for(Field declaredField : declaredFields)
		{
			if(Modifier.isStatic(declaredField.getModifiers()))
			{
				try
				{
					ShaderLabRole value = (ShaderLabRole) declaredField.get(null);
					value.setName(declaredField.getName());

					ourRoles.put(declaredField.getName().toLowerCase(Locale.US), value);
				}
				catch(IllegalAccessException e)
				{
					throw new Error(e);
				}
			}
		}
	}
}
