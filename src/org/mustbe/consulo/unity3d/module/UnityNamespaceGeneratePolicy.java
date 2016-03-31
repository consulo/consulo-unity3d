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
public class UnityNamespaceGeneratePolicy extends DotNetNamespaceGeneratePolicy
{
	public static final UnityNamespaceGeneratePolicy INSTANCE = new UnityNamespaceGeneratePolicy(null);

	public static UnityNamespaceGeneratePolicy createOrGet(@NotNull Unity3dRootModuleExtension rootModuleExtension)
	{
		String namespacePrefix = rootModuleExtension.getNamespacePrefix();
		if(namespacePrefix != null)
		{
			return new UnityNamespaceGeneratePolicy(namespacePrefix);
		}
		return INSTANCE;
	}

	@Nullable
	private String myNamespacePrefix;

	public UnityNamespaceGeneratePolicy(@Nullable String namespacePrefix)
	{
		myNamespacePrefix = namespacePrefix;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String calculateDirtyNamespace(@NotNull PsiDirectory psiDirectory)
	{
		Project project = psiDirectory.getProject();
		VirtualFile baseDir = project.getBaseDir();
		if(baseDir == null)
		{
			return myNamespacePrefix;
		}

		VirtualFile currentDirectory = psiDirectory.getVirtualFile();

		VirtualFile assetsDirectory = baseDir.findChild(Unity3dProjectUtil.ASSETS_DIRECTORY);
		if(assetsDirectory != null)
		{
			VirtualFile targetDirectory = assetsDirectory;

			VirtualFile temp = currentDirectory;
			while(temp != null && !temp.equals(targetDirectory))
			{
				if("Editor".equals(temp.getName()))
				{
					targetDirectory = temp;
				}
				temp = temp.getParent();
			}

			// if not editor path
			if(targetDirectory.equals(assetsDirectory))
			{
				for(String path : Unity3dProjectUtil.FIRST_PASS_PATHS)
				{
					VirtualFile child = baseDir.findFileByRelativePath(path);
					if(child != null && VfsUtil.isAncestor(child, currentDirectory, false))
					{
						targetDirectory = child;
						break;
					}
				}
			}

			String relativePath = VfsUtil.getRelativePath(currentDirectory, targetDirectory, '.');
			if(relativePath != null)
			{
				if(!StringUtil.isEmpty(myNamespacePrefix))
				{
					return myNamespacePrefix + "." + relativePath;
				}
				return relativePath;
			}
		}
		return myNamespacePrefix;
	}
}
