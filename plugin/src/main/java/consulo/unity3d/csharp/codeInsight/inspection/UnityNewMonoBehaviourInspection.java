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

package consulo.unity3d.csharp.codeInsight.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 06.01.2016
 */
public class UnityNewMonoBehaviourInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		if(Unity3dModuleExtensionUtil.getRootModule(holder.getProject()) == null)
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}

		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitNewExpression(CSharpNewExpression expression)
			{
				DotNetType newType = expression.getNewType();
				if(newType == null)
				{
					return;
				}
				DotNetTypeRef typeRef = expression.toTypeRef(true);
				if(CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(expression, Unity3dTypes.UnityEngine.MonoBehaviour), typeRef))
				{
					holder.registerProblem(newType, Unity3dBundle.message("new.mono.behaviour.inspection.message"));
				}
			}
		};
	}

}
