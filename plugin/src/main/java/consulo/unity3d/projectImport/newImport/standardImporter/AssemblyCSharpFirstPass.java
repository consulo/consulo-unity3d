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
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
public class AssemblyCSharpFirstPass extends StandardModuleImporter
{
	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

	public AssemblyCSharpFirstPass()
	{
		super("Assembly-CSharp-firstpass", CSharpFileType.INSTANCE);
	}

	@Nonnull
	@Override
	protected Set<VirtualFile> getDirectoriesForAnalyze(@Nonnull Project project)
	{
		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;

		Set<VirtualFile> moduleDirs = new HashSet<>();
		for(String passPath : FIRST_PASS_PATHS)
		{
			VirtualFile file = baseDir.findFileByRelativePath(passPath);
			if(file != null)
			{
				moduleDirs.add(file);
			}
		}
		return moduleDirs;
	}
}
