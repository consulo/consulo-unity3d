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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import consulo.annotations.RequiredDispatchThread;
import consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;
import consulo.unity3d.module.ui.UnityConfigurationPanel;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dRootMutableModuleExtension extends Unity3dRootModuleExtension implements DotNetSimpleMutableModuleExtension<Unity3dRootModuleExtension>
{
	public Unity3dRootMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	public void setNamespacePrefix(@Nullable String prefix)
	{
		myNamespacePrefix = prefix;
	}

	@Nonnull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@Nonnull Runnable runnable)
	{
		return new UnityConfigurationPanel(this, getVariables(), runnable);
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@Nonnull Unity3dRootModuleExtension ex)
	{
		return isModifiedImpl(ex) || !Comparing.equal(getNamespacePrefix(), ex.getNamespacePrefix());
	}
}
