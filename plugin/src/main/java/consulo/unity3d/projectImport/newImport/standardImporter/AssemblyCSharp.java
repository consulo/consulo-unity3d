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

import consulo.csharp.lang.CSharpFileType;
import consulo.project.Project;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
public class AssemblyCSharp extends StandardModuleImporter
{
	public AssemblyCSharp()
	{
		super("Assembly-CSharp", CSharpFileType.INSTANCE);
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
		return Set.of(assetsDir);
	}
}
