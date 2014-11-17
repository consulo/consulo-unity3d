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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.11.2014
 */
public class UnityRunProfileState implements RunProfileState
{
	private final ExecutionEnvironment myExecutionEnvironment;
	private final GeneralCommandLine myRunCommandLine;

	public UnityRunProfileState(ExecutionEnvironment executionEnvironment, GeneralCommandLine runCommandLine)
	{
		myExecutionEnvironment = executionEnvironment;
		myRunCommandLine = runCommandLine;
	}

	@Nullable
	@Override
	public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException
	{
		val builder = TextConsoleBuilderFactory.getInstance().createBuilder(getExecutionEnvironment().getProject());

		OSProcessHandler osProcessHandler = new OSProcessHandler(getRunCommandLine());
		ConsoleView console = builder.getConsole();
		console.attachToProcess(osProcessHandler);
		return new DefaultExecutionResult(console, osProcessHandler);
	}

	public ExecutionEnvironment getExecutionEnvironment()
	{
		return myExecutionEnvironment;
	}

	public GeneralCommandLine getRunCommandLine()
	{
		return myRunCommandLine;
	}
}
