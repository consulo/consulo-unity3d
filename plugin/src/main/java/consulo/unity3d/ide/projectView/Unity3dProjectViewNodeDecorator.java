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

package consulo.unity3d.ide.projectView;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.project.ui.view.tree.ProjectViewNode;
import consulo.project.ui.view.tree.ProjectViewNodeDecorator;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentationData;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.virtualFileSystem.VirtualFile;

/**
 * @author VISTALL
 * @since 31.07.2015
 */
@ExtensionImpl
public class Unity3dProjectViewNodeDecorator implements ProjectViewNodeDecorator
{
	@RequiredUIAccess
	@Override
	public void decorate(ProjectViewNode node, PresentationData data)
	{
		Project project = node.getProject();
		if(project == null)
		{
			return;
		}

		VirtualFile virtualFile = node.getVirtualFile();
		if(virtualFile == null || virtualFile.getFileType() != Unity3dMetaFileType.INSTANCE)
		{
			return;
		}

		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(rootModuleExtension == null)
		{
			return;
		}

		if(Unity3dMetaFileProjectViewProvider.haveOwnerFile(virtualFile))
		{
			return;
		}

		data.clearText();
		data.addText(virtualFile.getName(), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
		String nameWithoutExtension = virtualFile.getNameWithoutExtension();
		data.setTooltip("File(directory) '" + nameWithoutExtension + "' is not exists, meta file can be deleted.");
	}
}
