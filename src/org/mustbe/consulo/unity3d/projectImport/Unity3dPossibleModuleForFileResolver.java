package org.mustbe.consulo.unity3d.projectImport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.roots.ContentEntryFileListener;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 06.04.2015
 */
public class Unity3dPossibleModuleForFileResolver implements ContentEntryFileListener.PossibleModuleForFileResolver
{
	@Nullable
	@Override
	@RequiredReadAction
	public Module resolve(@NotNull Project project, @NotNull VirtualFile virtualFile)
	{
		if(virtualFile.getFileType() == CSharpFileType.INSTANCE)
		{
			Module module = Unity3dModuleExtensionUtil.getRootModule(project);
			if(module == null)
			{
				return null;
			}

			VirtualFile baseDir = project.getBaseDir();
			assert baseDir != null;
			VirtualFile assetsDir = baseDir.findChild("Assets");
			if(assetsDir == null)
			{
				return null;
			}

			if(!VfsUtil.isAncestor(assetsDir, virtualFile, true))
			{
				return null;
			}

			VirtualFile parent = virtualFile.getParent();
			while(!assetsDir.equals(parent))
			{
				if(parent.getName().equals("Editor"))
				{
					return ModuleManager.getInstance(project).findModuleByName("Assembly-CSharp-Editor");
				}
				parent = parent.getParent();
			}

			String[] paths = new String[]{
					"Assets/Standard Assets",
					"Assets/Pro Standard Assets",
					"Assets/Plugins"
			};

			for(String path : paths)
			{
				VirtualFile pathFile = baseDir.findFileByRelativePath(path);
				if(pathFile != null && VfsUtil.isAncestor(pathFile, virtualFile, true))
				{
					return ModuleManager.getInstance(project).findModuleByName("Assembly-CSharp-firstpass");
				}
			}
			return ModuleManager.getInstance(project).findModuleByName("Assembly-CSharp");
		}
		else if(virtualFile.getFileType() == JavaScriptFileType.INSTANCE)
		{
			Module module = Unity3dModuleExtensionUtil.getRootModule(project);
			if(module == null)
			{
				return null;
			}

			VirtualFile baseDir = project.getBaseDir();
			assert baseDir != null;
			VirtualFile assetsDir = baseDir.findChild("Assets");
			if(assetsDir == null)
			{
				return null;
			}

			if(!VfsUtil.isAncestor(assetsDir, virtualFile, true))
			{
				return null;
			}
			return ModuleManager.getInstance(project).findModuleByName("Assembly-UnityScript-firstpass");
		}
		return null;
	}
}
