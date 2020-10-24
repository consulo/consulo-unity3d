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

import com.intellij.execution.process.ProcessInfo;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import consulo.execution.process.OSProcessUtil;
import consulo.logging.Logger;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class UnityProcessDialog extends ChooseElementsDialog<UnityProcess>
{
	private static final Logger LOG = Logger.getInstance(UnityProcessDialog.class);

	private Future<?> myTask;

	public UnityProcessDialog(@Nonnull Project project)
	{
		super(project, new ArrayList<>(), "Select Unity Process", "", true);
	}

	@Override
	public void show()
	{
		myTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) () ->
		{
			List<UnityProcess> elements = collectItems();

			UIUtil.invokeLaterIfNeeded(() ->
			{
				List<UnityProcess> selectedElements = myChooser.getSelectedElements();
				setElements(elements, selectedElements);
			});
		}, 0, 1, TimeUnit.SECONDS);
		super.show();
	}

	@Nonnull
	public static List<UnityProcess> collectItems()
	{
		Collection<UnityPlayer> players = UnityPlayerService.getInstance().getPlayers();
		List<UnityProcess> items = new ArrayList<>(players.size() + 1);
		try
		{
			for(UnityPlayer player : players)
			{
				if(player.isSupportDebugging())
				{
					items.add(new UnityProcess((int) player.getGuid(), player.getId(), player.getIp(), player.getDebuggerPort()));
				}
			}
			for(ProcessInfo processInfo : OSProcessUtil.getProcessList())
			{
				UnityProcess process = tryParseIfUnityProcess(processInfo);
				if(process != null)
				{
					items.add(process);
				}
			}
		}
		catch(Exception e)
		{
			LOG.error(e);
		}
		return items;
	}

	@Nullable
	public static UnityProcess tryParseIfUnityProcess(ProcessInfo processInfo)
	{
		String name = processInfo.getExecutableName();

		// Unity 2019.3 linux bug - 2020.1 ok
		if(name.equals("Main"))
		{
			String first = processInfo.getExecutableCannonicalPath().orElse("");
			if(first != null && first.endsWith("/Editor/Unity"))
			{
				return new UnityProcess(processInfo.getPid(), name, "localhost", buildDebuggerPort(processInfo.getPid()));
			}
		}

		if(StringUtil.startsWithIgnoreCase(name, "unity") || StringUtil.containsIgnoreCase(name, "Unity.app"))
		{
			// ignore 'UnityHelper' and 'Unity Helper'
			if(StringUtil.containsIgnoreCase(name, "Unity") && StringUtil.containsIgnoreCase(name, "Helper"))
			{
				return null;
			}

			if(StringUtil.containsIgnoreCase(name, "Unity") && StringUtil.containsIgnoreCase(name, "Hub") || StringUtil.containsIgnoreCase(name, "unityhub"))
			{
				return null;
			}

			if(StringUtil.containsIgnoreCase(name, "Unity") && StringUtil.containsIgnoreCase(name, "CrashHandler"))
			{
				return null;
			}

			if(StringUtil.containsIgnoreCase(name, "licensing"))
			{
				return null;
			}

			// UnityShader - Package Manager - Hub compiler
			if(StringUtil.containsIgnoreCase(name, "UnityShader") || StringUtil.containsIgnoreCase(name, "UnityPackageMan"))
			{
				return null;
			}

			return new UnityProcess(processInfo.getPid(), name, "localhost", buildDebuggerPort(processInfo.getPid()));
		}

		return null;
	}

	public static int buildDebuggerPort(int pid)
	{
		return 56000 + pid % 1000;
	}

	@Nullable
	@Override
	protected String getDimensionServiceKey()
	{
		return getClass().getSimpleName();
	}

	@Override
	protected void dispose()
	{
		if(myTask != null)
		{
			myTask.cancel(false);
		}
		super.dispose();
	}

	@Override
	protected String getItemText(UnityProcess item)
	{
		return item.getName() + " (" + item.getHost() + ":" + item.getPort() + ")";
	}

	@Nullable
	@Override
	protected Image getItemIcon(UnityProcess item)
	{
		return Unity3dIcons.Unity3d;
	}
}
