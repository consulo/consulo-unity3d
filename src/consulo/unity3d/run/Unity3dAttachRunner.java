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

package consulo.unity3d.run;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import consulo.annotations.RequiredDispatchThread;
import consulo.dotnet.execution.DebugConnectionInfo;
import consulo.dotnet.mono.debugger.MonoVirtualMachineListener;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.run.debugger.UnityDebugProcess;
import consulo.unity3d.run.debugger.UnityProcess;
import consulo.unity3d.run.debugger.UnityProcessDialog;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachRunner extends DefaultProgramRunner
{
	public static Unity3dAttachRunner getInstance()
	{
		return ProgramRunner.PROGRAM_RUNNER_EP.findExtension(Unity3dAttachRunner.class);
	}

	@NotNull
	@Override
	public String getRunnerId()
	{
		return "Unity3dAttachRunner";
	}

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile)
	{
		return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof Unity3dAttachConfiguration;
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull final ExecutionEnvironment environment) throws ExecutionException
	{
		Unity3dAttachConfiguration runProfile = (Unity3dAttachConfiguration) environment.getRunProfile();

		UnityProcess process = null;
		switch(runProfile.getAttachTarget())
		{
			case UNITY_EDITOR:
				process = UnityEditorCommunication.findEditorProcess();
				break;
			case BY_NAME:
				for(UnityProcess unityProcess : UnityProcessDialog.collectItems())
				{
					if(StringUtil.isEmpty(runProfile.getProcessName()) || Comparing.equal(unityProcess.getName(), runProfile.getProcessName()))
					{
						process = unityProcess;
						break;
					}
				}
				break;
			case FROM_DIALOG:
				UnityProcessDialog dialog = new UnityProcessDialog(environment.getProject());

				List<UnityProcess> unityProcesses = dialog.showAndGetResult();

				process = ContainerUtil.getFirstItem(unityProcesses);
				if(process == null)
				{
					return null;
				}
				break;
		}

		if(process == null)
		{
			throw new ExecutionException("Process not find for attach");
		}
		ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
		return runContentDescriptor(executionResult, environment, process, null);
	}

	@NotNull
	@RequiredDispatchThread
	public static RunContentDescriptor runContentDescriptor(ExecutionResult executionResult,
			@NotNull final ExecutionEnvironment environment,
			@NotNull UnityProcess selected,
			@Nullable final ConsoleView consoleView) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();
		final XDebugSession debugSession = XDebuggerManager.getInstance(environment.getProject()).startSession(environment, session ->
		{
			DebugConnectionInfo debugConnectionInfo = new DebugConnectionInfo(selected.getHost(), selected.getPort(), true);
			final UnityDebugProcess process = new UnityDebugProcess(session, environment.getRunProfile(), debugConnectionInfo, consoleView);
			process.setExecutionResult(executionResult);

			process.getDebugThread().addListener(new MonoVirtualMachineListener()
			{
				@Override
				public void connectionSuccess(@NotNull VirtualMachine machine)
				{
					ProcessHandler processHandler = process.getProcessHandler();
					processHandler.notifyTextAvailable(String.format("Success attach to '%s' at %s:%d", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDOUT);
				}

				@Override
				public void connectionStopped()
				{
				}

				@Override
				public void connectionFailed()
				{
					ProcessHandler processHandler = process.getProcessHandler();
					processHandler.notifyTextAvailable(String.format("Failed attach to '%s' at %s:%d", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDERR);
					StopProcessAction.stopProcess(processHandler);
				}
			});
			process.start();
			return process;
		});

		return debugSession.getRunContentDescriptor();
	}
}
