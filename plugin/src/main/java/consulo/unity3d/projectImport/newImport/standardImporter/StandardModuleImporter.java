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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import consulo.unity3d.projectImport.newImport.UnityAssemblyContext;
import consulo.unity3d.projectImport.newImport.UnityProjectImporterWithAsmDef;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
public abstract class StandardModuleImporter
{
	private final String myName;
	private final FileType myFileType;

	protected StandardModuleImporter(String name, FileType fileType)
	{
		myName = name;
		myFileType = fileType;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@Nonnull
	protected abstract Set<VirtualFile> getDirectoriesForAnalyze(@Nonnull Project project);

	public void analyzeSourceFiles(@Nonnull Project project, UnityAssemblyContext assemblyContext, Set<String> registeredFiles)
	{
		Set<VirtualFile> directories = getDirectoriesForAnalyze(project);
		if(directories.isEmpty())
		{
			return;
		}

		FileTypeManager fileTypeManager = FileTypeManager.getInstance();

		for(VirtualFile directory : directories)
		{
			VfsUtil.visitChildrenRecursively(directory, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(fileTypeManager.isFileIgnored(file) || UnityProjectImporterWithAsmDef.EDITOR_DIRECTORY.equalsIgnoreCase(file.getName()))
					{
						return false;
					}

					if(file.getFileType() == myFileType)
					{
						String url = file.getUrl();
						if(!registeredFiles.add(url))
						{
							return false;
						}

						assemblyContext.addSourceFile(file);
					}
					
					return true;
				}
			});
		}
	}
}
