/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.ide.projectView;

import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

/**
 * @author VISTALL
 * @since 31.07.2015
 */
public class Unity3dProjectViewNodeDecorator implements ProjectViewNodeDecorator
{
	@RequiredDispatchThread
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

	@Override
	public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer)
	{

	}
}
