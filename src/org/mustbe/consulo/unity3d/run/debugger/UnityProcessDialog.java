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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class UnityProcessDialog extends ChooseElementsDialog<UnityProcess>
{
	private boolean myClosed;

	public UnityProcessDialog(@NotNull Project project)
	{
		super(project, new ArrayList<UnityProcess>(), "Select Unity Process", "", true);

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable()
		{
			@Override
			public void run()
			{
				while(!myClosed)
				{
					try
					{
						UIUtil.invokeLaterIfNeeded(new Runnable()
						{
							@Override
							public void run()
							{
								List<UnityProcess> selectedElements = myChooser.getSelectedElements();
								UnityProcessDialog.this.setElements(collectItems(), selectedElements);
							}
						});
					}
					finally
					{
						try
						{
							Thread.sleep(1000L);
						}
						catch(InterruptedException e)
						{
							//
						}
					}
				}
			}
		});
	}

	@NotNull
	private static List<UnityProcess> collectItems()
	{
		Collection<UnityPlayer> players = UnityPlayerService.getInstance().getPlayers();
		List<UnityProcess> items = new ArrayList<UnityProcess>(players.size() + 1);
		for(UnityPlayer player : players)
		{
			items.add(new UnityProcess((int) player.getGuid(), player.getId(), player.getIp(), player.getDebuggerPort()));
		}
		JavaSysMon javaSysMon = new JavaSysMon();
		ProcessInfo[] processInfos = javaSysMon.processTable();
		for(ProcessInfo processInfo : processInfos)
		{
			String name = processInfo.getName();
			if(StringUtil.startsWithIgnoreCase(name, "unity") || StringUtil.containsIgnoreCase(name, "Unity.app") && !StringUtil.containsIgnoreCase
					(name, "UnityShader"))
			{
				items.add(new UnityProcess(processInfo.getPid(), name, "localhost", 56000 + processInfo.getPid() % 1000));
			}
		}
		return items;
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
		myClosed = true;
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
