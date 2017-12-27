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

package consulo.unity3d.csharp.psi.reference;

import org.jetbrains.annotations.NotNull;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.unity3d.Unity3dTypes;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public class Unity3dEventMethodReferenceProvider extends PsiReferenceContributor
{
	private static final String[] ourEventMethodNames = {
			"StartCoroutine",
			"StopCoroutine",
			"Invoke",
			"InvokeRepeating",
			"IsInvoking",
			"CancelInvoke"
	};

	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar registrar)
	{
		PsiElementPattern.Capture<CSharpConstantExpressionImpl> pattern = StandardPatterns.psiElement(CSharpConstantExpressionImpl.class);
		pattern = pattern.withParent(CSharpCallArgument.class);
		pattern = pattern.withSuperParent(3, CSharpMethodCallExpressionImpl.class);

		registrar.registerReferenceProvider(pattern, new PsiReferenceProvider()
		{
			@NotNull
			@Override
			@RequiredReadAction
			public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext)
			{
				CSharpConstantExpressionImpl constantExpression = (CSharpConstantExpressionImpl) psiElement;
				// accept only strings
				if(!constantExpression.isValidHost())
				{
					return PsiReference.EMPTY_ARRAY;
				}
				CSharpMethodCallExpressionImpl methodCallExpression = PsiTreeUtil.getParentOfType(constantExpression, CSharpMethodCallExpressionImpl.class);
				assert methodCallExpression != null;
				DotNetExpression callExpression = methodCallExpression.getCallExpression();
				if(!(callExpression instanceof CSharpReferenceExpression))
				{
					return PsiReference.EMPTY_ARRAY;
				}

				String referenceName = ((CSharpReferenceExpression) callExpression).getReferenceName();
				if(referenceName == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}

				if(!ArrayUtil.contains(referenceName, ourEventMethodNames))
				{
					return PsiReference.EMPTY_ARRAY;
				}

				DotNetTypeRef targetTypeRef = DotNetTypeRef.ERROR_TYPE;
				DotNetExpression qualifier = ((CSharpReferenceExpression) callExpression).getQualifier();
				if(qualifier != null)
				{
					targetTypeRef = qualifier.toTypeRef(true);
				}
				else
				{
					CSharpTypeDeclaration type = PsiTreeUtil.getParentOfType(methodCallExpression, CSharpTypeDeclaration.class);
					if(type != null)
					{
						targetTypeRef = new CSharpTypeRefByTypeDeclaration(type);
					}
				}

				if(!CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(methodCallExpression, Unity3dTypes.UnityEngine.MonoBehaviour), targetTypeRef, methodCallExpression))
				{
					return PsiReference.EMPTY_ARRAY;
				}
				return new PsiReference[]{new Unity3dEventMethodReference(constantExpression)};
			}
		});
	}
}
