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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
@ExtensionImpl
public class UnityCompareTagInspection extends UnityLocalInspectionTool
{
	private static class ReplaceByCompareTagFix extends LocalQuickFixOnPsiElement
	{
		private ReplaceByCompareTagFix(@Nonnull PsiElement element)
		{
			super(element);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Replace by CompareTag";
		}

		@Override
		@RequiredReadAction
		public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement psiElement, @Nonnull PsiElement psiElement1)
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
		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly)
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

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "GameObject.tag equality warning";
	}
}
