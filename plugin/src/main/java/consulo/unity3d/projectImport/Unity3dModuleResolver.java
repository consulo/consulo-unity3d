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

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06.04.2015
 */
public class Unity3dModuleResolver implements CreateFileFromTemplateAction.ModuleResolver
{
	@Nullable
	@Override
	@RequiredReadAction
	public Module resolveModule(@Nonnull PsiDirectory psiDirectory, @Nonnull FileType fileType)
	{
		Module module = resolveModuleImpl(psiDirectory, fileType);
		return module != null && module.getModuleDirUrl() != null ? null : module;
	}

	@Nullable
	@RequiredReadAction
	public Module resolveModuleImpl(@Nonnull PsiDirectory psiDirectory, @Nonnull FileType fileType)
	{
		if(fileType == CSharpFileType.INSTANCE)
		{
			return findModule(psiDirectory, "CSharp");
		}
		else if(fileType == JavaScriptFileType.INSTANCE)
		{
			return findModule(psiDirectory, "UnityScript");
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	private Module findModule(PsiDirectory directory, String modulePrefix)
	{
		Project project = directory.getProject();
		VirtualFile parent = directory.getVirtualFile();

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

		if(!VfsUtil.isAncestor(assetsDir, parent, true))
		{
			return null;
		}

		while(!assetsDir.equals(parent))
		{
			if(parent.getName().equals("Editor"))
			{
				return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix + "-Editor");
			}
			parent = parent.getParent();
		}

		for(String path : AssemblyCSharpFirstPass.FIRST_PASS_PATHS)
		{
			VirtualFile pathFile = baseDir.findFileByRelativePath(path);
			if(pathFile != null && VfsUtil.isAncestor(pathFile, parent, true))
			{
				return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix + "-firstpass");
			}
		}
		return ModuleManager.getInstance(project).findModuleByName("Assembly-" + modulePrefix);
	}
}
