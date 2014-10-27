package org.mustbe.consulo.unity3d.csharp.module.extension;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpConfigurationPanel;
import org.mustbe.consulo.csharp.module.extension.CSharpMutableModuleExtension;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpMutableModuleExtension extends Unity3dCSharpModuleExtension implements CSharpMutableModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull Runnable runnable)
	{
		return new CSharpConfigurationPanel(this);
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull Unity3dCSharpModuleExtension unity3dCSharpModuleExtension)
	{
		return isModifiedImpl(unity3dCSharpModuleExtension);
	}
}
