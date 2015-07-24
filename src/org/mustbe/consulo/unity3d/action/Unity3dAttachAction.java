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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.run.Unity3dAttachApplicationType;
import org.mustbe.consulo.unity3d.run.Unity3dAttachConfiguration;
import org.mustbe.consulo.unity3d.run.Unity3dAttachRunner;
import org.mustbe.consulo.unity3d.run.debugger.Unity3dAttachProcessHandler;
import org.mustbe.consulo.unity3d.run.debugger.Unity3dDebuggerSettings;
import org.mustbe.consulo.unity3d.run.debugger.UnityProcess;
import org.mustbe.consulo.unity3d.run.debugger.UnityProcessDialog;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.impl.settings.XDebuggerSettingsManager;

/**
 * @author VISTALL
 * @since 17.04.2015
 */
@Logger
public class Unity3dAttachAction extends AnAction
{
	private AtomicBoolean myBusyState = new AtomicBoolean();

	public Unity3dAttachAction()
	{
		super(Unity3dIcons.Attach);
	}

	@RequiredDispatchThread
	@Override
	public void actionPerformed(final AnActionEvent anActionEvent)
	{
		if(myBusyState.get())
		{
			return;
		}

		final Project project = anActionEvent.getProject();
		if(project == null)
		{
			return;
		}

		myBusyState.set(true);

		Unity3dDebuggerSettings settings = XDebuggerSettingsManager.getInstanceImpl().getSettings(Unity3dDebuggerSettings.class);
		if(settings.myAttachToSingleProcessWithoutDialog)
		{
			new Task.Backgroundable(project, "Searching Unity3D process", false)
			{
				@Override
				public void run(@NotNull ProgressIndicator indicator)
				{
					final UnityProcess processToAttach;

					List<UnityProcess> unityProcesses = UnityProcessDialog.collectItems();
					if(unityProcesses.size() == 1)
					{
						processToAttach = ContainerUtil.getFirstItem(unityProcesses);
					}
					else
					{
						processToAttach = null;
					}

					UIUtil.invokeLaterIfNeeded(new Runnable()
					{
						@Override
						public void run()
						{
							executeOrShowDialog(project, processToAttach);
						}
					});
				}
			}.queue();
		}
		else
		{
			executeOrShowDialog(project, null);
		}
	}

	@RequiredDispatchThread
	private void executeOrShowDialog(@NotNull Project project, @Nullable UnityProcess firstItem)
	{
		try
		{
			if(firstItem == null)
			{
				UnityProcessDialog dialog = new UnityProcessDialog(project);

				List<UnityProcess> unityProcesses = dialog.showAndGetResult();

				firstItem = ContainerUtil.getFirstItem(unityProcesses);
				if(firstItem == null)
				{
					return;
				}
			}

			ExecutionEnvironmentBuilder builder = new ExecutionEnvironmentBuilder(project, DefaultDebugExecutor.getDebugExecutorInstance());

			Unity3dAttachConfiguration configuration = new Unity3dAttachConfiguration(project, Unity3dAttachApplicationType.ourDummyInstance
					.getConfigurationFactories()[0], firstItem);

			RunManager runManager = RunManager.getInstance(project);
			RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl((RunManagerImpl) runManager,
					configuration, false)
			{
				@Override
				public boolean isEquivalentTo(@NotNull RunnerAndConfigurationSettings obj)
				{
					RunConfiguration conf1 = getConfiguration();
					RunConfiguration conf2 = obj.getConfiguration();
					return conf1 instanceof Unity3dAttachConfiguration && conf2 instanceof Unity3dAttachConfiguration && (
							(Unity3dAttachConfiguration) conf1).getUnityProcess().hashCode() == ((Unity3dAttachConfiguration) conf2).getUnityProcess
							().hashCode();
				}
			};
			runnerAndConfigurationSettings.setSingleton(true);

			builder.runnerAndSettings(Unity3dAttachRunner.ourDummyInstance, runnerAndConfigurationSettings);

			builder.runProfile(configuration);

			ProcessHandler[] runningProcesses = ExecutionManager.getInstance(project).getRunningProcesses();
			for(ProcessHandler runningProcess : runningProcesses)
			{
				if(runningProcess instanceof Unity3dAttachProcessHandler && !runningProcess.isProcessTerminated())
				{
					ExecutionManager.getInstance(project).restartRunProfile(builder.build());
					return;
				}
			}

			builder.buildAndExecute();
		}
		catch(ExecutionException e)
		{
			LOGGER.error(e);
		}
		finally
		{
			myBusyState.set(false);
		}
	}

	@RequiredDispatchThread
	@Override
	public void update(AnActionEvent e)
	{
		e.getPresentation().setEnabled(!myBusyState.get());
	}
}
