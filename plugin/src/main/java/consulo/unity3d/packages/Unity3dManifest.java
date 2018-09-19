/*
 * Copyright 2013-2018 consulo.io
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

package consulo.unity3d.packages;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Version;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dManifest
{
	private static final Logger LOGGER = Logger.getInstance(Unity3dManifest.class);

	private static final Unity3dManifest EMPTY = new Unity3dManifest();

	@Nonnull
	public static Unity3dManifest parse(@Nonnull Project project)
	{
		return CachedValuesManager.getManager(project).getCachedValue(project, () ->
		{
			Path projectPath = Paths.get(project.getBasePath());
			Path manifestJson = projectPath.resolve(Paths.get("Packages", "manifest.json"));
			if(Files.exists(manifestJson))
			{
				Gson gson = new Gson();
				try (Reader reader = Files.newBufferedReader(manifestJson))
				{
					return CachedValueProvider.Result.create(gson.fromJson(reader, Unity3dManifest.class), PsiModificationTracker.MODIFICATION_COUNT);
				}
				catch(Exception e)
				{
					LOGGER.error(e);
				}
			}
			return CachedValueProvider.Result.create(EMPTY, PsiModificationTracker.MODIFICATION_COUNT);
		});
	}

	private Map<String, String> dependencies = Collections.emptyMap();

	public boolean isExcluded(@Nonnull String id)
	{
		return Comparing.equal(dependencies.get(id), "excluded");
	}

	@Nonnull
	public Map<String, Version> getParsedVersions()
	{
		if(dependencies.isEmpty())
		{
			return Collections.emptyMap();
		}
		Map<String, Version> map = new LinkedHashMap<>();
		for(Map.Entry<String, String> entry : dependencies.entrySet())
		{
			if("excluded".equals(entry.getValue()))
			{
				continue;
			}

			Version version = Version.parseVersion(entry.getValue());
			if(version == null)
			{
				continue;
			}
			map.put(entry.getKey(), version);
		}
		return map;
	}
}