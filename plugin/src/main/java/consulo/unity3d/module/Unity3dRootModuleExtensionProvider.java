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

package consulo.unity3d.module;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import consulo.unity3d.icon.Unity3dIconGroup;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
@ExtensionImpl
public class Unity3dRootModuleExtensionProvider implements ModuleExtensionProvider<Unity3dRootModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "unity3d";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("Unity (Game Engine)");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return Unity3dIconGroup.unity3d();
	}

	@Nonnull
	@Override
	public ModuleExtension<Unity3dRootModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dRootModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<Unity3dRootModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dRootMutableModuleExtension(getId(), moduleRootLayer);
	}
}
