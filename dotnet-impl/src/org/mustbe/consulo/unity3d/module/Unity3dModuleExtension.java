package org.mustbe.consulo.unity3d.module;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.dotnet.module.extension.BaseDotNetModuleExtension;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dModuleExtension extends BaseDotNetModuleExtension<Unity3dModuleExtension>
{
	public Unity3dModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
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

		String pathForMono = Unity3dBundleType.getPathForMono(homePath, getLibrarySuffix());

		File[] array = EMPTY_FILE_ARRAY;

		File dir = new File(pathForMono);
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			if(files != null)
			{
				array = ArrayUtil.mergeArrays(array, files);
			}
		}

		String managedPath = Unity3dBundleType.getManagedPath(homePath, getLibrarySuffix());

		dir = new File(managedPath);
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			if(files != null)
			{
				array = ArrayUtil.mergeArrays(array, files);
			}
		}
		return array;
	}

	@NotNull
	public String getLibrarySuffix()
	{
		return "unity";
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return Unity3dBundleType.class;
	}

	@NotNull
	@Override
	public GeneralCommandLine createDefaultCommandLine(@NotNull String s, @Nullable DebugConnectionInfo debugConnectionInfo)
	{
		return new GeneralCommandLine();
	}

	@NotNull
	@Override
	public String getDebugFileExtension()
	{
		return ".mdb";
	}
}
