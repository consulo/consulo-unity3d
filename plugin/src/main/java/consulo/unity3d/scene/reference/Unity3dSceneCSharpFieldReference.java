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

package consulo.unity3d.scene.reference;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.unity3d.scene.Unity3dAssetUtil;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dSceneCSharpFieldReference extends Unity3dKeyValueReferenceBase
{
	private VirtualFile myCSharpFile;

	public Unity3dSceneCSharpFieldReference(YAMLKeyValue keyValue, VirtualFile file)
	{
		super(keyValue);
		myCSharpFile = file;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement resolve()
	{
		Project project = myKeyValue.getProject();
		PsiFile file = PsiManager.getInstance(project).findFile(myCSharpFile);
		if(!(file instanceof CSharpFile))
		{
			return null;
		}

		CSharpTypeDeclaration type = Unity3dAssetUtil.findPrimaryType(file);
		if(type == null)
		{
			return null;
		}

		return findField(type, getCanonicalText());
	}

	@RequiredReadAction
	private static CSharpFieldDeclaration findField(CSharpTypeDeclaration owner, String name)
	{
		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner, psiElementProcessor, new ExecuteTarget[]{ExecuteTarget.FIELD}, OverrideProcessor.ALWAYS_TRUE);

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, DotNetGenericExtractor.EMPTY);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, true, state);
		for(PsiElement element : psiElementProcessor.getElements())
		{
			if(element instanceof CSharpFieldDeclaration)
			{
				return (CSharpFieldDeclaration) element;
			}
		}
		return null;
	}
}
