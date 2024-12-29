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

package consulo.unity3d.scene.prefab;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.find.FindManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
@ExtensionImpl
public class Unity3dAssetEditorNotificationProvider implements EditorNotificationProvider
{
	private final Project myProject;

	@Inject
	public Unity3dAssetEditorNotificationProvider(Project project)
	{
		myProject = project;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "unity-asset-usage";
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> supplier)
	{
		if(file.getFileType() != Unity3dYMLAssetFileType.INSTANCE)
		{
			return null;
		}
		final String uuid = Unity3dAssetUtil.getGUID(myProject, file);
		if(uuid == null)
		{
			return null;
		}
		MultiMap<VirtualFile, Unity3dYMLAsset> map = Unity3dYMLAsset.findAssetAsAttach(myProject, file);
		if(map.isEmpty())
		{
			return null;
		}

		PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
		if(psiFile == null)
		{
			return null;
		}

		final EditorNotificationBuilder panel = supplier.get();
		panel.withText(LocalizeValue.localizeTODO("Used asset..."));
		panel.withAction(LocalizeValue.localizeTODO("Find usages..."), (e) -> FindManager.getInstance(myProject).findUsages(psiFile));
		return panel;
	}
}
