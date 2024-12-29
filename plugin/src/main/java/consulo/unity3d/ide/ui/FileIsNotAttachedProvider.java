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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.msil.impl.representation.fileSystem.MsilFileRepresentationVirtualFile;
import consulo.project.Project;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2018-11-29
 */
@ExtensionImpl
public class FileIsNotAttachedProvider implements EditorNotificationProvider
{
	private final Project myProject;

	@Inject
	public FileIsNotAttachedProvider(Project project)
	{
		myProject = project;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "unity-file-not-attached";
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile virtualFile, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> supplier)
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
			EditorNotificationBuilder panel = supplier.get();
			panel.withText(LocalizeValue.localizeTODO("File is not attached to project. Some features are unavailable (code analysis, debugging, etc)"));
			panel.withAction(LocalizeValue.localizeTODO("Re-import Unity Project"), (e) ->
			{
				Unity3dProjectImporter.syncProjectStep(myProject, rootModuleExtension.getSdk(), null, true);
			});
			return panel;
		}
		return null;
	}
}
