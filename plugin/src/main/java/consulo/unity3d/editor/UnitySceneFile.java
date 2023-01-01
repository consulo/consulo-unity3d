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

package consulo.unity3d.editor;

import consulo.language.Language;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiManager;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.01.2016
 */
public class UnitySceneFile extends LightElement implements NavigatablePsiElement
{
	private VirtualFile myVirtualFile;

	public UnitySceneFile(Project project, VirtualFile virtualFile)
	{
		super(PsiManager.getInstance(project), Language.ANY);
		myVirtualFile = virtualFile;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		if("unity".equals(myVirtualFile.getName()))
		{
			return;
		}
		UnityOpenScene p = new UnityOpenScene(myVirtualFile.getPath());
		UnityEditorCommunication.request(getProject(), p, false);
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return new ItemPresentation()
		{
			@Nullable
			@Override
			public String getPresentableText()
			{
				return VirtualFileUtil.getRelativePath(myVirtualFile, getProject().getBaseDir());
			}

			@Nullable
			@Override
			public String getLocationString()
			{
				return null;
			}

			@Nullable
			@Override
			public Image getIcon()
			{
				return myVirtualFile.getFileType().getIcon();
			}
		};
	}

	@Override
	public String toString()
	{
		return "SceneFile: " + myVirtualFile;
	}
}
