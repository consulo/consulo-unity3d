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

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import consulo.platform.Platform;
import consulo.roots.ModuleRootLayer;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dProjectImportUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dRootModuleExtension extends BaseDotNetSimpleModuleExtension<Unity3dRootModuleExtension>
{
	private static final int NET_2_TO_3_5 = 0;
	private static final int NET_4_6 = 1;

	protected String myNamespacePrefix = null;
	protected int scriptRuntimeVersion;

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

		List<String> pathsForLibraries = getPathsForLibraries(homePath, sdk);

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

	@Nonnull
	private List<String> getPathsForLibraries(String homePath, @Nonnull Sdk sdk)
	{
		Version version = Unity3dProjectImportUtil.parseVersion(sdk.getVersionString());

		Platform.OperatingSystem os = Platform.current().os();

		List<String> list = new SmartList<>();
		if(os.isMac())
		{
			list.add(homePath + "/Contents/Frameworks/Managed");
			switch(scriptRuntimeVersion)
			{
				case NET_2_TO_3_5:
					list.add(homePath + "/Contents/Frameworks/Mono/lib/mono/2.0");
					// actual at unity5.4 beta
					list.add(homePath + "/Contents/Mono/lib/mono/2.0");
					break;
				case NET_4_6:
					list.add(homePath + "/Contents/MonoBleedingEdge/lib/mono/4.5");
					break;
			}

			// actual at unity5.4 beta
			list.add(homePath + "/Contents/Managed");

			// dead path?
			addUnityExtensions(list, version, homePath + "/Contents/Frameworks/UnityExtensions/Unity");
			// actual mac path
			addUnityExtensions(list, version, homePath + "/Contents/UnityExtensions/Unity");

			// try to resolve external PlaybackEngines
			File homeFile = new File(homePath);
			if(homeFile.exists())
			{
				File parentFile = homeFile.getParentFile();

				if(parentFile != null)
				{
					File playbackEngines = new File(parentFile, "PlaybackEngines/VuforiaSupport/Managed");
					if(playbackEngines.exists())
					{
						list.add(playbackEngines.getPath() + "/Runtime");
						list.add(playbackEngines.getPath() + "/Editor");
					}
				}
			}
		}
		else if(os.isWindows() || SystemInfo.isLinux)
		{
			list.add(homePath + "/Editor/Data/Managed");
			switch(scriptRuntimeVersion)
			{
				case NET_2_TO_3_5:
					list.add(homePath + "/Editor/Data/Mono/lib/mono/2.0");
					break;
				case NET_4_6:
					list.add(homePath + "/Editor/Data/MonoBleedingEdge/lib/mono/4.5");
					break;
			}

			addUnityExtensions(list, version, homePath + "/Editor/Data/UnityExtensions/Unity");
		}

		if(version.isOrGreaterThan(2017, 2))
		{
			File vuforiaSpport = new File(homePath, "/Editor/Data/PlaybackEngines/VuforiaSupport");
			if(vuforiaSpport.exists())
			{
				list.add(homePath + "/Editor/Data/PlaybackEngines/VuforiaSupport/Managed/Runtime");
				list.add(homePath + "/Editor/Data/PlaybackEngines/VuforiaSupport/Managed/Editor");
			}
		}
		return list;
	}

	private static void addUnityExtensions(List<String> list, @Nonnull Version version, String baseDir)
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

	private static void addUnityExtension(List<String> list, @Nonnull VirtualFile dir, @Nonnull Version version)
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
