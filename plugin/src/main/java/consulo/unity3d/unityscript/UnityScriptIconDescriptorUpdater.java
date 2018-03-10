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

package consulo.unity3d.unityscript;

import javax.annotation.Nonnull;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.util.BitUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.unityscript.lang.impl.csharp.UnityScriptToNativeElementTransformer;
import consulo.unity3d.unityscript.module.extension.Unity3dScriptModuleExtension;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	@RequiredReadAction
	public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags)
	{
		if(element instanceof JSFile)
		{
			Unity3dScriptModuleExtension moduleExtension = ModuleUtilCore.getExtension(element, Unity3dScriptModuleExtension.class);
			if(moduleExtension == null)
			{
				return;
			}

			iconDescriptor.setMainIcon(AllIcons.Nodes.Class);
			iconDescriptor.addLayerIcon(Unity3dIcons.Js);
			if(BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY))
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_public);
			}
		}
		else if(element instanceof CSharpLightTypeDeclarationBuilder)
		{
			if(element.getUserData(UnityScriptToNativeElementTransformer.JS_MARKER) == Boolean.TRUE)
			{
				PsiElement navigationElement = element.getNavigationElement();
				assert navigationElement != null;

				updateIcon(iconDescriptor, navigationElement, flags);
			}
		}
	}
}
