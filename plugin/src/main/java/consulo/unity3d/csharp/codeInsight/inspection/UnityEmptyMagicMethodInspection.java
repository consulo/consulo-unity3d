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
import consulo.application.WriteAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.csharp.codeInsight.UnityEventCSharpMethodLineMarkerProvider;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 27-Oct-17
 */
@ExtensionImpl
public class UnityEmptyMagicMethodInspection extends UnityLocalInspectionTool
{
	public static class RemoveMethodFix extends LocalQuickFixOnPsiElement
	{
		public RemoveMethodFix(@Nonnull CSharpMethodDeclaration declaration)
		{
			super(declaration);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Remove method";
		}

		@Nls
		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#\\Unity";
		}

		@Override
		public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement psiElement, @Nonnull PsiElement psiElement1)
		{
			WriteAction.run(psiElement::delete);
		}
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Empty magic methods";
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
			public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
			{
				UnityFunctionManager.FunctionInfo magicMethod = UnityEventCSharpMethodLineMarkerProvider.findMagicMethod(declaration);
				if(magicMethod != null)
				{
					PsiElement codeBlock = declaration.getCodeBlock().getElement();
					if(codeBlock == null || codeBlock instanceof CSharpBlockStatementImpl && ((CSharpBlockStatementImpl) codeBlock).getStatements().length == 0)
					{
						PsiElement nameIdentifier = declaration.getNameIdentifier();
						assert nameIdentifier != null;
						holder.registerProblem(nameIdentifier, Unity3dLocalize.emptyMagicMethodInspectionMessage().getValue(), new RemoveMethodFix(declaration));
					}
				}
			}
		};
	}
}
