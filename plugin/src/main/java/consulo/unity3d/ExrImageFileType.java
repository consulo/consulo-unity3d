/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d;

import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.fileType.FileType;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13/09/2021
 */
public class ExrImageFileType implements FileType
{
	public static final FileType INSTANCE = new ExrImageFileType();

	@Nonnull
	@Override
	public String getId()
	{
		return "EXR_IMAGE";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("EXR image files");
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "exr";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return PlatformIconGroup.filetypesAny_type();
	}

	@Override
	public boolean isBinary()
	{
		return true;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}
}
