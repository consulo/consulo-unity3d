package org.mustbe.consulo.unity3d.run;

import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.run.debugger.UnityDebugProcess;
import org.mustbe.consulo.unity3d.run.debugger.UnityPlayer;
import org.mustbe.consulo.unity3d.run.debugger.UnityPlayerService;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Condition;
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
		Collection<UnityPlayer> players = UnityPlayerService.getInstance().getPlayers();
		List<UnityPlayer> debugPlayers = ContainerUtil.filter(players, new Condition<UnityPlayer>()
		{
			@Override
			public boolean value(UnityPlayer unityPlayer)
			{
				return unityPlayer.isSupportDebugging();
			}
		});
		ChooseElementsDialog<UnityPlayer> dialog = new ChooseElementsDialog<UnityPlayer>(environment.getProject(), debugPlayers,
				"Select Unity Player", "", true)
		{
			@Override
			protected String getItemText(UnityPlayer item)
			{
				return item.getId() + " (" + item.getIp() + ":" + item.getDebuggerPort() + ")";
			}

			@Nullable
			@Override
			protected Icon getItemIcon(UnityPlayer item)
			{
				return Unity3dIcons.Unity3d;
			}
		};

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
						if(!debugConnectionInfo.isServer())
						{
							process.start();
						}
						if(debugConnectionInfo.isServer())
						{
							process.start();
						}
						return process;
					}
				});

		return debugSession.getRunContentDescriptor();
	}
}
