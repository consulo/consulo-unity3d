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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.roots.OrderEntryWithTracking;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.module.Unity3dChildModuleExtension;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dPackageWatcher;

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
		public String[] getUrls(@Nonnull OrderRootType rootType)
		{
			List<String> urls = new ArrayList<>();
			// moved to project files
			if(getVersion().ordinal() >= Unity3dDefineByVersion.UNITY_2018_3.ordinal())
			{
				if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
				{
					String projectPackageCache = getProjectPackageCache();
					if(new File(projectPackageCache, myName).exists())
					{
						urls.add(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(projectPackageCache + "/" + myName));
					}
				}
			}
			else
			{
				Unity3dPackageWatcher watcher = Unity3dPackageWatcher.getInstance();
				if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
				{
					for(String path : watcher.getPackageDirPaths())
					{
						if(new File(path, myName).exists())
						{
							urls.add(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(path + "/" + myName));
						}
					}
				}
			}

			return ArrayUtil.toStringArray(urls);
		}

		@Nonnull
		@Override
		public VirtualFile[] getFiles(@Nonnull OrderRootType rootType)
		{
			List<VirtualFile> files = new ArrayList<>();
			LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
			// moved to project files
			if(getVersion().ordinal() >= Unity3dDefineByVersion.UNITY_2018_3.ordinal())
			{
				if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
				{
					String projectPackageCache = getProjectPackageCache();
					VirtualFile file = localFileSystem.findFileByIoFile(new File(projectPackageCache, myName));
					ContainerUtil.addIfNotNull(files, file);
				}
			}
			else
			{
				Unity3dPackageWatcher watcher = Unity3dPackageWatcher.getInstance();
				if(rootType == BinariesOrderRootType.getInstance() || rootType == SourcesOrderRootType.getInstance())
				{
					for(String path : watcher.getPackageDirPaths())
					{
						VirtualFile file = localFileSystem.findFileByIoFile(new File(path, myName));
						ContainerUtil.addIfNotNull(files, file);
					}
				}
			}
			return VfsUtilCore.toVirtualFileArray(files);
		}

		@Nonnull
		private String getProjectPackageCache()
		{
			return myModuleRootLayer.getProject().getBasePath() + "/Library/PackageCache";
		}

		@Nonnull
		private Unity3dDefineByVersion getVersion()
		{
			Unity3dRootModuleExtension extension = myModuleRootLayer.getExtension(Unity3dRootModuleExtension.class);
			if(extension == null)
			{
				return Unity3dDefineByVersion.UNKNOWN;
			}
			Sdk sdk = extension.getSdk();
			if(sdk == null)
			{
				return Unity3dDefineByVersion.UNKNOWN;
			}
			return Unity3dDefineByVersion.find(sdk.getVersionString());
		}
	};

	private String myName;

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String name)
	{
		this(rootLayer, name, true);
	}

	public Unity3dPackageOrderEntry(@Nonnull ModuleRootLayerImpl rootLayer, String name, boolean init)
	{
		super(Unity3dPackageOrderEntryType.getInstance(), rootLayer, ProjectRootManagerImpl.getInstanceImpl(rootLayer.getProject()));
		myName = name;
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
		return entry instanceof Unity3dPackageOrderEntry && Comparing.equal(myName, entry.getPresentableName());
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