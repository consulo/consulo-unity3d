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

package org.mustbe.consulo.unity3d.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugProcess;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.xdebugger.XDebugSession;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class UnityDebugProcess extends DotNetDebugProcess
{
	private ConsoleView myConsoleView;

	public UnityDebugProcess(XDebugSession session, DebugConnectionInfo debugConnectionInfo, RunProfile runProfile, ConsoleView consoleView)
	{
		super(session, debugConnectionInfo, runProfile);
		myConsoleView = consoleView;
	}

	@Nullable
	@Override
	protected ProcessHandler doGetProcessHandler()
	{
		return null;
	}

	@NotNull
	@Override
	public ExecutionConsole createConsole()
	{
		if(myConsoleView != null)
		{
			return myConsoleView;
		}
		ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(getSession().getProject()).getConsole();
		consoleView.attachToProcess(getProcessHandler());
		return consoleView;
	}
}
