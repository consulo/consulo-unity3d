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

package consulo.unity3d.run.debugger;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.MessageCategory;
import com.intellij.xdebugger.XDebugSession;
import consulo.disposer.Disposable;
import consulo.dotnet.debugger.nodes.logicView.DotNetLogicValueView;
import consulo.dotnet.debugger.nodes.logicView.EnumerableDotNetLogicValueView;
import consulo.dotnet.execution.DebugConnectionInfo;
import consulo.dotnet.mono.debugger.MonoDebugProcess;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.console.Unity3dConsoleManager;
import consulo.unity3d.jsonApi.UnityLogPostHandlerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class UnityDebugProcess extends MonoDebugProcess
{
	private ConsoleView myConsoleView;
	@Nullable
	private Disposable myMessageBusConnection;

	public UnityDebugProcess(XDebugSession session, RunProfile runProfile, DebugConnectionInfo debugConnectionInfo, ConsoleView consoleView, boolean insideEditor)
	{
		super(session, runProfile, debugConnectionInfo);
		myConsoleView = consoleView;

		if(insideEditor)
		{
			myMessageBusConnection = Unity3dConsoleManager.getInstance().registerProcessor(session.getProject(), list ->
			{
				ConsoleView view = session.getConsoleView();
				if(view == null)
				{
					return;
				}

				for(UnityLogPostHandlerRequest request : list)
				{
					switch(request.getMessageCategory())
					{
						case MessageCategory.ERROR:
							print(request.condition, request.stackTrace, view, ConsoleViewContentType.ERROR_OUTPUT);
							break;
						default:
							print(request.condition, request.stackTrace, view, ConsoleViewContentType.NORMAL_OUTPUT);
							break;
					}
				}
			});
		}
	}

	private void print(String text, String stacktrace, ConsoleView view, ConsoleViewContentType contentType)
	{
		view.print(text + "\n", contentType);
		if(contentType == ConsoleViewContentType.ERROR_OUTPUT && !StringUtil.isEmpty(stacktrace))
		{
			StringBuilder builder = new StringBuilder();
			String[] strings = StringUtil.splitByLines(stacktrace);
			for(String line : strings)
			{
				builder.append("  at ").append(line).append("\n");
			}

			view.print(builder.toString(), contentType);
		}
	}

	@Nonnull
	@Override
	protected DotNetLogicValueView[] createLogicValueViews()
	{
		DotNetLogicValueView[] views = super.createLogicValueViews();
		for(DotNetLogicValueView view : views)
		{
			if(view instanceof EnumerableDotNetLogicValueView)
			{
				((EnumerableDotNetLogicValueView) view).addIgnoredType(Unity3dTypes.UnityEngine.MonoBehaviour);
			}
		}
		return views;
	}

	@Override
	public void stop()
	{
		if(myMessageBusConnection != null)
		{
			myMessageBusConnection.dispose();
			myMessageBusConnection = null;
		}

		super.stop();
	}

	@Nonnull
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
