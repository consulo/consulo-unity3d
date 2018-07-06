/*
 * Copyright 2013-2018 consulo.io
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2018-07-06
 */
public class Unity3dBinaryFileType implements FileType
{
	public static final Unity3dBinaryFileType INSTANCE = new Unity3dBinaryFileType();

	@Nonnull
	@Override
	public String getId()
	{
		return "UNITY_BINARY";
	}

	@Nonnull
	@Override
	public String getDescription()
	{
		return "Unity binary file";
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return AllIcons.FileTypes.Any_type;
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
