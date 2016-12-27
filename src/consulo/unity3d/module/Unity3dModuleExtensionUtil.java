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

package consulo.unity3d.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 29.03.2015
 */
public class Unity3dModuleExtensionUtil
{
	@Nullable
	@RequiredReadAction
	public static Module getRootModule(@NotNull Project project)
	{
		ModuleManager moduleManager = ModuleManager.getInstance(project);
		for(Module module : moduleManager.getModules())
		{
			VirtualFile baseDir = project.getBaseDir();
			if(baseDir == null)
			{
				continue;
			}

			if(baseDir.equals(module.getModuleDir()))
			{
				return module;
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static Unity3dRootModuleExtension getRootModuleExtension(@NotNull Project project)
	{
		Module rootModule = getRootModule(project);
		if(rootModule == null)
		{
			return null;
		}
		return ModuleUtilCore.getExtension(rootModule, Unity3dRootModuleExtension.class);
	}
}
