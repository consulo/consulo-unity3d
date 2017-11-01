/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.csharp.codeInsight.inspection;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class UnityCompareTagInspection extends LocalInspectionTool
{
	private static class ReplaceByCompareTagFix extends LocalQuickFixOnPsiElement
	{
		private ReplaceByCompareTagFix(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Replace by CompareTag";
		}

		@Override
		@RequiredReadAction
		public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @NotNull PsiElement psiElement, @NotNull PsiElement psiElement1)
		{
			CSharpBinaryExpressionImpl expression = (CSharpBinaryExpressionImpl) psiElement;

			DotNetExpression leftExpression = expression.getLeftExpression();
			if(!(leftExpression instanceof CSharpReferenceExpression))
			{
				return;
			}

			StringBuilder builder = new StringBuilder();

			if(expression.getOperatorElement().getOperatorElementType() == CSharpTokens.NTEQ)
			{
				builder.append("!");
			}

			DotNetExpression qualifier = ((CSharpReferenceExpression) leftExpression).getQualifier();
			if(qualifier != null)
			{
				builder.append(qualifier.getText()).append(".");
			}

			builder.append("CompareTag(");
			builder.append(expression.getRightExpression().getText());
			builder.append(")");

			DotNetExpression methodCall = CSharpFileFactory.createExpression(project, builder.toString());

			expression.replace(methodCall);
		}

		@Nls
		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		if(Unity3dModuleExtensionUtil.getRootModule(holder.getProject()) == null)
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}

		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitBinaryExpression(CSharpBinaryExpressionImpl expression)
			{
				IElementType operatorElementType = expression.getOperatorElement().getOperatorElementType();

				if(operatorElementType == CSharpTokens.EQEQ || operatorElementType == CSharpTokens.NTEQ)
				{
					DotNetExpression leftExpression = expression.getLeftExpression();

					DotNetExpression rightExpression = expression.getRightExpression();
					if(rightExpression == null)
					{
						return;
					}

					if(leftExpression instanceof CSharpReferenceExpression)
					{
						String referenceName = ((CSharpReferenceExpression) leftExpression).getReferenceName();
						if("tag".equals(referenceName))
						{
							PsiElement maybeField = ((CSharpReferenceExpression) leftExpression).resolve();
							if(maybeField instanceof CSharpPropertyDeclaration)
							{

								DotNetExpression qualifier = ((CSharpReferenceExpression) leftExpression).getQualifier();
								CSharpTypeDeclaration target = null;
								if(qualifier == null)
								{
									target = PsiTreeUtil.getParentOfType(leftExpression, CSharpTypeDeclaration.class);
								}
								else if(qualifier instanceof CSharpReferenceExpression)
								{
									PsiElement resolveTarget = ((CSharpReferenceExpression) qualifier).resolve();
									if(resolveTarget instanceof CSharpTypeDeclaration)
									{
										target = (CSharpTypeDeclaration) resolveTarget;
									}
								}

								if(target == null)
								{
									return;
								}

								if(target.isInheritor(Unity3dTypes.UnityEngine.Component, true))
								{
									holder.registerProblem(expression, "Using CompareTag for tag comparison does not cause allocations", new ReplaceByCompareTagFix(expression));
								}
							}
						}
					}
				}
			}
		};
	}
}
