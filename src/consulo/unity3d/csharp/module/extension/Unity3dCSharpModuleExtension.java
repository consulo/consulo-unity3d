/*
 * Copyright 2013-2014 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.module.extension.BaseCSharpSimpleModuleExtension;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.roots.ModuleRootLayer;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.module.Unity3dChildModuleExtension;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpModuleExtension extends BaseCSharpSimpleModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
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
	public boolean isSupportedLanguageVersion(@NotNull CSharpLanguageVersion languageVersion)
	{
		return languageVersion == getLanguageVersion();
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		Unity3dChildModuleExtension extension = getModuleRootLayer().getExtension(Unity3dChildModuleExtension.class);
		if(extension != null)
		{
			Sdk sdk = extension.getSdk();
			if(sdk != null)
			{
				Unity3dDefineByVersion version = Unity3dDefineByVersion.find(sdk.getVersionString());
				if(version.ordinal() >= Unity3dDefineByVersion.UNITY_5_5.ordinal())
				{
					return CSharpLanguageVersion._6_0;
				}
			}
		}
		return CSharpLanguageVersion._4_0;
	}
}
