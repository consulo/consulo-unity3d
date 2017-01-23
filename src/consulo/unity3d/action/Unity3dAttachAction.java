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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.util.IconUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.impl.settings.XDebuggerSettingManagerImpl;
import consulo.annotations.RequiredDispatchThread;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.run.Unity3dAttachApplicationType;
import consulo.unity3d.run.Unity3dAttachConfiguration;
import consulo.unity3d.run.Unity3dAttachRunner;
import consulo.unity3d.run.debugger.Unity3dDebuggerSettings;
import consulo.unity3d.run.debugger.UnityProcess;
import consulo.unity3d.run.debugger.UnityProcessDialog;

/**
 * @author VISTALL
 * @since 17.04.2015
 */
public class Unity3dAttachAction extends DumbAwareAction
{
	private static final Logger LOGGER = Logger.getInstance(Unity3dAttachAction.class);

	private AtomicBoolean myBusyState = new AtomicBoolean();

	public Unity3dAttachAction()
	{
		super("Attach to Unity3D process", null, Unity3dIcons.Attach);

		getTemplatePresentation().setVisible(false);
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

		Unity3dDebuggerSettings settings = XDebuggerSettingManagerImpl.getInstanceImpl().getSettings(Unity3dDebuggerSettings.class);
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

			Unity3dAttachConfiguration configuration = new Unity3dAttachConfiguration(project, Unity3dAttachApplicationType.ourDummyInstance.getConfigurationFactories()[0], firstItem);

			RunManager runManager = RunManager.getInstance(project);
			RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl((RunManagerImpl) runManager, configuration, false);
			runnerAndConfigurationSettings.setSingleton(true);

			builder.runnerAndSettings(Unity3dAttachRunner.ourDummyInstance, runnerAndConfigurationSettings);

			builder.runProfile(configuration);

			ExecutionManager.getInstance(project).restartRunProfile(builder.build());
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
		final Presentation presentation = e.getPresentation();
		final Project project = e.getProject();

		if(project == null || project.isDisposed())
		{
			presentation.setEnabledAndVisible(false);
			return;
		}

		presentation.setVisible(Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null);
		if(!presentation.isVisible())
		{
			return;
		}

		if(DumbService.getInstance(project).isDumb() || !project.isInitialized())
		{
			presentation.setIcon(getTemplatePresentation().getIcon());
			presentation.setEnabled(false);
			return;
		}

		boolean enabled = !myBusyState.get();
		e.getPresentation().setEnabled(enabled);
		if(enabled)
		{
			presentation.setIcon(getIcon(project, getTemplatePresentation().getIcon()));
		}
		else
		{
			presentation.setIcon(getTemplatePresentation().getIcon());
		}
	}

	private Icon getIcon(Project project, Icon icon)
	{
		final ExecutionManagerImpl executionManager = ExecutionManagerImpl.getInstance(project);
		List<RunContentDescriptor> runningDescriptors = executionManager.getRunningDescriptors(new Condition<RunnerAndConfigurationSettings>()
		{
			@Override
			public boolean value(RunnerAndConfigurationSettings s)
			{
				return s.getConfiguration() instanceof Unity3dAttachConfiguration;
			}
		});

		if(runningDescriptors.isEmpty())
		{
			return icon;
		}

		if(runningDescriptors.size() == 1)
		{
			return ExecutionUtil.getLiveIndicator(icon);
		}
		else
		{
			return IconUtil.addText(icon, String.valueOf(runningDescriptors.size()));
		}
	}
}
