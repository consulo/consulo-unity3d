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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import consulo.unity3d.scene.Unity3dMetaManager;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public class Unity3dYMLAsset
{
	@NotNull
	public static MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@NotNull Project project, @Nullable VirtualFile file)
	{
		return file == null ? MultiMap.empty() : Unity3dMetaManager.getInstance(project).findAssetAsAttach(file);
	}

	@NotNull
	private final String myGuild;
	@Nullable
	private final String myGameObjectName;

	private final int myStartOffset;
	@NotNull
	private final List<Unity3dYMLField> myValues;

	public Unity3dYMLAsset(@NotNull String guild, @Nullable String gameObjectName, int startOffset, @NotNull List<Unity3dYMLField> values)
	{
		myGuild = guild;
		myGameObjectName = gameObjectName;
		myStartOffset = startOffset;
		myValues = values;
	}

	public int getStartOffset()
	{
		return myStartOffset;
	}

	@Nullable
	public String getGameObjectName()
	{
		return myGameObjectName;
	}

	@NotNull
	public String getGuild()
	{
		return myGuild;
	}

	@NotNull
	public List<Unity3dYMLField> getValues()
	{
		return myValues;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		Unity3dYMLAsset that = (Unity3dYMLAsset) o;

		if(!myGuild.equals(that.myGuild))
		{
			return false;
		}
		if(myGameObjectName != null ? !myGameObjectName.equals(that.myGameObjectName) : that.myGameObjectName != null)
		{
			return false;
		}
		if(!myValues.equals(that.myValues))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = myGuild.hashCode();
		result = 31 * result + (myGameObjectName != null ? myGameObjectName.hashCode() : 0);
		result = 31 * result + myValues.hashCode();
		return result;
	}
}
