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

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ArrayListSet;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceWithValidation;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public class Unity3dEventMethodReference extends PsiReferenceBase<CSharpConstantExpressionImpl> implements CSharpReferenceWithValidation
{
	@RequiredReadAction
	public Unity3dEventMethodReference(CSharpConstantExpressionImpl element)
	{
		super(element, CSharpConstantExpressionImpl.getStringValueTextRange(element));
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getErrorMessage(@Nonnull PsiElement element)
	{
		return StringUtil.SINGLE_QUOTER.fun((String) ((CSharpConstantExpressionImpl) element).getValue()) + " is not resolved";
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement resolve()
	{
		CSharpMethodCallExpressionImpl methodCallExpression = PsiTreeUtil.getParentOfType(myElement, CSharpMethodCallExpressionImpl.class);
		assert methodCallExpression != null;
		DotNetExpression callExpression = methodCallExpression.getCallExpression();
		if(!(callExpression instanceof CSharpReferenceExpression))
		{
			return null;
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
			return null;
		}

		if(methodCallExpression.resolveToCallable() == null)
		{
			return null;
		}

		DotNetTypeResolveResult typeResolveResult = targetTypeRef.resolve();
		PsiElement element = typeResolveResult.getElement();
		if(element == null)
		{
			return null;
		}

		return findMethodByName((String) myElement.getValue(), element);
	}

	@RequiredReadAction
	private static DotNetMethodDeclaration findMethodByName(@Nonnull String name, PsiElement owner)
	{
		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner, psiElementProcessor, new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP}, OverrideProcessor
				.ALWAYS_TRUE);

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, DotNetGenericExtractor.EMPTY);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, true, state);

		for(PsiElement psiElement : psiElementProcessor.getElements())
		{
			if(psiElement instanceof CSharpElementGroup)
			{
				for(PsiElement element : ((CSharpElementGroup<?>) psiElement).getElements())
				{
					if(element instanceof DotNetMethodDeclaration)
					{
						return (DotNetMethodDeclaration) element;
					}
				}
			}
		}
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Object[] getVariants()
	{
		CSharpMethodCallExpressionImpl methodCallExpression = PsiTreeUtil.getParentOfType(myElement, CSharpMethodCallExpressionImpl.class);
		assert methodCallExpression != null;
		DotNetExpression callExpression = methodCallExpression.getCallExpression();
		if(!(callExpression instanceof CSharpReferenceExpression))
		{
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
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
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		DotNetTypeResolveResult typeResolveResult = targetTypeRef.resolve();
		PsiElement element = typeResolveResult.getElement();
		if(element == null)
		{
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		CSharpResolveOptions build = CSharpResolveOptions.build();
		build.element(myElement);
		build.kind(CSharpReferenceExpression.ResolveToKind.ANY_MEMBER);
		build.completion();

		Set<LookupElementBuilder> elements = new ArrayListSet<>();
		CSharpReferenceExpressionImplUtil.processAnyMember(build, DotNetGenericExtractor.EMPTY, element, resolveResult -> {
			PsiElement psiElement = resolveResult.getElement();
			if(psiElement instanceof CSharpMethodDeclaration)
			{
				String name = ((CSharpMethodDeclaration) psiElement).getName();
				if(name == null)
				{
					return true;
				}

				LookupElementBuilder builder = LookupElementBuilder.create(name);
				builder = builder.withIcon((Image) AllIcons.Nodes.Method);

				elements.add(builder);
			}
			return true;
		});

		return elements.toArray();
	}
}
