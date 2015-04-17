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

package org.mustbe.consulo.unity3d.action;

import org.consulo.lombok.annotations.Logger;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.run.Unity3dAttachApplicationType;
import org.mustbe.consulo.unity3d.run.Unity3dAttachConfiguration;
import org.mustbe.consulo.unity3d.run.Unity3dAttachRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 17.04.2015
 */
@Logger
public class Unity3dAttachAction extends AnAction
{
	public Unity3dAttachAction()
	{
		super(Unity3dIcons.Attach);
	}

	@Override
	public void update(AnActionEvent e)
	{
		super.update(e);
		e.getPresentation().setEnabled(!Unity3dAttachRunner.ourDummyInstance.isRunning());
	}

	@Override
	public void actionPerformed(final AnActionEvent anActionEvent)
	{
		final Project project = anActionEvent.getProject();
		if(project == null)
		{
			return;
		}

		ExecutionEnvironmentBuilder builder = new ExecutionEnvironmentBuilder(project, DefaultDebugExecutor.getDebugExecutorInstance());
		builder.runner(Unity3dAttachRunner.ourDummyInstance);
		builder.runProfile(new Unity3dAttachConfiguration(project, Unity3dAttachApplicationType.ourDummyInstance.getConfigurationFactories()[0],
				"Unity3D Attach"));

		try
		{
			builder.buildAndExecute();
		}
		catch(ExecutionException e)
		{
			LOGGER.error(e);
		}
	}
}
