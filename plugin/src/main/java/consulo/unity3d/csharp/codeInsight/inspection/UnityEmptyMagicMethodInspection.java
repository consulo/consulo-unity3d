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

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nls;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.csharp.codeInsight.UnityEventCSharpMethodLineMarkerProvider;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 27-Oct-17
 */
public class UnityEmptyMagicMethodInspection extends LocalInspectionTool
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
					PsiElement codeBlock = declaration.getCodeBlock();
					if(codeBlock == null || codeBlock instanceof CSharpBlockStatementImpl && ((CSharpBlockStatementImpl) codeBlock).getStatements().length == 0)
					{
						PsiElement nameIdentifier = declaration.getNameIdentifier();
						assert nameIdentifier != null;
						holder.registerProblem(nameIdentifier, Unity3dBundle.message("empty.magic.method.inspection.message"), new RemoveMethodFix(declaration));
					}
				}
			}
		};
	}
}
