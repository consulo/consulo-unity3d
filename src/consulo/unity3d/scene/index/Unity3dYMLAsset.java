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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.indexing.FileBasedIndex;
import consulo.unity3d.scene.Unity3dAssetUtil;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public class Unity3dYMLAsset
{
	@NotNull
	public static MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@NotNull Project project, @Nullable VirtualFile file, boolean single)
	{
		String uuid = Unity3dAssetUtil.getGUID(project, file);
		if(uuid == null)
		{
			return MultiMap.empty();
		}

		CommonProcessors.CollectProcessor<Integer> fileIds = new CommonProcessors.CollectProcessor<>();

		FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
		fileBasedIndex.processAllKeys(Unity3dYMLAssetIndexExtension.KEY, fileIds, project);

		GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
		MultiMap<VirtualFile, Unity3dYMLAsset> map = MultiMap.create();
		for(int fileId : fileIds.getResults())
		{
			ProgressManager.checkCanceled();

			VirtualFile assertFile = fileBasedIndex.findFileById(project, fileId);
			if(assertFile == null)
			{
				continue;
			}

			fileBasedIndex.processValues(Unity3dYMLAssetIndexExtension.KEY, fileId, assertFile, (virtualFile, list) ->
			{
				for(Unity3dYMLAsset asset : list)
				{
					if(Comparing.equal(uuid, asset.getGuild()))
					{
						map.putValue(assertFile, asset);

						if(single)
						{
							return false;
						}
					}
				}

				return true;
			}, scope);

			if(single && !map.isEmpty())
			{
				break;
			}
		}
		return map;
	}

	private final String myGuild;
	private final List<Couple<String>> myValues;

	public Unity3dYMLAsset(@NotNull String guild, @NotNull List<Couple<String>> values)
	{
		myGuild = guild;
		myValues = values;
	}

	public String getGuild()
	{
		return myGuild;
	}

	public List<Couple<String>> getValues()
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
		result = 31 * result + myValues.hashCode();
		return result;
	}
}
