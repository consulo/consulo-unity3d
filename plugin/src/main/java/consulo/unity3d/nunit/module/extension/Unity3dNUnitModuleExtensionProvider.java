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

package consulo.unity3d.nunit.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.nunit.icon.NUnitIconGroup;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
@ExtensionImpl
public class Unity3dNUnitModuleExtensionProvider implements ModuleExtensionProvider<Unity3dNUnitModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "unity3d-nunit-child";
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
		return LocalizeValue.localizeTODO("NUnit (Unity)");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return NUnitIconGroup.nunit();
	}

	@Nonnull
	@Override
	public ModuleExtension<Unity3dNUnitModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dNUnitModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<Unity3dNUnitModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new Unity3dNUnitMutableModuleExtension(getId(), moduleRootLayer);
	}
}
