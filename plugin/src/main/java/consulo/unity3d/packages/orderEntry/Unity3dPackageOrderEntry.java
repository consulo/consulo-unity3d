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

package consulo.unity3d.packages.orderEntry;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootPolicy;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.ClonableOrderEntry;
import com.intellij.openapi.roots.impl.LibraryOrderEntryBaseImpl;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.roots.impl.RootProviderBaseImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.platform.Platform;
import consulo.roots.OrderEntryWithTracking;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.unity3d.module.Unity3dChildModuleExtension;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dPackageWatcher;
import consulo.vfs.util.ArchiveVfsUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dPackageOrderEntry extends LibraryOrderEntryBaseImpl implements ClonableOrderEntry, OrderEntryWithTracking
{
	private RootProvider myRootProvider = new RootProviderBaseImpl()
	{
		@Nonnull
		@Override
		@RequiredReadAction
		public String[] getUrls(@Nonnull OrderRootType rootType)
		{
			List<String> urls = new SmartList<>();

			if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
			{
				// moved to project files
				String projectPackageCache = getProjectPackageCache();
				if(new File(projectPackageCache, myNameWithVersion).exists())
				{
					urls.add(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(projectPackageCache + "/" + myNameWithVersion));
				}

				if(urls.isEmpty())
				{
					Unity3dPackageWatcher watcher = Unity3dPackageWatcher.getInstance();
					for(String path : watcher.getPackageDirPaths())
					{
						if(new File(path, myNameWithVersion).exists())
						{
							urls.add(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(path + "/" + myNameWithVersion));
						}
					}
				}
			}

			if(urls.isEmpty())
			{
				Sdk sdk = getSdk();
				if(sdk != null)
				{
					String builtInPath = sdk.getHomePath() + "/" + getBuiltInPackagesRelativePath();
					String packageName = getPackageName();
					if(new File(builtInPath, packageName).exists())
					{
						urls.add(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(builtInPath + "/" + packageName));
					}
				}
			}

			return ArrayUtil.toStringArray(urls);
		}

		@Nonnull
		@Override
		@RequiredReadAction
		public VirtualFile[] getFiles(@Nonnull OrderRootType rootType)
		{
			List<VirtualFile> files = new ArrayList<>();

			if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
			{
				LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

				// moved to project files
				String projectPackageCache = getProjectPackageCache();
				VirtualFile localFile = localFileSystem.findFileByIoFile(new File(projectPackageCache, myNameWithVersion));
				addDotNetModulesInsideLibrary(files, localFile);

				if(files.isEmpty())
				{
					Unity3dPackageWatcher watcher = Unity3dPackageWatcher.getInstance();
					for(String path : watcher.getPackageDirPaths())
					{
						VirtualFile file = localFileSystem.findFileByIoFile(new File(path, myNameWithVersion));
						addDotNetModulesInsideLibrary(files, file);
					}
				}
			}

			if(files.isEmpty())
			{
				Sdk sdk = getSdk();
				if(sdk != null)
				{
					VirtualFile homeDirectory = sdk.getHomeDirectory();
					if(homeDirectory != null)
					{
						VirtualFile builtInDirectory = homeDirectory.findFileByRelativePath(getBuiltInPackagesRelativePath());
						if(builtInDirectory != null)
						{
							VirtualFile packageDirectory = builtInDirectory.findChild(getPackageName());
							ContainerUtil.addIfNotNull(files, packageDirectory);
						}
					}
				}
			}
			return VfsUtilCore.toVirtualFileArray(files);
		}

		private void addDotNetModulesInsideLibrary(@Nonnull List<VirtualFile> result, @Nullable VirtualFile virtualFile)
		{
			if(virtualFile == null)
			{
				return;
			}

			result.add(virtualFile);

			boolean isEditor = StringUtil.contains(myModuleRootLayer.getModule().getName(), "Editor");
			if(virtualFile.isDirectory())
			{
				addChildrenNetModules(virtualFile, result);

				if(isEditor)
				{
					VirtualFile editorDirectory = virtualFile.findChild("Editor");
					if(editorDirectory != null)
					{
						addChildrenNetModules(editorDirectory, result);
					}
				}
			}
		}

		private void addChildrenNetModules(VirtualFile virtualFile, List<VirtualFile> result)
		{
			for(VirtualFile fileOrDir : virtualFile.getChildren())
			{
				if(fileOrDir.getFileType() == DotNetModuleFileType.INSTANCE)
				{
					VirtualFile archiveRoot = ArchiveVfsUtil.getArchiveRootForLocalFile(fileOrDir);
					if(archiveRoot != null)
					{
						result.add(archiveRoot);
					}
				}
			}
		}

		@Nonnull
		private String getProjectPackageCache()
		{
			return myModuleRootLayer.getProject().getBasePath() + "/Library/PackageCache";
		}

		@Nonnull
		private String getBuiltInPackagesRelativePath()
		{
			if(Platform.current().os().isMac())
			{
				return "Contents/Resources/PackageManager/BuiltInPackages";
			}
			return "Editor/Data/Resources/PackageManager/BuiltInPackages";
		}

		@Nullable
		@RequiredReadAction
		private Sdk getSdk()
		{
			// FIXME [VISTALL] we can't access to another module from roots and we don't need target module for UI, that mean we don't care about then we call that code
			Unity3dRootModuleExtension extension = Unity3dModuleExtensionUtil.getRootModuleExtension(myModuleRootLayer.getProject());
			if(extension == null)
			{
				return null;
			}
			return extension.getSdk();
		}

		@Nonnull
		private String getPackageName()
		{
			List<String> values = StringUtil.split(myNameWithVersion, "@");
			if(values.size() != 2)
			{
				return myNameWithVersion;
			}
			return values.get(0);
		}
	};

	private String myNameWithVersion;

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String nameWithVersion)
	{
		this(rootLayer, nameWithVersion, true);
	}

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String nameWithVersion, boolean init)
	{
		super(Unity3dPackageOrderEntryType.getInstance(), rootLayer, ProjectRootManagerImpl.getInstanceImpl(rootLayer.getProject()));
		myNameWithVersion = nameWithVersion;
		if(init)
		{
			init();

			myProjectRootManagerImpl.addOrderWithTracking(this);
		}
	}

	@Nullable
	@Override
	public Object getEqualObject()
	{
		Unity3dRootModuleExtension extension = myModuleRootLayer.getExtension(Unity3dRootModuleExtension.class);
		if(extension == null)
		{
			return ObjectUtil.NULL;
		}
		return extension.getSdk();
	}

	@Nullable
	@Override
	public RootProvider getRootProvider()
	{
		return myRootProvider;
	}

	@Nonnull
	@Override
	public String getPresentableName()
	{
		return myNameWithVersion;
	}

	@Override
	public boolean isValid()
	{
		return myModuleRootLayer.getExtension(Unity3dChildModuleExtension.class) != null;
	}

	@Override
	public <R> R accept(RootPolicy<R> rRootPolicy, @Nullable R r)
	{
		return rRootPolicy.visitOrderEntry(this, r);
	}

	@Override
	public boolean isEquivalentTo(@Nonnull OrderEntry entry)
	{
		return entry instanceof Unity3dPackageOrderEntry && Comparing.equal(myNameWithVersion, entry.getPresentableName());
	}

	@Override
	public boolean isSynthetic()
	{
		return false;
	}

	@Override
	public OrderEntry cloneEntry(ModuleRootLayerImpl layer)
	{
		return new Unity3dPackageOrderEntry(layer, getPresentableName());
	}
}