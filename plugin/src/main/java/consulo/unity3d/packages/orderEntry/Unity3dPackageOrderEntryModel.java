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

import consulo.annotation.access.RequiredReadAction;
import consulo.content.OrderRootType;
import consulo.content.RootProvider;
import consulo.content.RootProviderBase;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.unity3d.module.Unity3dChildModuleExtension;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dPackageWatcher;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dPackageOrderEntryModel implements CustomOrderEntryModel
{
	private RootProvider myRootProvider = new RootProviderBase()
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

			if(myFileUrl != null)
			{
				VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(myFileUrl);
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
			return VirtualFileUtil.toVirtualFileArray(files);
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

	private ModuleRootLayer myModuleRootLayer;

	private String myName;
	@Nullable
	private String myVersion;
	@Nullable
	private String myFileUrl;

	public Unity3dPackageOrderEntryModel(String name, @Nullable String version, @Nullable String url)
	{
		myName = name;
		myVersion = version;
		myFileUrl = url;
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

	@Nonnull
	@Override
	public RootProvider getRootProvider()
	{
		return myRootProvider;
	}

	@Override
	public void bind(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		myModuleRootLayer = moduleRootLayer;
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
		return myFileUrl;
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
	public boolean isEquivalentTo(@Nonnull CustomOrderEntryModel model)
	{
		return model instanceof Unity3dPackageOrderEntryModel && Objects.equals(myName, model.getPresentableName()) &&
				Objects.equals(myVersion, ((Unity3dPackageOrderEntryModel) model).getVersion()) &&
				Objects.equals(getFileUrl(), ((Unity3dPackageOrderEntryModel) model).getFileUrl());
	}

	@Override
	public boolean isSynthetic()
	{
		return false;
	}

	@Nonnull
	@Override
	public Unity3dPackageOrderEntryModel clone()
	{
		return new Unity3dPackageOrderEntryModel(myName, myVersion, getFileUrl());
	}
}