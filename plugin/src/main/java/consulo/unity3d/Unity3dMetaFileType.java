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

package consulo.unity3d;

import consulo.language.file.LanguageFileType;
import consulo.language.plain.PlainTextFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jetbrains.yaml.YAMLLanguage;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.03.2015
 */
public class Unity3dMetaFileType extends LanguageFileType
{
	public static final Unity3dMetaFileType INSTANCE = new Unity3dMetaFileType();

	public Unity3dMetaFileType()
	{
		super(YAMLLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "META";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("Meta files");
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "meta";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return PlainTextFileType.INSTANCE.getIcon();
	}
}
