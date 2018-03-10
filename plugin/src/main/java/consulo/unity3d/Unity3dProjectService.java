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

package consulo.unity3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.ClearableLazyValue;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.module.Unity3dRootModuleExtension;

/**
 * @author VISTALL
 * @since 18-Jun-17
 */
public class Unity3dProjectService implements Disposable
{
	private static class CacheValue
	{
		private Module myRootModule;
		private Unity3dRootModuleExtension myRootModuleExtension;

		private CacheValue(Module rootModule, Unity3dRootModuleExtension rootModuleExtension)
		{
			myRootModule = rootModule;
			myRootModuleExtension = rootModuleExtension;
		}
	}

	@Nonnull
	public static Unity3dProjectService getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, Unity3dProjectService.class);
	}

	private ClearableLazyValue<CacheValue> myValue = new ClearableLazyValue<CacheValue>()
	{
		@Nonnull
		@Override
		@RequiredReadAction
		protected CacheValue compute()
		{
			Module rootModule = findRootModuleImpl(myProject);
			return new CacheValue(rootModule, rootModule == null ? null : ModuleUtilCore.getExtension(rootModule, Unity3dRootModuleExtension.class));
		}
	};

	@Nullable
	@RequiredReadAction
	private static Module findRootModuleImpl(@Nonnull Project project)
	{
		VirtualFile baseDir = project.getBaseDir();
		if(baseDir == null)
		{
			return null;
		}

		ModuleManager moduleManager = ModuleManager.getInstance(project);
		for(Module module : moduleManager.getModules())
		{
			if(baseDir.equals(module.getModuleDir()))
			{
				return module;
			}
		}
		return null;
	}

	private final Project myProject;

	Unity3dProjectService(Project project)
	{
		myProject = project;
		project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener()
		{
			@Override
			public void rootsChanged(ModuleRootEvent event)
			{
				myValue.drop();
			}
		});
	}

	@Nullable
	@RequiredReadAction
	public Module getRootModule()
	{
		return myValue.getValue().myRootModule;
	}

	@Nullable
	@RequiredReadAction
	public Unity3dRootModuleExtension getRootModuleExtension()
	{
		return myValue.getValue().myRootModuleExtension;
	}

	@Override
	public void dispose()
	{
		myValue.drop();
	}
}
