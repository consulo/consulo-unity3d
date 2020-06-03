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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import consulo.disposer.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.SmartList;
import com.intellij.util.SystemProperties;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author VISTALL
 * @since 20-Oct-17
 */
@Singleton
public class Unity3dPackageWatcher implements Disposable
{
	@Nonnull
	public static Unity3dPackageWatcher getInstance()
	{
		return Application.get().getComponent(Unity3dPackageWatcher.class);
	}

	private final LocalFileSystem myLocalFileSystem;

	private Set<LocalFileSystem.WatchRequest> myWatchRequests = Collections.emptySet();

	private final List<String> myPackageDirPaths;

	@Inject
	public Unity3dPackageWatcher(VirtualFileManager virtualFileManager)
	{
		myLocalFileSystem = LocalFileSystem.get(virtualFileManager);

		myPackageDirPaths = convertUserPathToPackagePaths();

		if(myPackageDirPaths.isEmpty())
		{
			return;
		}

		myWatchRequests = myLocalFileSystem.addRootsToWatch(myPackageDirPaths, true);
	}

	@Nonnull
	public List<String> getPackageDirPaths()
	{
		return myPackageDirPaths;
	}

	@Override
	public void dispose()
	{
		myLocalFileSystem.removeWatchedRoots(myWatchRequests);
	}

	@Nonnull
	private List<String> convertUserPathToPackagePaths()
	{
		return getUnityUserPaths().stream().map(path -> FileUtil.toSystemIndependentName(path) + "/cache/packages/packages.unity.com/").collect(Collectors.toList());
	}

	@Nonnull
	private static List<String> getUnityUserPaths()
	{
		List<String> paths = new SmartList<>();
		if(SystemInfo.isWinVistaOrNewer)
		{
			paths.add(Shell32Util.getFolderPath(ShlObj.CSIDL_LOCAL_APPDATA) + "\\Unity");

			PointerByReference pointerByReference = new PointerByReference();
			// LocalLow
			WinNT.HRESULT hresult = Shell32.INSTANCE.SHGetKnownFolderPath(Guid.GUID.fromString("{A520A1A4-1780-4FF6-BD18-167343C5AF16}"), 0, null, pointerByReference);

			if(hresult.longValue() == 0)
			{
				paths.add(pointerByReference.getValue().getWideString(0) + "\\Unity");
			}
		}
		else if(SystemInfo.isMac)
		{
			paths.add(SystemProperties.getUserHome() + "/Library/Unity");
		}
		else if(SystemInfo.isLinux)
		{
			paths.add(SystemProperties.getUserHome() + "/.config/unity3d");
		}

		return paths;
	}
}
