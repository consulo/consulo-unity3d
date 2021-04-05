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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
			VirtualFile[] files = getFiles(rootType);
			return Arrays.stream(files).map(VirtualFile::getUrl).toArray(String[]::new);
		}

		@Nonnull
		@Override
		@RequiredReadAction
		public VirtualFile[] getFiles(@Nonnull OrderRootType rootType)
		{
			List<VirtualFile> files = new ArrayList<>();

			if(myVersion != null)
			{
				String nameWithVersion = myName + "@" + myVersion;

				if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
				{
					LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

					// moved to project files
					String projectPackageCache = getProjectPackageCache();
					VirtualFile localFile = localFileSystem.findFileByIoFile(new File(projectPackageCache, nameWithVersion));
					addDotNetModulesInsideLibrary(files, localFile);

					if(files.isEmpty())
					{
						Unity3dPackageWatcher watcher = Unity3dPackageWatcher.getInstance();
						for(String path : watcher.getPackageDirPaths())
						{
							VirtualFile file = localFileSystem.findFileByIoFile(new File(path, nameWithVersion));
							addDotNetModulesInsideLibrary(files, file);
						}
					}
				}
			}

			if(myFilePointer != null)
			{
				VirtualFile file = myFilePointer.getFile();
				if(file != null)
				{
					files.add(file);
				}
			}

			if(files.isEmpty())
			{
				Sdk sdk = getSdk();
				if(sdk != null)
				{
					String path = Unity3dPackageWatcher.getInstance().getBuiltInPackagesPath(sdk);
					VirtualFile builtInPackageDir = LocalFileSystem.getInstance().findFileByPath(path);
					if(builtInPackageDir != null)
					{
						VirtualFile packageDirectory = builtInPackageDir.findChild(myName);
						ContainerUtil.addIfNotNull(files, packageDirectory);
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
				// more faster check - and it will SOE if file type is unknown
				if(DotNetModuleFileType.isDllFile(fileOrDir.getName()))
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
	};

	private String myName;
	@Nullable
	private String myVersion;
	@Nullable
	private VirtualFilePointer myFilePointer;

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String name, @Nullable String version, @Nullable String url)
	{
		this(rootLayer, name, version, url, true);
	}

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String name, @Nullable String version, @Nullable String url, boolean init)
	{
		super(Unity3dPackageOrderEntryType.getInstance(), rootLayer, ProjectRootManagerImpl.getInstanceImpl(rootLayer.getProject()));
		myName = name;
		myVersion = version;
		myFilePointer = url == null ? null : VirtualFilePointerManager.getInstance().create(url, this, null);

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
		return myName;
	}

	@Nullable
	public String getFileUrl()
	{
		return myFilePointer == null ? null : myFilePointer.getUrl();
	}

	@Nullable
	public String getVersion()
	{
		return myVersion;
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
		return entry instanceof Unity3dPackageOrderEntry && Objects.equals(myName, entry.getPresentableName()) &&
				Objects.equals(myVersion, ((Unity3dPackageOrderEntry) entry).getVersion()) &&
				Objects.equals(getFileUrl(), ((Unity3dPackageOrderEntry) entry).getFileUrl());
	}

	@Override
	public boolean isSynthetic()
	{
		return false;
	}

	@Override
	public OrderEntry cloneEntry(ModuleRootLayerImpl layer)
	{
		return new Unity3dPackageOrderEntry(layer, myName, myVersion, getFileUrl());
	}
}