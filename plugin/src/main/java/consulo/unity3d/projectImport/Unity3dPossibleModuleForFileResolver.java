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

package consulo.unity3d.projectImport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.roots.ContentEntryFileListener;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 06.04.2015
 */
public class Unity3dPossibleModuleForFileResolver implements ContentEntryFileListener.PossibleModuleForFileResolver
{
	@Nullable
	@Override
	@RequiredReadAction
	public Module resolve(@NotNull Project project, @NotNull VirtualFile virtualFile)
	{
		Module module = resolveImpl(project, virtualFile);
		return module != null && module.getModuleDirUrl() != null ? null : module;
	}

	@RequiredReadAction
	@Nullable
	private Module resolveImpl(Project project, VirtualFile virtualFile)
	{
		if(virtualFile.getFileType() == CSharpFileType.INSTANCE)
		{
			return findModule(project, virtualFile, "CSharp");
		}
		else if(virtualFile.getFileType() == JavaScriptFileType.INSTANCE)
		{
			return findModule(project, virtualFile, "UnityScript");
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	private Module findModule(Project project, VirtualFile virtualFile, String modulePrefix)
	{
		Module module = Unity3dModuleExtensionUtil.getRootModule(project);
		if(module == null)
		{
			return null;
		}

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;
		VirtualFile assetsDir = baseDir.findChild("Assets");
		if(assetsDir == null)
		{
			return null;
		}

		if(!VfsUtil.isAncestor(assetsDir, virtualFile, true))
		{
			return null;
		}

		VirtualFile parent = virtualFile.getParent();
		while(!assetsDir.equals(parent))
		{
			if(parent.getName().equals("Editor"))
			{
				return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix + "-Editor");
			}
			parent = parent.getParent();
		}

		String[] paths = new String[]{
				"Assets/Standard Assets",
				"Assets/Pro Standard Assets",
				"Assets/Plugins"
		};

		for(String path : paths)
		{
			VirtualFile pathFile = baseDir.findFileByRelativePath(path);
			if(pathFile != null && VfsUtil.isAncestor(pathFile, virtualFile, true))
			{
				return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix + "-firstpass");
			}
		}
		return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix);
	}
}
