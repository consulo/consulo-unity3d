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

package consulo.unity3d.ide.projectView;

import consulo.project.Project;
import consulo.project.ui.view.ProjectView;
import consulo.project.ui.view.ProjectViewPane;
import consulo.project.ui.view.ProjectViewPaneOptionProvider;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.ToggleAction;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.util.dataholder.KeyWithDefaultValue;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class Unity3dShowMetaFileProjectViewPaneOptionProvider extends ProjectViewPaneOptionProvider.BoolValue
{
	public static final KeyWithDefaultValue<Boolean> KEY = KeyWithDefaultValue.create("show-meta-files", Boolean.FALSE);

	public final class ShowMetaFilesAction extends ToggleAction
	{
		private ProjectViewPane myPane;

		private ShowMetaFilesAction(ProjectViewPane pane)
		{
			super("Show '.meta' Files", "Show '.meta' Files", null);
			myPane = pane;
		}

		@Override
		public boolean isSelected(AnActionEvent event)
		{
			return myPane.getUserData(KEY);
		}

		@Override
		public void setSelected(AnActionEvent event, boolean flag)
		{
			Boolean value = myPane.getUserData(KEY);
			assert value != null;
			if(value != flag)
			{
				myPane.putUserData(KEY, flag);
				myPane.updateFromRoot(true);
			}
		}

		@Override
		@RequiredUIAccess
		public void update(AnActionEvent e)
		{
			super.update(e);
			final Presentation presentation = e.getPresentation();
			Project project = e.getData(Project.KEY);
			final ProjectView projectView = ProjectView.getInstance(project);
			presentation.setVisible(projectView.getCurrentProjectViewPane() == myPane && Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null);
		}
	}

	@Nonnull
	@Override
	public KeyWithDefaultValue<Boolean> getKey()
	{
		return KEY;
	}

	@Override
	@RequiredUIAccess
	public void addToolbarActions(@Nonnull ProjectViewPane pane, @Nonnull DefaultActionGroup actionGroup)
	{
		if(pane instanceof ProjectViewPane)
		{
			actionGroup.addAction(new ShowMetaFilesAction(pane)).setAsSecondary(true);
		}
	}
}
