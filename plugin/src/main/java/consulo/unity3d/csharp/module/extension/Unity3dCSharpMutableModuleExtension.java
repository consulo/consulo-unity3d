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

package consulo.unity3d.csharp.module.extension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.disposer.Disposable;
import consulo.roots.ModuleRootLayer;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpMutableModuleExtension extends Unity3dCSharpModuleExtension implements CSharpSimpleMutableModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		return null;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@Nonnull Unity3dCSharpModuleExtension mutableModuleExtension)
	{
		return isEnabled() != mutableModuleExtension.isEnabled() ||
				!myLanguageVersionPointer.equals(mutableModuleExtension.getLanguageVersionPointer());
	}
}
