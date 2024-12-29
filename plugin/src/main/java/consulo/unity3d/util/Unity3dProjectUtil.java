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

package consulo.unity3d.util;

import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 03-Nov-17
 */
public class Unity3dProjectUtil
{
	@Nullable
	public static Project findProjectByPath(String projectPath)
	{
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(projectPath);
		if(fileByPath != null)
		{
			Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
			for(Project openProject : openProjects)
			{
				if(fileByPath.equals(openProject.getBaseDir()))
				{
					return openProject;
				}
			}
		}
		return null;
	}
}
