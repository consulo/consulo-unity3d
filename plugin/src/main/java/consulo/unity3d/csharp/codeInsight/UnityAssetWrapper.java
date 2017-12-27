/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.csharp.codeInsight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.unity3d.scene.index.Unity3dYMLField;

/**
 * @author VISTALL
 * @since 04-Sep-17
 */
class UnityAssetWrapper extends FakePsiElement implements Navigatable
{
	private final VirtualFile myVirtualFile;
	private final Unity3dYMLAsset myAsset;
	private final int myOffset;
	private final Unity3dYMLField myField;
	private final Project myProject;

	UnityAssetWrapper(VirtualFile virtualFile, Unity3dYMLAsset asset, @NotNull Unity3dYMLField field, Project project)
	{
		this(virtualFile, asset, field.getOffset(), field, project);
	}

	UnityAssetWrapper(VirtualFile virtualFile, Unity3dYMLAsset asset, int offset, @Nullable Unity3dYMLField field, Project project)
	{
		myVirtualFile = virtualFile;
		myAsset = asset;
		myOffset = offset;
		myField = field;
		myProject = project;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return null;
	}

	@NotNull
	@Override
	public Project getProject()
	{
		return myProject;
	}

	public VirtualFile getVirtualFile()
	{
		return myVirtualFile;
	}

	public Unity3dYMLAsset getAsset()
	{
		return myAsset;
	}

	public Unity3dYMLField getField()
	{
		return myField;
	}

	@Override
	public void navigate(boolean b)
	{
		OpenFileDescriptor descriptor = new OpenFileDescriptor(myProject, myVirtualFile, myOffset);
		FileEditorManager.getInstance(myProject).openTextEditor(descriptor, true);
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public boolean canNavigateToSource()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public PsiElement getParent()
	{
		return null;
	}
}
