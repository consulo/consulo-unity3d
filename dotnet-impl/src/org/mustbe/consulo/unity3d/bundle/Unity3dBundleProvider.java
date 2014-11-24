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
