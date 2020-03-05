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

package consulo.unity3d.unityscript.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;


import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeUi;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptFileProjectViewProvider implements TreeStructureProvider, DumbAware
{
	private final Project myProject;

	@Inject
	public UnityScriptFileProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredUIAccess
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		return AbstractTreeUi.calculateYieldingToWriteAction(() -> doModify(children, settings));
	}

	@RequiredReadAction
	@Nonnull
	private Collection<AbstractTreeNode> doModify(Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
		if(rootModuleExtension == null)
		{
			return children;
		}

		List<AbstractTreeNode> nodes = new ArrayList<>(children.size());
		for(AbstractTreeNode child : children)
		{
			ProgressManager.checkCanceled();

			Object value = child.getValue();
			if(value instanceof JSFile && ((JSFile) value).getFileType() == JavaScriptFileType.INSTANCE)
			{
				Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement((PsiElement) value);
				if(moduleForPsiElement != null)
				{
					nodes.add(new UnityScriptFileNode(myProject, (PsiFile) value, settings));
					continue;
				}
			}
			nodes.add(child);
		}
		return nodes;
	}
}
