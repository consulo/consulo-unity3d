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

package consulo.unity3d.nunit.module.extension;

import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionBase;
import consulo.nunit.module.extension.NUnitSimpleModuleExtension;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.01.2016
 */
public class Unity3dNUnitModuleExtension extends ModuleExtensionBase<Unity3dNUnitModuleExtension> implements NUnitSimpleModuleExtension<Unity3dNUnitModuleExtension>
{
	public Unity3dNUnitModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}
}
