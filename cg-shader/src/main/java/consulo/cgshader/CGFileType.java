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

package consulo.cgshader;

import consulo.application.AllIcons;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGFileType extends LanguageFileType
{
	public static final CGFileType INSTANCE = new CGFileType();

	public CGFileType()
	{
		super(CGLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "CG";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("CG shader files");
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "cg";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return AllIcons.FileTypes.Text;
	}
}
