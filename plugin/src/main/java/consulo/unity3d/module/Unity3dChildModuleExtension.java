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

package consulo.unity3d.module;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.util.ArrayUtil;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.module.extension.ModuleInheritableNamedPointer;
import consulo.module.extension.impl.ModuleExtensionImpl;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 29.03.2015
 */
public class Unity3dChildModuleExtension extends ModuleExtensionImpl<Unity3dChildModuleExtension> implements Unity3dModuleExtension<Unity3dChildModuleExtension>
{
	public Unity3dChildModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetNamespaceGeneratePolicy getNamespaceGeneratePolicy()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getNamespaceGeneratePolicy();
		}
		return UnityNamespaceGeneratePolicy.INSTANCE;
	}

	@Nonnull
	@Override
	public ModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return EmptyModuleInheritableNamedPointer.empty();
	}

	@Nullable
	@Override
	public Sdk getSdk()
	{
		return null;
	}

	@Nullable
	@Override
	public String getSdkName()
	{
		return null;
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		throw new UnsupportedOperationException("Use root module extension");
	}

	@Override
	@RequiredReadAction
	@Nonnull
	public List<String> getVariables()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getVariables();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isSupportCompilation()
	{
		return false;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public Map<String, String> getAvailableSystemLibraries()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getAvailableSystemLibraries();
		}
		return Collections.emptyMap();
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String[] getSystemLibraryUrls(@Nonnull String name, @Nonnull OrderRootType orderRootType)
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getSystemLibraryUrls(name, orderRootType);
		}
		return ArrayUtil.EMPTY_STRING_ARRAY;
	}
}
