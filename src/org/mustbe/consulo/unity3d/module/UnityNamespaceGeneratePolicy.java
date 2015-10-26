package org.mustbe.consulo.unity3d.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import org.mustbe.consulo.unity3d.projectImport.Unity3dProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

/**
 * @author VISTALL
 * @since 26.10.2015
 */
public class UnityNamespaceGeneratePolicy implements DotNetNamespaceGeneratePolicy
{
	public static final UnityNamespaceGeneratePolicy INSTANCE = new UnityNamespaceGeneratePolicy();

	@RequiredReadAction
	@Nullable
	@Override
	public String calculateNamespace(@NotNull PsiDirectory psiDirectory)
	{
		Project project = psiDirectory.getProject();
		VirtualFile baseDir = project.getBaseDir();
		if(baseDir == null)
		{
			return null;
		}

		VirtualFile targetDir = psiDirectory.getVirtualFile();

		VirtualFile assetsDirectory = baseDir.findChild(Unity3dProjectUtil.ASSETS_DIRECTORY);
		if(assetsDirectory != null)
		{
			String relativePath = VfsUtil.getRelativePath(targetDir, assetsDirectory, '.');
			if(relativePath != null)
			{
				return StringUtil.replaceChar(relativePath, ' ', '_');
			}
		}
		return null;
	}
}
