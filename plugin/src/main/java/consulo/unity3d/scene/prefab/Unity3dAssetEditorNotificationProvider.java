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

import com.intellij.find.FindManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import consulo.annotation.access.RequiredReadAction;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.unity3d.scene.Unity3dAssetFileTypeDetector;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
public class Unity3dAssetEditorNotificationProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	private final Project myProject;

	@Inject
	public Unity3dAssetEditorNotificationProvider(Project project)
	{
		myProject = project;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor)
	{
		if(file.getFileType() != Unity3dYMLAssetFileType.INSTANCE || !ArrayUtil.contains(file.getExtension(), Unity3dAssetFileTypeDetector.ourAssetExtensions))
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

		final EditorNotificationPanel panel = new EditorNotificationPanel();
		panel.text("Used asset...");
		panel.createActionLabel("Find usages...", () -> FindManager.getInstance(myProject).findUsages(psiFile));
		return panel;
	}
}
