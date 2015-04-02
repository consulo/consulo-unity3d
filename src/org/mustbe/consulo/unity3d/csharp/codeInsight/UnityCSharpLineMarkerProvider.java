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

package org.mustbe.consulo.unity3d.csharp.codeInsight;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import org.mustbe.consulo.unity3d.csharp.UnityFunctionManager;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.util.ConstantFunction;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class UnityCSharpLineMarkerProvider implements LineMarkerProvider
{
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		return null;
	}

	@Override
	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result)
	{
		for(PsiElement element : elements)
		{
			LineMarkerInfo marker = createMarker(element);
			if(marker != null)
			{
				result.add(marker);
			}
		}
	}

	@Nullable
	private static LineMarkerInfo createMarker(PsiElement element)
	{
		if(element.getNode().getElementType() == CSharpTokens.IDENTIFIER && element.getParent() instanceof CSharpMethodDeclaration)
		{
			UnityFunctionManager.FunctionInfo functionInfo = UnityFunctionManager.getInstance().getFunctionInfo(element.getText());
			if(functionInfo == null)
			{
				return null;
			}
			Unity3dModuleExtension extension = ModuleUtilCore.getExtension(element, Unity3dModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element.getParent();
			PsiElement maybeTypeDeclaration = methodDeclaration.getParent();
			if(maybeTypeDeclaration instanceof CSharpTypeDeclaration && DotNetInheritUtil.isParent(Unity3dTypes.UnityEngine.MonoBehaviour,
					(DotNetTypeDeclaration) maybeTypeDeclaration, true))
			{
				if(!isEqualParameters(functionInfo.getParameters(), methodDeclaration))
				{
					return null;
				}

				return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), Unity3dIcons.EventMethod, Pass.UPDATE_OVERRIDEN_MARKERS,
						new ConstantFunction<PsiElement, String>(functionInfo.getDescription()), null, GutterIconRenderer.Alignment.LEFT);
			}
		}
		return null;
	}

	private static boolean isEqualParameters(Map<String, DotNetTypeRef> funcParameters, DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] parameters = parameterListOwner.getParameters();
		if(parameters.length == 0)
		{
			return true;
		}
		if(parameters.length != funcParameters.size())
		{
			return false;
		}

		int i = 0;
		for(DotNetTypeRef expectedTypeRef : funcParameters.values())
		{
			DotNetParameter parameter = parameters[i++];

			if(!CSharpTypeUtil.isTypeEqual(parameter.toTypeRef(true), expectedTypeRef, parameter))
			{
				return false;
			}
		}
		return true;
	}
}
