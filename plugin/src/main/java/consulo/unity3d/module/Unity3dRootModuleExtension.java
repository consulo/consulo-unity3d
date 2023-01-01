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

package consulo.unity3d.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.platform.Platform;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.Version;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dRootModuleExtension extends BaseDotNetSimpleModuleExtension<Unity3dRootModuleExtension>
{
	private static final int NET_2_TO_3_5 = 0;
	private static final int NET_4_6 = 1;

	protected String myNamespacePrefix = null;
	protected int scriptRuntimeVersion = NET_2_TO_3_5;

	public Unity3dRootModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@Nonnull
	@Override
	public DotNetNamespaceGeneratePolicy getNamespaceGeneratePolicy()
	{
		return UnityNamespaceGeneratePolicy.createOrGet(this);
	}

	@RequiredReadAction
	@Override
	public void commit(@Nonnull Unity3dRootModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		myNamespacePrefix = mutableModuleExtension.myNamespacePrefix;
		scriptRuntimeVersion = mutableModuleExtension.scriptRuntimeVersion;
	}

	@Override
	protected void getStateImpl(@Nonnull Element element)
	{
		super.getStateImpl(element);
		if(myNamespacePrefix != null)
		{
			element.setAttribute("namespace-prefix", myNamespacePrefix);
		}
		if(scriptRuntimeVersion != 0)
		{
			element.setAttribute("script-runtime-version", String.valueOf(scriptRuntimeVersion));
		}
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@Nonnull Element element)
	{
		super.loadStateImpl(element);
		myNamespacePrefix = element.getAttributeValue("namespace-prefix");
		scriptRuntimeVersion = Integer.parseInt(element.getAttributeValue("script-runtime-version", "0"));
	}

	@Nullable
	public String getNamespacePrefix()
	{
		return myNamespacePrefix;
	}

	@Override
	public boolean isSupportCompilation()
	{
		return false;
	}

	@Nonnull
	@Override
	public File[] getFilesForLibraries()
	{
		Sdk sdk = getSdk();
		if(sdk == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		String homePath = sdk.getHomePath();
		if(homePath == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		Collection<String> pathsForLibraries = getPathsForLibraries(homePath, sdk);

		File[] array = EMPTY_FILE_ARRAY;
		for(String pathsForLibrary : pathsForLibraries)
		{
			File dir = new File(pathsForLibrary);
			if(dir.exists())
			{
				File[] files = dir.listFiles();
				if(files != null)
				{
					array = ArrayUtil.mergeArrays(array, files);
				}
			}
		}
		return array;
	}

	public static void collectManagedDirectories(@Nonnull Sdk sdk, @Nonnull Platform.OperatingSystem os, @Nonnull Consumer<String> consumer)
	{
		String sdkHomePath = sdk.getHomePath();
		if(sdkHomePath == null)
		{
			return;
		}

		if(os.isMac())
		{
			consumer.accept(sdkHomePath + "/Contents/Frameworks/Managed");
		}
		else if(os.isWindows() || os.isLinux())
		{
			consumer.accept(sdkHomePath + "/Editor/Data/Managed");
		}
	}

	@Nonnull
	private Collection<String> getPathsForLibraries(String sdkHomePath, @Nonnull Sdk sdk)
	{
		Version version = Unity3dProjectImporter.parseVersion(sdk.getVersionString());

		Platform.OperatingSystem os = Platform.current().os();

		Set<String> paths = new LinkedHashSet<>();

		collectManagedDirectories(sdk, os, path ->
		{
			paths.add(path);

			// Unity Modules directory
			paths.add(path + "/UnityEngine");
		});

		if(os.isMac())
		{
			switch(scriptRuntimeVersion)
			{
				case NET_2_TO_3_5:
					paths.add(sdkHomePath + "/Contents/Frameworks/Mono/lib/mono/2.0");
					// actual at unity5.4 beta
					paths.add(sdkHomePath + "/Contents/Mono/lib/mono/2.0");
					paths.add(sdkHomePath + "/Contents/MonoBleedingEdge/lib/mono/2.0-api");
					break;
				case NET_4_6:
					paths.add(sdkHomePath + "/Contents/MonoBleedingEdge/lib/mono/4.5");
					break;
			}

			// actual at unity5.4 beta
			paths.add(sdkHomePath + "/Contents/Managed");

			// dead path?
			addUnityExtensions(paths, version, sdkHomePath + "/Contents/Frameworks/UnityExtensions/Unity");
			// actual mac path
			addUnityExtensions(paths, version, sdkHomePath + "/Contents/UnityExtensions/Unity");

			// try to resolve external PlaybackEngines
			File homeFile = new File(sdkHomePath);
			if(homeFile.exists())
			{
				File parentFile = homeFile.getParentFile();

				if(parentFile != null)
				{
					File playbackEngines = new File(parentFile, "PlaybackEngines/VuforiaSupport/Managed");
					if(playbackEngines.exists())
					{
						paths.add(playbackEngines.getPath() + "/Runtime");
						paths.add(playbackEngines.getPath() + "/Editor");
					}
				}
			}
		}
		else if(os.isWindows() || os.isLinux())
		{
			switch(scriptRuntimeVersion)
			{
				case NET_2_TO_3_5:
					paths.add(sdkHomePath + "/Editor/Data/Mono/lib/mono/2.0");
					// unity new distribution
					paths.add(sdkHomePath + "/Editor/Data/MonoBleedingEdge/lib/mono/2.0-api");
					break;
				case NET_4_6:
					paths.add(sdkHomePath + "/Editor/Data/MonoBleedingEdge/lib/mono/4.5");
					break;
			}

			addUnityExtensions(paths, version, sdkHomePath + "/Editor/Data/UnityExtensions/Unity");
		}

		if(version.isOrGreaterThan(2017, 2))
		{
			File vuforiaSpport = new File(sdkHomePath, "/Editor/Data/PlaybackEngines/VuforiaSupport");
			if(vuforiaSpport.exists())
			{
				paths.add(sdkHomePath + "/Editor/Data/PlaybackEngines/VuforiaSupport/Managed/Runtime");
				paths.add(sdkHomePath + "/Editor/Data/PlaybackEngines/VuforiaSupport/Managed/Editor");
			}
		}
		return paths;
	}

	private static void addUnityExtensions(Collection<String> list, @Nonnull Version version, String baseDir)
	{
		VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(baseDir);
		if(dir == null)
		{
			return;
		}

		for(VirtualFile virtualFile : dir.getChildren())
		{
			if(virtualFile.isDirectory())
			{
				addUnityExtension(list, virtualFile, version);
			}
		}
	}

	private static void addUnityExtension(Collection<String> list, @Nonnull VirtualFile dir, @Nonnull Version version)
	{
		// UnityUI 4.6.X specific
		// {EXTENSION_NAME}/{VERSION}/{LIBRARY}

		// UnityUI 5.0 specific
		// {EXTENSION_NAME}/{LIBRARY}

		if(version.isOrGreaterThan(5, 0, 0))
		{
			list.add(dir.getPath());
			VirtualFile editorDir = dir.findChild("Editor");
			if(editorDir != null)
			{
				list.add(editorDir.getPath());
			}

			editorDir = dir.findChild("Runtime");
			if(editorDir != null)
			{
				list.add(editorDir.getPath());
			}
		}
		else
		{
			for(VirtualFile child : dir.getChildren())
			{
				if(child.isDirectory())
				{
					list.add(child.getPath());
					VirtualFile editorDir = child.findChild("Editor");
					if(editorDir != null)
					{
						list.add(editorDir.getPath());
					}
				}
			}
		}
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return Unity3dBundleType.class;
	}
}
