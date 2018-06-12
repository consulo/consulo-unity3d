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

package consulo.unity3d.csharp.codeInsight;

import gnu.trove.THashMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtil;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.ui.shared.ColorValue;
import consulo.ui.shared.RGBColor;
import consulo.ui.style.StandardColors;
import consulo.unity3d.Unity3dTypes;

/**
 * @author VISTALL
 * @since 01.04.2015
 */
@SuppressWarnings("UseJBColor")
public class UnityCSharpStaticElementColorProvider implements ElementColorProvider
{
	private static final Map<String, ColorValue> staticNames = new THashMap<String, ColorValue>()
	{
		{
			put("red", StandardColors.RED.getStaticValue());
			put("green", StandardColors.GREEN.getStaticValue());
			put("blue", StandardColors.BLUE.getStaticValue());
			put("white", StandardColors.WHITE.getStaticValue());
			put("black", StandardColors.BLACK.getStaticValue());
			put("yellow", StandardColors.YELLOW.getStaticValue());
			put("cyan", StandardColors.CYAN.getStaticValue());
			put("magenta", StandardColors.MAGENTA.getStaticValue());
			put("gray", StandardColors.GRAY.getStaticValue());
			put("grey", StandardColors.GRAY.getStaticValue());
			put("clear", new RGBColor(0, 0, 0, 0));
		}
	};

	@Nullable
	@Override
	@RequiredReadAction
	public ColorValue getColorFrom(@Nonnull PsiElement element)
	{
		IElementType elementType = element.getNode().getElementType();
		if(elementType == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpReferenceExpression)
			{
				String referenceName = ((CSharpReferenceExpression) parent).getReferenceName();
				ColorValue color = staticNames.get(referenceName);
				if(color != null)
				{
					PsiElement resolvedElement = ((CSharpReferenceExpression) parent).resolve();
					if(resolvedElement instanceof DotNetVariable)
					{
						if(parentIsColorType(resolvedElement, Unity3dTypes.UnityEngine.Color))
						{
							return color;
						}
					}
				}
			}
		}
		else if(elementType == CSharpTokens.NEW_KEYWORD)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof CSharpNewExpression))
			{
				return null;
			}

			PsiElement resolvedElementMaybeConstructor = ((CSharpNewExpression) parent).resolveToCallable();
			if(!(resolvedElementMaybeConstructor instanceof CSharpConstructorDeclaration))
			{
				return null;
			}

			DotNetType newType = ((CSharpNewExpression) parent).getNewType();
			if(newType == null)
			{
				return null;
			}

			if(parentIsColorType(resolvedElementMaybeConstructor, Unity3dTypes.UnityEngine.Color))
			{
				ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent).multiResolve(false));
				if(!(validResult instanceof MethodResolveResult))
				{
					return null;
				}

				MethodResolvePriorityInfo calcResult = ((MethodResolveResult) validResult).getCalcResult();
				Map<String, Float> map = new HashMap<>(4);
				for(NCallArgument nCallArgument : calcResult.getArguments())
				{
					String parameterName = nCallArgument.getParameterName();
					if(parameterName == null)
					{
						continue;
					}
					CSharpCallArgument callArgument = nCallArgument.getCallArgument();
					if(callArgument == null)
					{
						continue;
					}
					DotNetExpression argumentExpression = callArgument.getArgumentExpression();
					if(argumentExpression == null)
					{
						continue;
					}

					Object value = new ConstantExpressionEvaluator(argumentExpression).getValue();
					if(value instanceof Number)
					{
						float floatValue = ((Number) value).floatValue();
						if(floatValue < 0 || floatValue > 1)
						{
							return null;
						}
						map.put(parameterName, floatValue);
					}
					else
					{
						return null;
					}
				}

				if(map.size() == 3 || map.size() == 4)
				{
					return RGBColor.fromFloatValues(map.get("r"), map.get("g"), map.get("b"), ObjectUtil.<Float>notNull(map.get("a"), 1f));
				}
			}
		}

		return null;
	}

	@RequiredReadAction
	public static boolean parentIsColorType(PsiElement resolvedElement, @Nonnull String type)
	{
		PsiElement typeParent = resolvedElement.getParent();
		return typeParent instanceof CSharpTypeDeclaration && type.equals(((CSharpTypeDeclaration) typeParent).getVmQName());
	}

	@Override
	@RequiredWriteAction
	public void setColorTo(@Nonnull PsiElement element, @Nonnull ColorValue color)
	{
		PsiElement targetElement;
		if(element.getNode().getElementType() == CSharpTokens.NEW_KEYWORD)
		{
			targetElement = PsiTreeUtil.getParentOfType(element, CSharpNewExpression.class);
		}
		else
		{
			targetElement = PsiTreeUtil.getParentOfType(element, CSharpReferenceExpression.class);
		}
		assert targetElement != null;

		String constantName = null;

		for(Map.Entry<String, ColorValue> entry : staticNames.entrySet())
		{
			if(equals(entry.getValue(), color))
			{
				constantName = entry.getKey();
				break;
			}
		}


		boolean qualified;
		if(targetElement instanceof CSharpReferenceExpression)
		{
			// for example Color.grey or UnityEngine.Color.grey
			PsiElement qualifier = ((CSharpReferenceExpression) targetElement).getQualifier();
			qualified = qualifier instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) qualifier).getQualifier() != null;
		}
		else
		{
			CSharpUserType newType = (CSharpUserType) ((CSharpNewExpression) targetElement).getNewType();
			// new Color or new UnityEngine.Color
			qualified = newType.getReferenceExpression().getQualifier() != null;
		}

		StringBuilder builder = new StringBuilder();
		if(constantName == null)
		{
			builder.append("new ");
		}

		if(qualified)
		{
			builder.append(Unity3dTypes.UnityEngine.Color);
		}
		else
		{
			builder.append(StringUtil.getShortName(Unity3dTypes.UnityEngine.Color));
		}

		if(constantName != null)
		{
			builder.append(".").append(constantName);
		}
		else
		{
			builder.append("(");
			float[] components = color.toRGB().getFloatValues();

			builder.append(components[0]).append("f").append(", ");
			builder.append(components[1]).append("f").append(", ");
			builder.append(components[2]).append("f");

			if(components[3] != 1f)
			{
				builder.append(", ");
				builder.append(components[3]).append("f");
			}
			builder.append(")");
		}

		DotNetExpression expression = CSharpFileFactory.createExpression(element.getProject(), builder.toString());

		targetElement.replace(expression);
	}

	private static boolean equals(ColorValue o1, ColorValue o2)
	{
		return o1.toRGB().equals(o2.toRGB());
	}
}
