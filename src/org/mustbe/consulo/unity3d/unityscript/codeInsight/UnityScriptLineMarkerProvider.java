package org.mustbe.consulo.unity3d.unityscript.codeInsight;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.csharp.UnityFunctionManager;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
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

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptLineMarkerProvider implements LineMarkerProvider
{
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		return null;
	}

	@Override
	@RequiredReadAction
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
	@RequiredReadAction
	private static LineMarkerInfo createMarker(PsiElement element)
	{
		if(element.getNode().getElementType() == JSTokenTypes.IDENTIFIER && element.getParent() instanceof JSReferenceExpression && element
				.getParent().getParent() instanceof JSFunction)
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
			JSFunction jsFunction = (JSFunction) element.getParent().getParent();
			if(jsFunction.getParent() instanceof JSFile)
			{
				if(!isEqualParameters(functionInfo.getParameters(), jsFunction))
				{
					return null;
				}

				return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), Unity3dIcons.EventMethod, Pass.UPDATE_OVERRIDEN_MARKERS,
						new ConstantFunction<PsiElement, String>(functionInfo.getDescription()), null, GutterIconRenderer.Alignment.LEFT);
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
