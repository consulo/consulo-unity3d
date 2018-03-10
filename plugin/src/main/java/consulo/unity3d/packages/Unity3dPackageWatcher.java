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

package consulo.unity3d.packages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author VISTALL
 * @since 20-Oct-17
 */
public class Unity3dPackageWatcher implements ApplicationComponent, Disposable
{
	@Nonnull
	public static Unity3dPackageWatcher getInstance()
	{
		return ApplicationManager.getApplication().getComponent(Unity3dPackageWatcher.class);
	}

	private LocalFileSystem.WatchRequest myWatchRequest;

	private volatile Unity3dPackageIndex myIndex;

	private String myPackageDirPath;

	@Override
	public void initComponent()
	{
		String packagePath = getPackagePath();
		if(packagePath == null)
		{
			return;
		}

		myPackageDirPath = packagePath;

		myWatchRequest = LocalFileSystem.getInstance().addRootToWatch(packagePath, true);

		VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener()
		{
			@Override
			public void fileCopied(@Nonnull VirtualFileCopyEvent event)
			{
				dropCache(event);
			}

			@Override
			public void fileCreated(@Nonnull VirtualFileEvent event)
			{
				dropCache(event);
			}

			@Override
			public void fileMoved(@Nonnull VirtualFileMoveEvent event)
			{
				dropCache(event);
			}

			@Override
			public void fileDeleted(@Nonnull VirtualFileEvent event)
			{
				dropCache(event);
			}

			private void dropCache(VirtualFileEvent event)
			{
				VirtualFile packageDir = LocalFileSystem.getInstance().findFileByPath(packagePath);
				if(packageDir == null)
				{
					return;
				}
				if(VfsUtil.isAncestor(packageDir, event.getFile(), false))
				{
					myIndex = null;
				}
			}
		}, this);
	}

	@Nonnull
	public Unity3dPackageIndex getIndex()
	{
		if(myPackageDirPath == null)
		{
			return Unity3dPackageIndex.EMPTY;
		}

		Unity3dPackageIndex index = myIndex;
		if(index != null)
		{
			return index;
		}

		Unity3dPackageIndex newIndex = buildIndex();
		myIndex = newIndex;
		return newIndex;
	}

	@Nonnull
	private Unity3dPackageIndex buildIndex()
	{
		VirtualFile rootDir = LocalFileSystem.getInstance().findFileByPath(myPackageDirPath);
		if(rootDir == null)
		{
			return Unity3dPackageIndex.EMPTY;
		}

		MultiMap<String, Unity3dPackage> map = MultiMap.createOrderedSet();

		for(VirtualFile packageDir : rootDir.getChildren())
		{
			CharSequence name = packageDir.getNameSequence();

			List<CharSequence> split = StringUtil.split(name, "@");
			if(split.size() != 2)
			{
				continue;
			}

			CharSequence id = split.get(0);
			CharSequence version = split.get(1);

			Version parsedVersion = Version.parseVersion(version.toString());
			if(parsedVersion == null)
			{
				continue;
			}

			String idAsString = id.toString();
			map.putValue(idAsString, new Unity3dPackage(idAsString, parsedVersion, packageDir.getPath()));
		}

		if(map.isEmpty())
		{
			return Unity3dPackageIndex.EMPTY;
		}

		List<Unity3dPackage> topPackages = new ArrayList<>();
		for(Map.Entry<String, Collection<Unity3dPackage>> entry : map.entrySet())
		{
			// hack for get last element
			List<Unity3dPackage> value = (List<Unity3dPackage>) entry.getValue();

			Unity3dPackage lastItem = ContainerUtil.getLastItem(value);
			if(lastItem == null)
			{
				continue;
			}

			topPackages.add(lastItem);
		}

		return new Unity3dPackageIndex(topPackages);
	}

	@Override
	public void dispose()
	{
		LocalFileSystem.getInstance().removeWatchedRoot(myWatchRequest);
	}

	@Nullable
	private static String getPackagePath()
	{
		String unityUserPath = getUnityUserPath();
		if(unityUserPath == null)
		{
			return null;
		}
		unityUserPath = FileUtil.toSystemIndependentName(unityUserPath) + "/cache/packages/packages.unity.com/";
		return unityUserPath;
	}

	@Nullable
	private static String getUnityUserPath()
	{
		if(SystemInfo.isWinVistaOrNewer)
		{
			PointerByReference pointerByReference = new PointerByReference();
			WinNT.HRESULT hresult = Shell32.INSTANCE.SHGetKnownFolderPath(Guid.GUID.fromString("{A520A1A4-1780-4FF6-BD18-167343C5AF16}"), 0, null, pointerByReference);

			if(hresult.longValue() != 0)
			{
				return null;
			}
			return pointerByReference.getValue().getWideString(0) + "\\Unity";
		}
		else if(SystemInfo.isMac)
		{
			return SystemProperties.getUserHome() + "/Library/Unity";
		}
		else if(SystemInfo.isLinux)
		{
			return SystemProperties.getUserHome() + "/.config/unity3d";
		}

		return null;
	}
}
