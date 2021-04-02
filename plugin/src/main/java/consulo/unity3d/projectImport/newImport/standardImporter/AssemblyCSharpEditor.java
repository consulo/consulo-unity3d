/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.projectImport.newImport.standardImporter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import consulo.csharp.lang.CSharpFileType;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.newImport.UnityProjectImporterWithAsmDef;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
public class AssemblyCSharpEditor extends StandardModuleImporter
{
	public AssemblyCSharpEditor()
	{
		super("Assembly-CSharp-Editor", CSharpFileType.INSTANCE);
	}

	@Override
	public boolean isEditorModule()
	{
		return true;
	}

	@Nonnull
	@Override
	protected Set<VirtualFile> getDirectoriesForAnalyze(@Nonnull Project project)
	{
		final VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;
		final VirtualFile assetsDir = baseDir.findFileByRelativePath(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDir == null)
		{
			return Set.of();
		}
		
		Set<VirtualFile> directories = new HashSet<>();
		VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
		{
			@Override
			public boolean visitFile(@Nonnull VirtualFile file)
			{
				if(file.isDirectory() && StringUtil.equalsIgnoreCase(UnityProjectImporterWithAsmDef.EDITOR_DIRECTORY, file.getNameSequence()))
				{
					directories.add(file);
				}
				return true;
			}
		});

		return directories;
	}
}
