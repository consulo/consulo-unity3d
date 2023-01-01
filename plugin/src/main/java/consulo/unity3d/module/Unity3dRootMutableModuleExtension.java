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

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.extension.swing.SwingMutableModuleExtension;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.unity3d.module.ui.UnityConfigurationPanel;
import consulo.util.lang.Comparing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dRootMutableModuleExtension extends Unity3dRootModuleExtension implements DotNetSimpleMutableModuleExtension<Unity3dRootModuleExtension>, SwingMutableModuleExtension
{
	public Unity3dRootMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	public void setNamespacePrefix(@Nullable String prefix)
	{
		myNamespacePrefix = prefix;
	}

	public void setScriptRuntimeVersion(int version)
	{
		scriptRuntimeVersion = version;
	}

	@Nonnull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		return VerticalLayout.create().add(Label.create("Unsupported UI"));
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public JComponent createConfigurablePanel(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
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
		return isModifiedImpl(ex) || !Comparing.equal(getNamespacePrefix(), ex.getNamespacePrefix()) || scriptRuntimeVersion != ex.scriptRuntimeVersion;
	}
}
