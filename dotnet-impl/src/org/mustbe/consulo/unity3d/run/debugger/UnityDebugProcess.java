package org.mustbe.consulo.unity3d.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugProcess;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.xdebugger.XDebugSession;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class UnityDebugProcess extends DotNetDebugProcess
{
	public UnityDebugProcess(XDebugSession session, DebugConnectionInfo debugConnectionInfo, RunProfile runProfile)
	{
		super(session, debugConnectionInfo, runProfile);
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
		return TextConsoleBuilderFactory.getInstance().createBuilder(getSession().getProject()).getConsole();
	}
}
