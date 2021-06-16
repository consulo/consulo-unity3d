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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.text.CaseInsensitiveStringHashingStrategy;
import consulo.annotation.access.RequiredReadAction;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import consulo.ui.image.Image;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.util.collection.Maps;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2019-07-20
 */
public class UnityDirectoryIconDescriptorUpdater implements IconDescriptorUpdater
{
	private final Map<String, Image> myFolderIcons = Maps.newHashMap(CaseInsensitiveStringHashingStrategy.INSTANCE);

	public UnityDirectoryIconDescriptorUpdater()
	{
		myFolderIcons.put("Editor", Unity3dIcons.EditorLayer);
		myFolderIcons.put("Plugins", Unity3dIcons.PluginsLayer);
		myFolderIcons.put("Pro Standard Assets", Unity3dIcons.AssetsLayer);
		myFolderIcons.put("Standard Assets", Unity3dIcons.AssetsLayer);
		myFolderIcons.put("Gizmos", Unity3dIcons.GizmosLayer);
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

		if(!VfsUtil.isAncestor(assetsDirectory, ((PsiDirectory) psiElement).getVirtualFile(), false))
		{
			return;
		}

		iconDescriptor.addLayerIcon(image);
	}
}
