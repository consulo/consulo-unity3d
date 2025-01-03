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

import consulo.fileEditor.FileEditorManager;
import consulo.language.impl.psi.FakePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.navigation.Navigatable;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.unity3d.scene.index.Unity3dYMLField;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

	UnityAssetWrapper(VirtualFile virtualFile, Unity3dYMLAsset asset, @Nonnull Unity3dYMLField field, Project project)
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

	@Nonnull
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
		OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(myProject).builder(myVirtualFile).offset(myOffset).build();
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
