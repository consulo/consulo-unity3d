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

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.language.psi.PsiDirectory;
import consulo.project.Project;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26.10.2015
 */
public class UnityNamespaceGeneratePolicy extends DotNetNamespaceGeneratePolicy
{
	public static final UnityNamespaceGeneratePolicy INSTANCE = new UnityNamespaceGeneratePolicy(null);

	public static UnityNamespaceGeneratePolicy createOrGet(@Nonnull Unity3dRootModuleExtension rootModuleExtension)
	{
		String namespacePrefix = rootModuleExtension.getNamespacePrefix();
		if(namespacePrefix != null)
		{
			return new UnityNamespaceGeneratePolicy(namespacePrefix);
		}
		return INSTANCE;
	}

	@Nullable
	private String myNamespacePrefix;

	public UnityNamespaceGeneratePolicy(@Nullable String namespacePrefix)
	{
		myNamespacePrefix = namespacePrefix;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String calculateDirtyNamespace(@Nonnull PsiDirectory psiDirectory)
	{
		Project project = psiDirectory.getProject();
		VirtualFile baseDir = project.getBaseDir();
		if(baseDir == null)
		{
			return myNamespacePrefix;
		}

		VirtualFile currentDirectory = psiDirectory.getVirtualFile();

		VirtualFile assetsDirectory = baseDir.findChild(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDirectory != null)
		{
			VirtualFile targetDirectory = assetsDirectory;

			VirtualFile temp = currentDirectory;
			while(temp != null && !temp.equals(targetDirectory))
			{
				if("Editor".equals(temp.getName()))
				{
					targetDirectory = temp;
				}
				temp = temp.getParent();
			}

			// if path is not changed
			if(targetDirectory.equals(assetsDirectory))
			{
				temp = currentDirectory;
				while(temp != null && !temp.equals(targetDirectory))
				{
					if("Scripts".equals(temp.getName()))
					{
						targetDirectory = temp;
					}
					temp = temp.getParent();
				}
			}

			// if path is not changed
			if(targetDirectory.equals(assetsDirectory))
			{
				for(String path : AssemblyCSharpFirstPass.FIRST_PASS_PATHS)
				{
					VirtualFile child = baseDir.findFileByRelativePath(path);
					if(child != null && VirtualFileUtil.isAncestor(child, currentDirectory, false))
					{
						targetDirectory = child;
						break;
					}
				}
			}

			String relativePath = VirtualFileUtil.getRelativePath(currentDirectory, targetDirectory, '.');
			if(relativePath != null)
			{
				if(!StringUtil.isEmpty(myNamespacePrefix))
				{
					if(StringUtil.isEmpty(relativePath))
					{
						return myNamespacePrefix;
					}
					return myNamespacePrefix + "." + relativePath;
				}
				return relativePath;
			}
		}
		return myNamespacePrefix;
	}
}
