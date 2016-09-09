package consulo.unity3d.csharp.codeInsight;

import gnu.trove.THashMap;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.Unity3dTypes;
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
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;

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

	@Nullable
	@Override
	@RequiredReadAction
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
				ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent)
						.multiResolve(false));
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
					return new Color(map.get("r"), map.get("g"), map.get("b"), ObjectUtil.<Float>notNull(map.get("a"),
							1f));
				}
			}
		}

		return null;
	}

	@RequiredReadAction
	public static boolean parentIsColorType(PsiElement resolvedElement, @NotNull String type)
	{
		PsiElement typeParent = resolvedElement.getParent();
		return typeParent instanceof CSharpTypeDeclaration && type.equals(((CSharpTypeDeclaration) typeParent)
				.getVmQName());
	}

	@Override
	@RequiredWriteAction
	public void setColorTo(@NotNull PsiElement element, @NotNull Color color)
	{
		PsiElement targetElement = null;
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

		for(Map.Entry<String, Color> entry : staticNames.entrySet())
		{
			if(entry.getValue().equals(color))
			{
				constantName = entry.getKey();
				break;
			}
		}


		boolean qualified = false;
		if(targetElement instanceof CSharpReferenceExpression)
		{
			// for example Color.grey or UnityEngine.Color.grey
			PsiElement qualifier = ((CSharpReferenceExpression) targetElement).getQualifier();
			qualified = qualifier instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) qualifier)
					.getQualifier() != null;
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
			float[] components = color.getRGBComponents(null);

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
}
