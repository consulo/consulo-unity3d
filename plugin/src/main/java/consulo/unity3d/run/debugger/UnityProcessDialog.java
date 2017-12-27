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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;
import consulo.unity3d.Unity3dIcons;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class UnityProcessDialog extends ChooseElementsDialog<UnityProcess>
{
	private static final Logger LOGGER = Logger.getInstance(UnityProcessDialog.class);

	private Future<?> myTask;

	public UnityProcessDialog(@NotNull Project project)
	{
		super(project, new ArrayList<>(), "Select Unity Process", "", true);
	}

	@Override
	public void show()
	{
		myTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) () -> UIUtil.invokeLaterIfNeeded(() ->
		{
			List<UnityProcess> selectedElements = myChooser.getSelectedElements();
			UnityProcessDialog.this.setElements(collectItems(), selectedElements);
		}), 0, 1, TimeUnit.SECONDS);
		super.show();
	}

	@NotNull
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
			JavaSysMon javaSysMon = new JavaSysMon();
			ProcessInfo[] processInfos = javaSysMon.processTable();
			for(ProcessInfo processInfo : processInfos)
			{
				String name = processInfo.getName();
				if((StringUtil.startsWithIgnoreCase(name, "unity") || StringUtil.containsIgnoreCase(name, "Unity.app")) && !(StringUtil.containsIgnoreCase(name, "Unity") && StringUtil
						.containsIgnoreCase(name, "Helper")) //ignore 'UnityHelper' and 'Unity Helper'
						&& !StringUtil.containsIgnoreCase(name, "UnityShader"))
				{
					items.add(new UnityProcess(processInfo.getPid(), name, "localhost", buildDebuggerPort(processInfo.getPid())));
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e);
		}
		return items;
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
	protected Icon getItemIcon(UnityProcess item)
	{
		return Unity3dIcons.Unity3d;
	}
}
