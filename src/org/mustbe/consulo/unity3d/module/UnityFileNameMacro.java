/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.module;

import com.intellij.ide.macro.Macro;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;

/**
 * @author VISTALL
 * @since 17.11.14
 */
public class UnityFileNameMacro extends Macro
{
	@Override
	public String getName()
	{
		return "UnityFileName";
	}

	@Override
	public String getDescription()
	{
		return "Unity File Name";
	}

	@Override
	public String expand(DataContext dataContext)
	{
		final Module module = LangDataKeys.MODULE.getData(dataContext);
		if(module == null)
		{
			return null;
		}
		Unity3dModuleExtension extension = ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class);
		if(extension != null)
		{
			return extension.getFileName();
		}
		return null;
	}
}