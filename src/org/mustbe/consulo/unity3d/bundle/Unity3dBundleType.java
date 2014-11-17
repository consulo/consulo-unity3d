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
		return "0.0"; //TODO [VISTALL] get version
	}

	@Override
	public String suggestSdkName(String s, String s2)
	{
		return getPresentableName();
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "Unity3D";
	}
}
