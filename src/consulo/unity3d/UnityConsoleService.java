package consulo.unity3d;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Condition;
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
import consulo.lombok.annotations.ProjectService;

/**
 * @author VISTALL
 * @since 09-Jun-16
 */
@ProjectService
public class UnityConsoleService
{
	@RequiredDispatchThread
	public static void byPath(@NotNull String projectPath, @NotNull PairConsumer<Project, NewErrorTreeViewPanel> consumer)
	{
		UnityConsoleService consoleService = findByProjectPath(projectPath);
		if(consoleService == null)
		{
			return;
		}

		NewErrorTreeViewPanel viewPanel = consoleService.getOrInitPanel();
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
	private NewErrorTreeViewPanel getOrInitPanel()
	{
		if(myErrorPanel != null)
		{
			return myErrorPanel;
		}
		ToolWindow toolWindow = MessageView.SERVICE.getInstance(myProject).getToolWindow();

		final ContentManager contentManager = toolWindow.getContentManager();
		Content[] contents = contentManager.getContents();
		Content content = ContainerUtil.find(contents, new Condition<Content>()
		{
			@Override
			public boolean value(Content content)
			{
				return content.getUserData(ourViewKey) != null;
			}
		});

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
		toolWindow.show(EmptyRunnable.getInstance());
		myErrorPanel = errorTreeViewPanel;
		return myErrorPanel;
	}
}
