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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.base.module.extension.BaseCSharpSimpleModuleExtension;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.module.content.layer.ModuleRootLayer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpModuleExtension extends BaseCSharpSimpleModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getAssemblyTitle()
	{
		return getModule().getName();
	}

	@Override
	public boolean isSupportedLanguageVersion(@Nonnull CSharpLanguageVersion languageVersion)
	{
		return false;
	}
}
