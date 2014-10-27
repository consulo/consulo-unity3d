package org.mustbe.consulo.unity3d.module;

import javax.swing.JComponent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.ui.UnityConfigurationPanel;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dMutableModuleExtension extends Unity3dModuleExtension implements DotNetMutableModuleExtension<Unity3dModuleExtension>
{
	public Unity3dMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull Runnable runnable)
	{
		return new UnityConfigurationPanel(this, getVariables(), runnable);
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull Unity3dModuleExtension unity3dModuleExtension)
	{
		return isModifiedImpl(unity3dModuleExtension);
	}
}
