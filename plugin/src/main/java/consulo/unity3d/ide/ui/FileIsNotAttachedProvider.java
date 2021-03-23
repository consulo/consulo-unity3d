/*
 * Copyright 2013-2018 consulo.io
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

package consulo.unity3d.ide.ui;

import com.intellij.ProjectTopics;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.module.extension.ModuleExtension;
import consulo.msil.representation.fileSystem.MsilFileRepresentationVirtualFile;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2018-11-29
 */
public class FileIsNotAttachedProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	private final Project myProject;

	@Inject
	public FileIsNotAttachedProvider(Project project, final EditorNotifications notifications)
	{
		myProject = project;
		myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener()
		{
			@Override
			public void rootsChanged(ModuleRootEvent event)
			{
				notifications.updateAllNotifications();
			}
		});
		myProject.getMessageBus().connect().subscribe(ModuleExtension.CHANGE_TOPIC, (oldExtension, newExtension) -> notifications.updateAllNotifications());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@Nonnull VirtualFile virtualFile, @Nonnull FileEditor fileEditor)
	{
		if(virtualFile.getFileType() != CSharpFileType.INSTANCE)
		{
			return null;
		}

		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
		if(rootModuleExtension == null)
		{
			return null;
		}

		if(ProjectFileIndex.getInstance(myProject).isInLibraryClasses(virtualFile) || virtualFile instanceof MsilFileRepresentationVirtualFile)
		{
			return null;
		}

		Module module = ModuleUtilCore.findModuleForFile(virtualFile, myProject);

		if(module == null || module.equals(rootModuleExtension.getModule()))
		{
			EditorNotificationPanel panel = new EditorNotificationPanel();
			panel.text("File is not attached to project. Some features are unavailable (code analysis, debugging, etc)");
			panel.createActionLabel("Re-import Unity Project", () ->
			{
				Unity3dProjectImporter.syncProjectStep(myProject, rootModuleExtension.getSdk(), null, true);
			});
			return panel;
		}
		return null;
	}
}
