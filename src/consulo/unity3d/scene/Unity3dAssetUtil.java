/*
 * Copyright 2013-2016 consulo.io
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

package consulo.unity3d.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
public class Unity3dAssetUtil
{
	@NotNull
	public static VirtualFile[] sortAssetFiles(VirtualFile[] virtualFiles)
	{
		ContainerUtil.sort(virtualFiles, (o1, o2) -> weight(o1) - weight(o2));
		return virtualFiles;
	}

	private static int weight(VirtualFile virtualFile)
	{
		int i = ArrayUtil.indexOf(Unity3dAssetFileTypeDetector.ourAssetExtensions, virtualFile.getExtension());
		if(i == -1)
		{
			return 1000;
		}
		else
		{
			return (i + 1) * 10;
		}
	}

	@Nullable
	public static String getGUID(@Nullable Project project, @Nullable VirtualFile virtualFile)
	{
		if(virtualFile == null || project == null)
		{
			return null;
		}
		return Unity3dMetaManager.getInstance(project).getGUID(virtualFile);
	}
}
