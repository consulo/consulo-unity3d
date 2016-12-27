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

package consulo.unity3d.unityscript.codeInsight;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.util.ConstantFunction;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.module.Unity3dModuleExtension;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptLineMarkerProvider implements LineMarkerProvider
{
	@RequiredReadAction
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		return createMarker(element);
	}

	@Nullable
	@RequiredReadAction
	private static LineMarkerInfo createMarker(PsiElement element)
	{
		if(element.getNode().getElementType() == JSTokenTypes.IDENTIFIER && element.getParent() instanceof JSReferenceExpression && element
				.getParent().getParent() instanceof JSFunction)
		{
			UnityFunctionManager functionManager = UnityFunctionManager.getInstance();
			Map<String, UnityFunctionManager.FunctionInfo> map = functionManager.getFunctionsByType().get(Unity3dTypes.UnityEngine.MonoBehaviour);
			if(map == null)
			{
				return null;
			}
			UnityFunctionManager.FunctionInfo functionInfo = map.get(element.getText());
			if(functionInfo == null)
			{
				return null;
			}
			Unity3dModuleExtension extension = ModuleUtilCore.getExtension(element, Unity3dModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			JSFunction jsFunction = (JSFunction) element.getParent().getParent();
			if(jsFunction.getParent() instanceof JSFile)
			{
				if(!isEqualParameters(functionInfo.getParameters(), jsFunction))
				{
					return null;
				}

				return new LineMarkerInfo<>(element, element.getTextRange(), Unity3dIcons.EventMethod, Pass.LINE_MARKERS, new ConstantFunction<>
						(functionInfo.getDescription()), null, GutterIconRenderer.Alignment.LEFT);
			}
		}
		return null;
	}

	private static boolean isEqualParameters(Map<String, String> funcParameters, JSFunction function)
	{
		JSParameter[] parameters = function.getParameterList().getParameters();
		if(parameters.length == 0)
		{
			return true;
		}
		if(parameters.length != funcParameters.size())
		{
			return false;
		}

		/*int i = 0;
		for(DotNetTypeRef expectedTypeRef : funcParameters.values())
		{
			JSParameter parameter = parameters[i++];

			if(!CSharpTypeUtil.isTypeEqual(parameter.toTypeRef(true), expectedTypeRef, parameter))
			{
				return false;
			}
		}   */
		return true;
	}
}
