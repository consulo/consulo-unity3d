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

import javax.annotation.Nonnull;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import consulo.ui.RequiredUIAccess;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.run.Unity3dAttachRunner;
import consulo.unity3d.run.debugger.UnityProcess;

/**
 * @author VISTALL
 * @since 30.01.2016
 */
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
		UnityProcess editorProcess = UnityEditorCommunication.findEditorProcess();
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
