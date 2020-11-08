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

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.logging.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dManifest implements Cloneable
{
	private static final Logger LOG = Logger.getInstance(Unity3dManifest.class);

	public static final Unity3dManifest EMPTY = new Unity3dManifest();

	public static class ScopeRegistry
	{
		public String name;
		public String url;
		public String [] scopes;
	}

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
					LOG.warn(e);
				}
			}
			return CachedValueProvider.Result.create(EMPTY, PsiModificationTracker.MODIFICATION_COUNT);
		});
	}

	public static void write(@Nonnull Project project, @Nonnull String text)
	{
		Path projectPath = Paths.get(project.getBasePath());
		Path manifestJson = projectPath.resolve(Paths.get("Packages", "manifest.json"));

		try
		{
			VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(manifestJson.toFile());
			if(file == null)
			{
				Files.write(manifestJson, text.getBytes(StandardCharsets.UTF_8));
				LocalFileSystem.getInstance().refreshAndFindFileByIoFile(manifestJson.toFile());
			}
			else
			{
				file.setBinaryContent(text.getBytes(StandardCharsets.UTF_8));
			}
		}
		catch(IOException e)
		{
			LOG.error(e);
		}
	}

	public ScopeRegistry[] scopedRegistries;

	public Map<String, String> dependencies = Map.of();

	public String registry;

	public boolean isExcluded(@Nonnull String id)
	{
		return Comparing.equal(dependencies.get(id), "excluded");
	}

	@Nonnull
	public Map<String, String> getFilteredDependencies()
	{
		if(dependencies.isEmpty())
		{
			return Collections.emptyMap();
		}
		Map<String, String> map = new LinkedHashMap<>();
		for(Map.Entry<String, String> entry : dependencies.entrySet())
		{
			if("excluded".equals(entry.getValue()))
			{
				continue;
			}
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	@Override
	public Unity3dManifest clone()
	{
		try
		{
			return (Unity3dManifest) super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new Error(e);
		}
	}
}
