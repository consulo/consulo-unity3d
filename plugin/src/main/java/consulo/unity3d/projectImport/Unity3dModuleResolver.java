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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.javascript.language.JavaScriptFileType;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.NewFileModuleResolver;
import consulo.project.Project;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06.04.2015
 */
@ExtensionImpl
public class Unity3dModuleResolver implements NewFileModuleResolver
{
	private final Project myProject;

	@Inject
	public Unity3dModuleResolver(Project project)
	{
		myProject = project;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Module resolveModule( @Nonnull VirtualFile dir, @Nonnull FileType fileType)
	{
		Module module = resolveModuleImpl(dir, fileType);
		return module != null && module.getModuleDirUrl() != null ? null : module;
	}

	@Nullable
	@RequiredReadAction
	public Module resolveModuleImpl(@Nonnull VirtualFile file, @Nonnull FileType fileType)
	{
		if(fileType == CSharpFileType.INSTANCE)
		{
			return findModule(file, "CSharp");
		}
		else if(fileType == JavaScriptFileType.INSTANCE)
		{
			return findModule(file, "UnityScript");
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	private Module findModule(VirtualFile parent, String modulePrefix)
	{
		Module module = Unity3dModuleExtensionUtil.getRootModule(myProject);
		if(module == null)
		{
			return null;
		}

		VirtualFile baseDir = myProject.getBaseDir();
		assert baseDir != null;
		VirtualFile assetsDir = baseDir.findChild("Assets");
		if(assetsDir == null)
		{
			return null;
		}

		if(!VirtualFileUtil.isAncestor(assetsDir, parent, true))
		{
			return null;
		}

		while(!assetsDir.equals(parent))
		{
			if(parent.getName().equals("Editor"))
			{
				return ModuleManager.getInstance(myProject).findModuleByName("Assembly-" + modulePrefix + "-Editor");
			}
			parent = parent.getParent();
		}

		for(String path : AssemblyCSharpFirstPass.FIRST_PASS_PATHS)
		{
			VirtualFile pathFile = baseDir.findFileByRelativePath(path);
			if(pathFile != null && VirtualFileUtil.isAncestor(pathFile, parent, true))
			{
				return ModuleManager.getInstance(myProject).findModuleByName("Assembly-" + modulePrefix + "-firstpass");
			}
		}
		return ModuleManager.getInstance(myProject).findModuleByName("Assembly-" + modulePrefix);
	}
}
