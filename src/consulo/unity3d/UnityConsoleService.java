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

package consulo.unity3d;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.MessageView;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;

/**
 * @author VISTALL
 * @since 09-Jun-16
 */
public class UnityConsoleService
{
	@NotNull
	public static UnityConsoleService getInstance(@NotNull Project project)
	{
		return ServiceManager.getService(project, UnityConsoleService.class);
	}

	@RequiredDispatchThread
	public static void byPath(@NotNull String projectPath, @NotNull PairConsumer<Project, NewErrorTreeViewPanel> consumer)
	{
		UnityConsoleService consoleService = findByProjectPath(projectPath);
		if(consoleService == null)
		{
			return;
		}

		NewErrorTreeViewPanel viewPanel = consoleService.getOrInitPanel();
		if(viewPanel == null)
		{
			return;
		}
		consumer.consume(consoleService.getProject(), viewPanel);
	}

	@Nullable
	public static UnityConsoleService findByProjectPath(@NotNull String projectPath)
	{
		Project project = null;
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(projectPath);
		if(fileByPath != null)
		{
			Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
			for(Project openProject : openProjects)
			{
				if(fileByPath.equals(openProject.getBaseDir()))
				{
					project = openProject;
					break;
				}
			}
		}
		if(project == null)
		{
			return null;
		}
		return UnityConsoleService.getInstance(project);
	}

	private static class MyErrorPanel extends NewErrorTreeViewPanel
	{
		public MyErrorPanel(Project project)
		{
			super(project, "unityLog", false, true);
		}

		@Override
		protected void fillRightToolbarGroup(DefaultActionGroup group)
		{
			group.add(new ToggleAction("Clear on Play", null, AllIcons.Actions.Execute)
			{
				@Override
				public boolean isSelected(AnActionEvent anActionEvent)
				{
					return PropertiesComponent.getInstance(myProject).getBoolean(ourClearOnPlayUnityLog, ourDefaultClearOnPlayValue);
				}

				@Override
				public void setSelected(AnActionEvent anActionEvent, boolean b)
				{
					PropertiesComponent.getInstance(myProject).setValue(ourClearOnPlayUnityLog, b, ourDefaultClearOnPlayValue);
				}
			});

			group.add(new AnAction("Clear", null, AllIcons.Actions.Reset_to_empty)
			{
				@RequiredDispatchThread
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent)
				{
					clearMessages();
				}
			});
			super.fillRightToolbarGroup(group);
		}
	}

	private static final Key<Boolean> ourViewKey = Key.create("UnityLog");
	private static final String ourClearOnPlayUnityLog = "ClearOnPlayUnityLog";
	private static final boolean ourDefaultClearOnPlayValue = false;

	private MyErrorPanel myErrorPanel;
	private final Project myProject;
	private final AtomicBoolean myToolwindowInit = new AtomicBoolean();

	public UnityConsoleService(Project project)
	{
		myProject = project;
	}

	private boolean isClearOnPlay()
	{
		return PropertiesComponent.getInstance(myProject).getBoolean(ourClearOnPlayUnityLog, ourDefaultClearOnPlayValue);
	}

	@NotNull
	public Project getProject()
	{
		return myProject;
	}

	@RequiredDispatchThread
	public void onPlay()
	{
		if(myErrorPanel != null && isClearOnPlay())
		{
			myErrorPanel.clearMessages();

			ToolWindow toolWindow = MessageView.SERVICE.getInstance(myProject).getToolWindow();

			toolWindow.hide(EmptyRunnable.getInstance());
		}
	}

	@RequiredDispatchThread
	@Nullable
	private NewErrorTreeViewPanel getOrInitPanel()
	{
		if(myErrorPanel != null)
		{
			return myErrorPanel;
		}

		if(myToolwindowInit.compareAndSet(false, true))
		{
			MessageView messageView = MessageView.SERVICE.getInstance(myProject);
			messageView.runWhenInitialized(() ->
			{
				final ContentManager contentManager = messageView.getContentManager();
				Content[] contents = contentManager.getContents();
				Content content = ContainerUtil.find(contents, content1 -> content1.getUserData(ourViewKey) != null);

				MyErrorPanel errorTreeViewPanel = null;
				if(content == null)
				{
					errorTreeViewPanel = new MyErrorPanel(myProject);

					content = ContentFactory.SERVICE.getInstance().createContent(errorTreeViewPanel, "Editor", false);
					content.putUserData(ourViewKey, Boolean.TRUE);

					contentManager.addContent(content);
				}
				else
				{
					errorTreeViewPanel = (MyErrorPanel) content.getComponent();
				}

				contentManager.setSelectedContent(content, true);

				messageView.getToolWindow().show(EmptyRunnable.getInstance());
				myErrorPanel = errorTreeViewPanel;
			});
		}

		return myErrorPanel;
	}
}
