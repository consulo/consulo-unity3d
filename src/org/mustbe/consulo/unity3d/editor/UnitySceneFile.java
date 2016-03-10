package org.mustbe.consulo.unity3d.editor;

import javax.swing.Icon;

import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

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
				return VfsUtil.getRelativePath(myVirtualFile, getProject().getBaseDir());
			}

			@Nullable
			@Override
			public String getLocationString()
			{
				return null;
			}

			@Nullable
			@Override
			public Icon getIcon(boolean unused)
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
