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
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.csharp.codeInsight.UnityEventCSharpMethodLineMarkerProvider;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 27-Oct-17
 */
@ExtensionImpl
public class UnityOnGUIMethodInspection extends UnityLocalInspectionTool
{
	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "OnGUI magic method";
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
				String name = declaration.getName();
				if("OnGUI".equals(name))
				{
					UnityFunctionManager.FunctionInfo magicMethod = UnityEventCSharpMethodLineMarkerProvider.findMagicMethod(declaration);
					if(magicMethod != null)
					{
						PsiElement nameIdentifier = declaration.getNameIdentifier();
						assert nameIdentifier != null;
						holder.registerProblem(nameIdentifier, Unity3dLocalize.onguiMethodInspectionMessage().get(), new UnityEmptyMagicMethodInspection.RemoveMethodFix(declaration));
					}
				}
			}
		};
	}
}
