/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.run;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.unity3d.run.debugger.UnityDebugProcess;
import org.mustbe.consulo.unity3d.run.debugger.UnityPlayer;
import org.mustbe.consulo.unity3d.run.debugger.UnityProcessDialog;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityRemoteDebugRunner extends DefaultProgramRunner
{
	@NotNull
	@Override
	public String getRunnerId()
	{
		return "UnityRemoteDebugRunner";
	}

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile)
	{
		return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof UnityRemoteDebugConfiguration;
	}

	@Nullable
	@Override
	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull final ExecutionEnvironment environment) throws
			ExecutionException
	{
		UnityProcessDialog dialog = new UnityProcessDialog(environment.getProject());

		List<UnityPlayer> unityPlayers = dialog.showAndGetResult();

		final UnityPlayer firstItem = ContainerUtil.getFirstItem(unityPlayers);
		if(firstItem == null)
		{
			return null;
		}

		FileDocumentManager.getInstance().saveAllDocuments();
		final XDebugSession debugSession = XDebuggerManager.getInstance(environment.getProject()).startSession(environment,
				new XDebugProcessStarter()
				{
					@NotNull
					@Override
					public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException
					{
						DebugConnectionInfo debugConnectionInfo = new DebugConnectionInfo(firstItem.getIp(), firstItem.getDebuggerPort(), true);
						UnityDebugProcess process = new UnityDebugProcess(session, debugConnectionInfo, environment.getRunProfile());
						process.start();
						return process;
					}
				});

		return debugSession.getRunContentDescriptor();
	}
}
