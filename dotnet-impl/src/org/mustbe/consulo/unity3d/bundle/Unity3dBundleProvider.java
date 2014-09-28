package org.mustbe.consulo.unity3d.bundle;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.projectRoots.BundledSdkProvider;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dBundleProvider implements BundledSdkProvider
{
	@NotNull
	@Override
	public Sdk[] createBundledSdks()
	{
		Unity3dBundleType unity3dBundleType = Unity3dBundleType.getInstance();
		String suggestHomePath = unity3dBundleType.suggestHomePath();
		if(suggestHomePath == null)
		{
			return Sdk.EMPTY_ARRAY;
		}

		File file = new File(suggestHomePath);
		if(file.exists())
		{
			VirtualFile dirApp = LocalFileSystem.getInstance().findFileByIoFile(file);
			if(dirApp == null)
			{
				return Sdk.EMPTY_ARRAY;
			}

			String path = dirApp.getPath();
			if(!unity3dBundleType.isValidSdkHome(path))
			{
				return Sdk.EMPTY_ARRAY;
			}

			SdkImpl sdk = new SdkImpl("Unity3D (bundled)", unity3dBundleType);
			sdk.setHomePath(path);
			sdk.setVersionString(unity3dBundleType.getVersionString(path));
			return new Sdk[]{sdk};
		}
		return Sdk.EMPTY_ARRAY;
	}
}
