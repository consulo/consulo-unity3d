/*
 * Copyright 2013-2019 consulo.io
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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.unity3d.icon.Unity3dIconGroup;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.util.collection.HashingStrategy;
import consulo.util.collection.Maps;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2019-07-20
 */
@ExtensionImpl
public class UnityDirectoryIconDescriptorUpdater implements IconDescriptorUpdater
{
	private final Map<String, Image> myFolderIcons = Maps.newHashMap(HashingStrategy.CaseInsensitiveStringHashingStrategy.INSTANCE);

	public UnityDirectoryIconDescriptorUpdater()
	{
		myFolderIcons.put("Editor", Unity3dIconGroup.editfolder());
		myFolderIcons.put("Plugins", Unity3dIconGroup.pluginsfolder());
		myFolderIcons.put("Pro Standard Assets", Unity3dIconGroup.assetsfolder());
		myFolderIcons.put("Standard Assets", Unity3dIconGroup.assetsfolder());
	}

	@RequiredReadAction
	@Override
	public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement psiElement, int flags)
	{
		if(!(psiElement instanceof PsiDirectory))
		{
			return;
		}

		String name = ((PsiDirectory) psiElement).getName();
		Image image = myFolderIcons.get(name);
		if(image == null)
		{
			return;
		}

		Project project = psiElement.getProject();
		Module module = Unity3dModuleExtensionUtil.getRootModule(project);
		if(module == null)
		{
			return;
		}

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;
		VirtualFile assetsDirectory = baseDir.findChild(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDirectory == null)
		{
			return;
		}

		if(!VirtualFileUtil.isAncestor(assetsDirectory, ((PsiDirectory) psiElement).getVirtualFile(), false))
		{
			return;
		}

		iconDescriptor.setMainIcon(image);
	}
}
