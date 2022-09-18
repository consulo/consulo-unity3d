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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.unity3d.Unity3dTypes;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public abstract class Unity3dEventMethodReferenceProvider extends PsiReferenceContributor
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
			@Nonnull
			@Override
			@RequiredReadAction
			public PsiReference[] getReferencesByElement(@Nonnull PsiElement psiElement, @Nonnull ProcessingContext processingContext)
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

				if(!CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(methodCallExpression, Unity3dTypes.UnityEngine.MonoBehaviour), targetTypeRef))
				{
					return PsiReference.EMPTY_ARRAY;
				}
				return new PsiReference[]{new Unity3dEventMethodReference(constantExpression)};
			}
		});
	}
}
