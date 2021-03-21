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

package consulo.unity3d.scene.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import consulo.unity3d.scene.Unity3dMetaManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public record Unity3dYMLAsset(String guild, String gameObjectName, int startOffset, List<Unity3dYMLField> values)
{
	@Nonnull
	public static MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@Nonnull Project project, @Nullable VirtualFile file)
	{
		return file == null ? MultiMap.empty() : Unity3dMetaManager.getInstance(project).findAssetAsAttach(file);
	}

	@Deprecated
	public int getStartOffset()
	{
		return startOffset();
	}

	@Nullable
	@Deprecated
	public String getGameObjectName()
	{
		return gameObjectName();
	}

	@Nonnull
	@Deprecated
	public String getGuild()
	{
		return guild();
	}

	@Nonnull
	@Deprecated
	public List<Unity3dYMLField> getValues()
	{
		return values();
	}
}
