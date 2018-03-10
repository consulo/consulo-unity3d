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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.intellij.icons.AllIcons;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.problems.Problem;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.MessageView;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.MessageCategory;
import com.intellij.util.ui.UIUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.dotnet.compiler.DotNetCompilerMessage;
import consulo.unity3d.console.Unity3dConsoleManager;
import consulo.unity3d.jsonApi.UnityLogParser;
import consulo.unity3d.jsonApi.UnityLogPostHandlerRequest;

/**
 * @author VISTALL
 * @since 09-Jun-16
 */
public class Unity3dConsoleToolWindowService implements ProjectComponent
{
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
				public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
				{
					clearMessages();
				}
			});
			super.fillRightToolbarGroup(group);
		}
	}

	@Nonnull
	public static Unity3dConsoleToolWindowService getInstance(@Nonnull Project project)
	{
		return project.getComponent(Unity3dConsoleToolWindowService.class);
	}

	private static final Key<Boolean> ourViewKey = Key.create("UnityLog");
	private static final String ourClearOnPlayUnityLog = "ClearOnPlayUnityLog";
	private static final boolean ourDefaultClearOnPlayValue = false;

	private MyErrorPanel myErrorPanel;
	private final Project myProject;
	private final AtomicBoolean myToolwindowInit = new AtomicBoolean();

	private AccessToken myUnregister;

	public Unity3dConsoleToolWindowService(Project project)
	{
		myProject = project;
	}

	@Override
	public void projectOpened()
	{
		myUnregister = Unity3dConsoleManager.getInstance().registerProcessor(myProject, this::process);
	}

	private void process(Collection<UnityLogPostHandlerRequest> list)
	{
		UIUtil.invokeLaterIfNeeded(() ->
		{
			NewErrorTreeViewPanel panel = getOrInitPanel();
			WolfTheProblemSolver solver = WolfTheProblemSolver.getInstance(myProject);
			VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();

			for(UnityLogPostHandlerRequest request : list)
			{
				DotNetCompilerMessage message = UnityLogParser.extractFileInfo(myProject, request.condition);

				int value = request.getMessageCategory();

				if(message != null)
				{
					VirtualFile fileByUrl = message.getFileUrl() == null ? null : virtualFileManager.findFileByUrl(message.getFileUrl());
					if(fileByUrl != null && value == MessageCategory.ERROR)
					{
						Problem problem = solver.convertToProblem(fileByUrl, message.getLine(), message.getColumn(), new String[]{message.getMessage()});
						if(problem != null)
						{
							solver.reportProblems(fileByUrl, Collections.singletonList(problem));
						}
					}

					panel.addMessage(value, new String[]{message.getMessage()}, fileByUrl, message.getLine() - 1, message.getColumn(), null);
				}
				else
				{
					panel.addMessage(value, new String[]{
							request.condition,
							request.stackTrace
					}, null, -1, -1, null);
				}
			}
		});
	}

	@Override
	public void projectClosed()
	{
		myUnregister.finish();
	}

	private boolean isClearOnPlay()
	{
		return PropertiesComponent.getInstance(myProject).getBoolean(ourClearOnPlayUnityLog, ourDefaultClearOnPlayValue);
	}

	@Nonnull
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
		}
	}

	@RequiredDispatchThread
	@Nonnull
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

					content = ContentFactory.getInstance().createContent(errorTreeViewPanel, "Editor", false);
					content.putUserData(ourViewKey, Boolean.TRUE);

					contentManager.addContent(content);
				}
				else
				{
					errorTreeViewPanel = (MyErrorPanel) content.getComponent();
				}

				contentManager.setSelectedContent(content, true);

				myErrorPanel = errorTreeViewPanel;
			});
		}

		return myErrorPanel;
	}
}
