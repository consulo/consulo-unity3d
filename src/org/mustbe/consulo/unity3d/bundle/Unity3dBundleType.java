/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.bundle;

import java.io.File;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dBundleType extends SdkType
{
	@NotNull
	public static String getApplicationPath(@NotNull String sdkPath)
	{
		if(SystemInfo.isMac)
		{
			return sdkPath + "/Contents/MacOS/Unity";
		}
		else if(SystemInfo.isWindows)
		{
			return sdkPath + "/Editor/Unity.exe";
		}
		throw new IllegalArgumentException("Unknown system " + SystemInfo.OS_NAME);
	}

	@NotNull
	public static String getPathForMono(@NotNull String sdkPath, @NotNull String suffix)
	{
		if(SystemInfo.isMac)
		{
			return sdkPath + "/Contents/Frameworks/Mono/lib/mono/" + suffix;
		}
		else if(SystemInfo.isWindows)
		{
			return sdkPath + "/Editor/Data/Mono/lib/mono/" + suffix;
		}
		throw new IllegalArgumentException("Unknown system " + SystemInfo.OS_NAME);
	}

	@NotNull
	public static String getManagedPath(@NotNull String sdkPath, @NotNull String suffix)
	{
		if(SystemInfo.isMac)
		{
			return sdkPath + "/Contents/Frameworks/Managed";
		}
		else if(SystemInfo.isWindows)
		{
			return sdkPath + "/Editor/Data/Managed";
		}
		throw new IllegalArgumentException("Unknown system " + SystemInfo.OS_NAME);
	}

	@NotNull
	public static Unity3dBundleType getInstance()
	{
		return EP_NAME.findExtension(Unity3dBundleType.class);
	}

	public Unity3dBundleType()
	{
		super("UNITY3D");
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Nullable
	@Override
	public String suggestHomePath()
	{
		if(SystemInfo.isMac)
		{
			return "/Applications/Unity/Unity.app";
		}
		else if(SystemInfo.isWindows)
		{
			return "C:/Program Files (x86)/Unity";
		}
		return null;
	}

	@Override
	public boolean isValidSdkHome(String s)
	{
		return new File(getPathForMono(s, "unity")).exists();
	}

	@Nullable
	@Override
	public String getVersionString(String s)
	{
		if(SystemInfo.isWindows)
		{
			return WindowsVersionHelper.getVersion(s + "/Editor/Unity.exe");
		}
		return "0.0"; //TODO [VISTALL] get version
	}

	@Override
	public String suggestSdkName(String s, String sdkHome)
	{
		return getPresentableName() + " " + getVersionString(sdkHome);
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "Unity3D";
	}
}
