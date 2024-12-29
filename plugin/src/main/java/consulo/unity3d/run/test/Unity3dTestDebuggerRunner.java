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

package consulo.unity3d.run.test;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.execution.ui.console.ConsoleView;
import consulo.process.ExecutionException;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.run.Unity3dAttachRunner;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 30.01.2016
 */
@ExtensionImpl
public class Unity3dTestDebuggerRunner extends DefaultProgramRunner
{
	@Nonnull
	@Override
	public String getRunnerId()
	{
		return "UnityTestDebuggerRunner";
	}

	@Override
	@RequiredUIAccess
	protected RunContentDescriptor doExecute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment env) throws ExecutionException
	{
		UnityDebugProcessInfo editorProcess = UnityEditorCommunication.findEditorProcess();
		if(editorProcess == null)
		{
			throw new ExecutionException("Editor is not responding");
		}
		FileDocumentManager.getInstance().saveAllDocuments();

		ExecutionResult executionResult = state.execute(env.getExecutor(), this);
		if(executionResult == null)
		{
			return null;
		}
		return Unity3dAttachRunner.runContentDescriptor(executionResult, env, editorProcess, (ConsoleView) executionResult.getExecutionConsole(), true);
	}

	@Override
	public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile)
	{
		if(!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId))
		{
			return false;
		}
		return profile instanceof Unity3dTestConfiguration;
	}
}
