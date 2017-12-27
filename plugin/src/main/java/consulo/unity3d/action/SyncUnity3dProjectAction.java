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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotations.RequiredDispatchThread;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImportUtil;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
public class SyncUnity3dProjectAction extends AnAction
{
	public SyncUnity3dProjectAction()
	{
		super(Unity3dIcons.Unity3d);
	}

	@Override
	@RequiredDispatchThread
	public void actionPerformed(@NotNull AnActionEvent anActionEvent)
	{
		final Project project = anActionEvent.getProject();
		if(project == null)
		{
			return;
		}
		final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(rootModuleExtension == null)
		{
			return;
		}

		Unity3dProjectImportUtil.syncProjectStep1(project, rootModuleExtension.getSdk(), null, true);
	}

	@RequiredDispatchThread
	@Override
	public void update(@NotNull AnActionEvent e)
	{
		Presentation presentation = e.getPresentation();
		Project project = e.getProject();
		if(project == null || Unity3dModuleExtensionUtil.getRootModuleExtension(project) == null)
		{
			presentation.setEnabledAndVisible(false);
			return;
		}

		VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
		if(virtualFile == null || !virtualFile.equals(project.getBaseDir()))
		{
			presentation.setEnabledAndVisible(false);
		}

		if(project.getUserData(Unity3dProjectImportUtil.ourInProgressFlag) == Boolean.TRUE)
		{
			presentation.setEnabled(false);
			presentation.setVisible(true);
		}
	}
}
