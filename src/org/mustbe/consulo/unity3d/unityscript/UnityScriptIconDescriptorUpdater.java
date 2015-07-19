package org.mustbe.consulo.unity3d.unityscript;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IconDescriptorUpdater;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	@RequiredReadAction
	public void updateIcon(@NotNull IconDescriptor iconDescriptor, @NotNull PsiElement element, int flags)
	{
		if(element instanceof JSFile)
		{
			Module rootModule = Unity3dModuleExtensionUtil.getRootModule(element.getProject());
			if(rootModule == null)
			{
				return;
			}

			iconDescriptor.setMainIcon(AllIcons.Nodes.Class);
			iconDescriptor.addLayerIcon(Unity3dIcons.Js);
		}
	}
}
