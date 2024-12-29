/*
 * Copyright 2013-2022 consulo.io
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

package consulo.unity3d.uss;

import consulo.css.lang.CssLanguage;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.unity3d.uss.icon.UssIconGroup;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
public class USSFileType extends LanguageFileType
{
	public static final USSFileType INSTANCE = new USSFileType();

	private USSFileType()
	{
		super(CssLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "UNITY_CSS";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("Unity Style Sheet");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return UssIconGroup.ussfile();
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "uss";
	}
}
