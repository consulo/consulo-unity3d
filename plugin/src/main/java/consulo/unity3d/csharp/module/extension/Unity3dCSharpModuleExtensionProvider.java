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

package consulo.unity3d.csharp.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
@ExtensionImpl
public class Unity3dCSharpModuleExtensionProvider implements ModuleExtensionProvider<Unity3dCSharpModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "unity3d-csharp-child";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "unity3d-child";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("C# (Unity)");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Nonnull
	@Override
	public ModuleExtension<Unity3dCSharpModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dCSharpModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<Unity3dCSharpModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dCSharpMutableModuleExtension(getId(), moduleRootLayer);
	}
}
