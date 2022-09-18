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

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.unity3d.shaderlab.icon.ShaderLabIconGroup;
import org.jetbrains.yaml.YAMLLanguage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dYMLAssetFileType extends LanguageFileType
{
	public static final Unity3dYMLAssetFileType INSTANCE = new Unity3dYMLAssetFileType();

	private Unity3dYMLAssetFileType()
	{
		super(YAMLLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "UNITY_YML_ASSET";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("Unity yml asset file");
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
		return ShaderLabIconGroup.shader();
	}
}
