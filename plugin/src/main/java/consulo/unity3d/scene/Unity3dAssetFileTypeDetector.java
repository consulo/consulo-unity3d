/*
 * Copyright 2013-2016 consulo.io
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

package consulo.unity3d.scene;

import consulo.annotation.component.ExtensionImpl;
import consulo.util.io.ByteSequence;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeDetector;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
@ExtensionImpl
public class Unity3dAssetFileTypeDetector implements FileTypeDetector
{
	public static final List<String> ourAssetExtensions = List.of("unity", "prefab", "physicsMaterial2D", "mat", "asset", "anim", "controller", "spriteatlas", "mesh", "physicMaterial", "preset", "mask");

	@Nullable
	@Override
	public FileType detect(@Nonnull VirtualFile file, @Nonnull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText)
	{
		if(firstCharsIfText == null || firstCharsIfText.length() < 5)
		{
			return null;
		}

		String extension = file.getExtension();
		if(extension == null || !ourAssetExtensions.contains(extension))
		{
			return null;
		}

		if(StringUtil.startsWith(firstCharsIfText, "%YAML"))
		{
			return Unity3dYMLAssetFileType.INSTANCE;
		}

		return null;
	}

	@Nullable
	@Override
	public Collection<? extends FileType> getDetectedFileTypes()
	{
		return List.of(Unity3dYMLAssetFileType.INSTANCE);
	}

	@Override
	public int getDesiredContentPrefixLength()
	{
		return 20;
	}

	@Override
	public int getVersion()
	{
		return 11;
	}
}
