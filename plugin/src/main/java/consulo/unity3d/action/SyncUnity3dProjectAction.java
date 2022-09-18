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

package consulo.unity3d.action;

import consulo.annotation.component.ActionImpl;
import consulo.language.editor.CommonDataKeys;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
@ActionImpl(id = "SyncUnity3dProject")
public class SyncUnity3dProjectAction extends AnAction
{
	public SyncUnity3dProjectAction()
	{
		super(Unity3dIcons.Unity3d);
	}

	@Override
	@RequiredUIAccess
	public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
	{
		final Project project = anActionEvent.getData(Project.KEY);
		if(project == null)
		{
			return;
		}
		final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(rootModuleExtension == null)
		{
			return;
		}

		Unity3dProjectImporter.syncProjectStep(project, rootModuleExtension.getSdk(), null, true);
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		Presentation presentation = e.getPresentation();
		Project project = e.getData(Project.KEY);
		if(project == null || Unity3dModuleExtensionUtil.getRootModuleExtension(project) == null)
		{
			presentation.setEnabledAndVisible(false);
			return;
		}

		VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
		if(virtualFile == null || !virtualFile.equals(project.getBaseDir()))
		{
			presentation.setEnabledAndVisible(false);
			return;
		}

		if(project.getUserData(Unity3dProjectImporter.ourInProgressFlag) == Boolean.TRUE)
		{
			presentation.setEnabled(false);
			presentation.setVisible(true);
		}
	}
}
