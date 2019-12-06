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

package consulo.unity3d.unityscript.lang.impl.csharp;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSSourceElement;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.ToNativeElementTransformer;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifier;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.unityscript.lang.impl.UnityScriptDotNetTypeDeclaration;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.12.2015
 */
public class UnityScriptToNativeElementTransformer implements ToNativeElementTransformer
{
	public static final Key<Boolean> JS_MARKER = Key.create("unity.script.marker");

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement transform(@Nonnull PsiElement psiElement)
	{
		if(psiElement instanceof UnityScriptDotNetTypeDeclaration)
		{
			CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(psiElement)
			{
				@Nonnull
				@Override
				public Language getLanguage()
				{
					return JavaScriptLanguage.INSTANCE;
				}
			};
			PsiElement navigationElement = psiElement.getNavigationElement();

			builder.withName(((UnityScriptDotNetTypeDeclaration) psiElement).getName());
			builder.setNavigationElement(navigationElement);
			builder.addModifier(DotNetModifier.PUBLIC);
			builder.putUserData(JS_MARKER, Boolean.TRUE);
			builder.addExtendType(new CSharpTypeRefByQName(psiElement, Unity3dTypes.UnityEngine.MonoBehaviour));

			if(navigationElement instanceof JSFile)
			{
				for(JSSourceElement jsSourceElement : ((JSFile) navigationElement).getStatements())
				{
					if(jsSourceElement instanceof JSFunction)
					{
						String funcName = jsSourceElement.getName();
						if(funcName == null)
						{
							continue;
						}
						CSharpLightMethodDeclarationBuilder methodDeclarationBuilder = new CSharpLightMethodDeclarationBuilder(psiElement.getProject());
						methodDeclarationBuilder.addModifier(DotNetModifier.PUBLIC);
						methodDeclarationBuilder.withReturnType(new CSharpTypeRefByQName(psiElement, DotNetTypes.System.Void));
						methodDeclarationBuilder.withName(funcName);
						methodDeclarationBuilder.setNavigationElement(jsSourceElement);
						builder.addMember(methodDeclarationBuilder);
					}
				}
			}
			return builder;
		}
		return null;
	}
}
