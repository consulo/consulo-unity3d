/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.unity3d.csharp.codeInsight.inspection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.unity3d.Unity3dBundle;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 06.01.2016
 */
public class UnityNewMonoBehaviourInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
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
				if(CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(Unity3dTypes.UnityEngine.MonoBehaviour), typeRef, expression))
				{
					holder.registerProblem(newType, Unity3dBundle.message("new.mono.behaviour.inspection.message"));
				}
			}
		};
	}

}
