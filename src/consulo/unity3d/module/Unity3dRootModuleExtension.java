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

import java.io.File;
import java.util.List;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import consulo.roots.ModuleRootLayer;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.packages.Unity3dPackage;
import consulo.unity3d.packages.Unity3dPackageIndex;
import consulo.unity3d.packages.Unity3dPackageWatcher;
import consulo.unity3d.projectImport.Unity3dProjectUtil;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dRootModuleExtension extends BaseDotNetSimpleModuleExtension<Unity3dRootModuleExtension>
{
	protected String myNamespacePrefix = null;

	public Unity3dRootModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@NotNull
	@Override
	public DotNetNamespaceGeneratePolicy getNamespaceGeneratePolicy()
	{
		return UnityNamespaceGeneratePolicy.createOrGet(this);
	}

	@RequiredReadAction
	@Override
	public void commit(@NotNull Unity3dRootModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		myNamespacePrefix = mutableModuleExtension.myNamespacePrefix;
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		super.getStateImpl(element);
		if(myNamespacePrefix != null)
		{
			element.setAttribute("namespace-prefix", myNamespacePrefix);
		}
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		super.loadStateImpl(element);
		myNamespacePrefix = element.getAttributeValue("namespace-prefix");
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

	@NotNull
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

	@NotNull
	private List<String> getPathsForLibraries(String homePath, @NotNull Sdk sdk)
	{
		Version version = Unity3dProjectUtil.parseVersion(sdk.getVersionString());

		List<String> list = new SmartList<>();
		if(SystemInfo.isMac)
		{
			list.add(homePath + "/Contents/Frameworks/Managed");
			list.add(homePath + "/Contents/Frameworks/Mono/lib/mono/2.0");

			// actual at unity5.4 beta
			list.add(homePath + "/Contents/Managed");
			list.add(homePath + "/Contents/Mono/lib/mono/2.0");

			// dead path?
			addUnityExtensions(list, version, homePath + "/Contents/Frameworks/UnityExtensions/Unity");
			// actual mac path
			addUnityExtensions(list, version, homePath + "/Contents/UnityExtensions/Unity");
		}
		else if(SystemInfo.isWindows || SystemInfo.isLinux)
		{
			list.add(homePath + "/Editor/Data/Managed");
			list.add(homePath + "/Editor/Data/Mono/lib/mono/2.0");

			addUnityExtensions(list, version, homePath + "/Editor/Data/UnityExtensions/Unity");
		}

		if(version.is(2017, 2))
		{
			Unity3dPackageIndex index = Unity3dPackageWatcher.getInstance().getIndex();
			for(Unity3dPackage unity3dPackage : index.getTopPackages())
			{
				list.add(unity3dPackage.getPath());
			}
		}
		return list;
	}

	private static void addUnityExtensions(List<String> list, @NotNull Version version, String baseDir)
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

	private static void addUnityExtension(List<String> list, @NotNull VirtualFile dir, @NotNull Version version)
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

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return Unity3dBundleType.class;
	}
}
