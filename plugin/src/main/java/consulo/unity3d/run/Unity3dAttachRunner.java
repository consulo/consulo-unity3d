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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import consulo.dotnet.execution.DebugConnectionInfo;
import consulo.dotnet.mono.debugger.MonoVirtualMachineListener;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.run.debugger.UnityDebugProcess;
import consulo.unity3d.run.debugger.UnityPlayerService;
import consulo.unity3d.run.debugger.UnityProcess;
import consulo.unity3d.run.debugger.UnityProcessDialog;
import mono.debugger.VirtualMachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachRunner extends AsyncProgramRunner
{
	public static Unity3dAttachRunner getInstance()
	{
		return ProgramRunner.PROGRAM_RUNNER_EP.findExtension(Unity3dAttachRunner.class);
	}

	@Nonnull
	@Override
	public String getRunnerId()
	{
		return "Unity3dAttachRunner";
	}

	@Override
	public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile)
	{
		return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof Unity3dAttachConfiguration;
	}

	@Nonnull
	@RequiredUIAccess
	public static RunContentDescriptor runContentDescriptor(ExecutionResult executionResult,
															@Nonnull final ExecutionEnvironment environment,
															@Nonnull UnityProcess selected,
															@Nullable final ConsoleView consoleView,
															boolean insideEditor) throws ExecutionException
	{
		final XDebugSession debugSession = XDebuggerManager.getInstance(environment.getProject()).startSession(environment, session ->
		{
			DebugConnectionInfo debugConnectionInfo = new DebugConnectionInfo(selected.getHost(), selected.getPort(), true);
			final UnityDebugProcess process = new UnityDebugProcess(session, environment.getRunProfile(), debugConnectionInfo, consoleView, insideEditor);
			process.setExecutionResult(executionResult);

			process.getDebugThread().addListener(new MonoVirtualMachineListener()
			{
				private boolean myDisconnected = false;

				@Override
				public void connectionSuccess(@Nonnull VirtualMachine machine)
				{
					ProcessHandler processHandler = process.getProcessHandler();
					processHandler.notifyTextAvailable(String.format("Success attach to '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDOUT);
				}

				@Override
				public void connectionStopped()
				{
					if(myDisconnected)
					{
						return;
					}

					myDisconnected = true;
					ProcessHandler processHandler = process.getProcessHandler();
					processHandler.notifyTextAvailable(String.format("Disconnected from '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDERR);
					StopProcessAction.stopProcess(processHandler);
				}

				@Override
				public void connectionFailed()
				{
					ProcessHandler processHandler = process.getProcessHandler();
					processHandler.notifyTextAvailable(String.format("Failed attach to '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDERR);
					StopProcessAction.stopProcess(processHandler);
				}
			});
			process.start();
			return process;
		});

		return debugSession.getRunContentDescriptor();
	}

	@Nonnull
	@Override
	@RequiredUIAccess
	protected AsyncResult<RunContentDescriptor> execute(@Nonnull ExecutionEnvironment environment, @Nonnull RunProfileState state) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();

		AsyncResult<RunContentDescriptor> result = new AsyncResult<>();

		Unity3dAttachConfiguration runProfile = (Unity3dAttachConfiguration) environment.getRunProfile();

		ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

		switch(runProfile.getAttachTarget())
		{
			case UNITY_EDITOR:
				new Task.Backgroundable(environment.getProject(), "Searching Unity Editor...", false)
				{
					private UnityProcess myUnityProcess;

					@Override
					public void run(@Nonnull ProgressIndicator progressIndicator)
					{
						myUnityProcess = UnityEditorCommunication.findEditorProcess();
					}

					@RequiredUIAccess
					@Override
					public void onSuccess()
					{
						if(myUnityProcess != null)
						{
							try
							{
								result.setDone(runContentDescriptor(executionResult, environment, myUnityProcess, null, true));
							}
							catch(ExecutionException e)
							{
								result.rejectWithThrowable(e);
							}
						}
						else
						{
							result.rejectWithThrowable(new ExecutionException("Unity Editor is not running"));
						}
					}
				}.queue();
				return result;
			case BY_NAME:
				UnityPlayerService.getInstance().bindAndRun(environment.getProject(), () ->
				{
					new Task.Backgroundable(environment.getProject(), "Searching process by name: " + runProfile.getProcessName())
					{
						private UnityProcess myUnityProcess;

						@Override
						public void run(@Nonnull ProgressIndicator progressIndicator)
						{
							for(UnityProcess unityProcess : UnityProcessDialog.collectItems())
							{
								if(StringUtil.isEmpty(runProfile.getProcessName()) || Comparing.equal(unityProcess.getName(), runProfile.getProcessName()))
								{
									myUnityProcess = unityProcess;
									break;
								}
							}
						}

						@RequiredUIAccess
						@Override
						public void onFinished()
						{
							setRunDescriptor(result, environment, executionResult, myUnityProcess);
						}
					}.queue();
				});
				break;
			case FROM_DIALOG:
				UnityPlayerService.getInstance().bindAndRun(environment.getProject(), () ->
				{
					UnityProcessDialog dialog = new UnityProcessDialog(environment.getProject());

					List<UnityProcess> unityProcesses = dialog.showAndGetResult();

					UnityProcess process = ContainerUtil.getFirstItem(unityProcesses);
					if(process == null)
					{
						result.setDone(null);
						return;
					}
					setRunDescriptor(result, environment, executionResult, process);
				});
				break;
		}
		return result;
	}

	@RequiredUIAccess
	private static void setRunDescriptor(AsyncResult<RunContentDescriptor> result,
										 ExecutionEnvironment environment,
										 ExecutionResult executionResult,
										 @Nullable UnityProcess process)
	{
		if(process == null)
		{
			result.rejectWithThrowable(new ExecutionException("Process not find for attach"));
			return;
		}

		try
		{
			result.setDone(runContentDescriptor(executionResult, environment, process, null, true));
		}
		catch(ExecutionException e)
		{
			result.rejectWithThrowable(e);
		}
	}
}
