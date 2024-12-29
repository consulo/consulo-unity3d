/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.run.debugger.attach;

import consulo.execution.ProgramRunnerUtil;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.debug.attach.XAttachDebugger;
import consulo.execution.debug.attach.XAttachHost;
import consulo.platform.ProcessInfo;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.unity3d.run.Unity3dAttachApplicationType;
import consulo.unity3d.run.Unity3dAttachConfiguration;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import consulo.unity3d.run.debugger.UnityProcessDialog;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09/01/2021
 */
public class UnityAttachDebugger implements XAttachDebugger
{
	public static final UnityAttachDebugger INSTANCE = new UnityAttachDebugger();

	@Nonnull
	@Override
	public String getDebuggerDisplayName()
	{
		return "Unity Debugger";
	}

	@Override
	public void attachDebugSession(@Nonnull Project project, @Nonnull XAttachHost hostInfo, @Nonnull ProcessInfo info) throws ExecutionException
	{
		UnityDebugProcessInfo unityProcess = UnityProcessDialog.tryParseIfUnityProcess(info);
		if(unityProcess == null)
		{
			throw new ExecutionException("Target not found");
		}

		ConfigurationFactory factory = Unity3dAttachApplicationType.getInstance().getConfigurationFactories()[0];

		Unity3dAttachConfiguration configuration = new Unity3dAttachConfiguration(project, unityProcess.getName(), factory);
		configuration.setForceUnityProcess(unityProcess);

		RunnerAndConfigurationSettings runSettings = RunManager.getInstance(project).createConfiguration(configuration, factory);

		ProgramRunnerUtil.executeConfiguration(runSettings, DefaultDebugExecutor.getDebugExecutorInstance());
	}
}
