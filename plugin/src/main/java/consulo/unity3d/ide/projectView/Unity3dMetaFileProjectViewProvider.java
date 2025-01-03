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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.application.progress.ProgressManager;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.project.ui.view.tree.ProjectViewNode;
import consulo.project.ui.view.tree.TreeStructureProvider;
import consulo.project.ui.view.tree.ViewSettings;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.tree.TreeHelper;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 02.03.2015
 */
@ExtensionImpl
public class Unity3dMetaFileProjectViewProvider implements TreeStructureProvider, DumbAware
{
	private final Project myProject;

	@Inject
	public Unity3dMetaFileProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredUIAccess
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		return TreeHelper.calculateYieldingToWriteAction(() -> doModify(children, settings));
	}

	@Nonnull
	@RequiredReadAction
	private Collection<AbstractTreeNode> doModify(Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
		if(rootModuleExtension == null)
		{
			return children;
		}

		Boolean showMetaFiles = settings.getViewOption(Unity3dShowMetaFileProjectViewPaneOptionProvider.KEY);
		if(showMetaFiles == Boolean.TRUE)
		{
			return children;
		}

		List<AbstractTreeNode> nodes = new ArrayList<>(children.size());
		for(AbstractTreeNode child : children)
		{
			ProgressManager.checkCanceled();

			if(child instanceof ProjectViewNode)
			{
				VirtualFile virtualFile = ((ProjectViewNode) child).getVirtualFile();
				if(virtualFile != null && virtualFile.getFileType() == Unity3dMetaFileType.INSTANCE && haveOwnerFile(virtualFile))
				{
					continue;
				}
			}

			nodes.add(child);
		}
		return nodes;
	}

	public static boolean haveOwnerFile(VirtualFile virtualFile)
	{
		String nameWithoutExtension = virtualFile.getNameWithoutExtension();
		VirtualFile parent = virtualFile.getParent();
		return parent.findChild(nameWithoutExtension) != null;
	}
}
