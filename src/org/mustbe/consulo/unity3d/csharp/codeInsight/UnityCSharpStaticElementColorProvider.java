package org.mustbe.consulo.unity3d.csharp.codeInsight;

import gnu.trove.THashMap;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.unity3d.csharp.UnityTypes;
import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;

/**
 * @author VISTALL
 * @since 01.04.2015
 */
@SuppressWarnings("UseJBColor")
public class UnityCSharpStaticElementColorProvider implements ElementColorProvider
{
	private static final Map<String, Color> staticNames = new THashMap<String, Color>()
	{
		{
			put("red", Color.RED);
			put("green", Color.GREEN);
			put("blue", Color.BLUE);
			put("white", Color.WHITE);
			put("black", Color.BLACK);
			put("yellow", Color.YELLOW);
			put("cyan", Color.CYAN);
			put("magenta", Color.MAGENTA);
			put("gray", Color.GRAY);
			put("grey", Color.GRAY);
			put("clear", new Color(0, 0, 0, 0));
		}
	};

	public static class UnityColor
	{
		private float r;
		private float g;
		private float b;
		private float a;

		public UnityColor(float r, float g, float b, float a)
		{
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
	}

	@Nullable
	@Override
	public Color getColorFrom(@NotNull PsiElement element)
	{
		IElementType elementType = element.getNode().getElementType();
		if(elementType == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpReferenceExpression)
			{
				String referenceName = ((CSharpReferenceExpression) parent).getReferenceName();
				Color color = staticNames.get(referenceName);
				if(color != null)
				{
					PsiElement resolvedElement = ((CSharpReferenceExpression) parent).resolve();
					if(resolvedElement instanceof DotNetVariable)
					{
						if(parentIsColorType(resolvedElement, UnityTypes.UnityEngine.Color))
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

			if(parentIsColorType(resolvedElementMaybeConstructor, UnityTypes.UnityEngine.Color))
			{
				ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent).multiResolve(false));
				if(!(validResult instanceof MethodResolveResult))
				{
					return null;
				}

				MethodCalcResult calcResult = ((MethodResolveResult) validResult).getCalcResult();
				Map<String, Float> map = new HashMap<String, Float>(4);
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
					return new Color(map.get("r"), map.get("g"), map.get("b"), ObjectUtils.<Float>notNull(map.get("a"), 1f));
				}
			}
			else if(parentIsColorType(resolvedElementMaybeConstructor, UnityTypes.UnityEngine.Color32))
			{
				ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent).multiResolve(false));
				if(!(validResult instanceof MethodResolveResult))
				{
					return null;
				}

				MethodCalcResult calcResult = ((MethodResolveResult) validResult).getCalcResult();
				Map<String, Integer> map = new HashMap<String, Integer>(4);
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
						int intValue = ((Number) value).intValue();
						if(intValue < 0 || intValue > 255)
						{
							return null;
						}
						map.put(parameterName, intValue);
					}
					else
					{
						return null;
					}
				}

				if(map.size() == 4)
				{
					return new Color(map.get("r"), map.get("g"), map.get("b"), map.get("a"));
				}
			}
		}

		return null;
	}

	private static boolean parentIsColorType(PsiElement resolvedElement, @NotNull String type)
	{
		PsiElement typeParent = resolvedElement.getParent();
		return typeParent instanceof CSharpTypeDeclaration && type.equals(((CSharpTypeDeclaration) typeParent).getVmQName());
	}

	@Override
	public void setColorTo(@NotNull PsiElement element, @NotNull Color color)
	{

	}
}
