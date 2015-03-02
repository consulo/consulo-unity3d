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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 02.03.2015
 */
public class Unity3dMetaFileProjectViewProvider implements TreeStructureProvider, DumbAware
{
	private final Project myProject;

	public Unity3dMetaFileProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Override
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		List<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>(children.size());
		for(AbstractTreeNode child : children)
		{
			if(child instanceof ProjectViewNode)
			{
				VirtualFile virtualFile = ((ProjectViewNode)child).getVirtualFile();
				if(virtualFile != null && virtualFile.getFileType() == Unity3dMetaFileType.INSTANCE)
				{
					Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, myProject);
					if(moduleForFile != null && ModuleUtilCore.getExtension(moduleForFile, Unity3dModuleExtension.class) != null)
					{
						continue;
					}
				}
			}

			nodes.add(child);
		}
		return nodes;
	}

	@Nullable
	@Override
	public Object getData(Collection<AbstractTreeNode> selected, String dataName)
	{
		return null;
	}
}
