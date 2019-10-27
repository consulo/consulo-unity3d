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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.CharSequenceSubSequence;
import gnu.trove.TIntHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dAssetFileTypeDetector implements FileTypeRegistry.FileTypeDetector
{
	public static final String[] ourAssetExtensionsArray = {
			"unity",
			"prefab",
			"physicsMaterial2D",
			"mat",
			"asset"
	};

	public static final TIntHashSet ourAssetExtensions = new TIntHashSet();

	static
	{
		for(String extension : ourAssetExtensionsArray)
		{
			ourAssetExtensions.add(StringUtil.hashCode(extension));

			if(!ourAssetExtensions.contains(StringUtil.hashCode(new CharSequenceSubSequence(extension))))
			{
				throw new IllegalArgumentException("HashCode is not equals from StringUtil#hashCode() to String#hashCode");
			}
		}
	}

	@Nullable
	@Override
	public FileType detect(@Nonnull VirtualFile file, @Nonnull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText)
	{
		CharSequence extension = FileUtil.getExtension(file.getNameSequence());

		if(ourAssetExtensions.contains(StringUtil.hashCode(extension)))
		{
			if(firstCharsIfText == null)
			{
				return Unity3dBinaryAssetFileType.INSTANCE;
			}
			if(firstCharsIfText.length() > 5)
			{
				if(StringUtil.startsWith(firstCharsIfText, "%YAML"))
				{
					return Unity3dYMLAssetFileType.INSTANCE;
				}
			}

			return Unity3dBinaryAssetFileType.INSTANCE;
		}
		return null;
	}

	@Override
	public int getDesiredContentPrefixLength()
	{
		return 24;
	}

	@Override
	public int getVersion()
	{
		return 6;
	}
}
