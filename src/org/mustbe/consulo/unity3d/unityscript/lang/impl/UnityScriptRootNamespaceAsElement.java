/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.unityscript.lang.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.unity3d.unityscript.index.UnityScriptFileByNameIndex;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class UnityScriptRootNamespaceAsElement extends BaseDotNetNamespaceAsElement
{
	public UnityScriptRootNamespaceAsElement(@NotNull Project project, @NotNull Language language, @NotNull String qName)
	{
		super(project, language, qName);
	}

	@NotNull
	@Override
	@RequiredReadAction
	protected Collection<? extends PsiElement> getOnlyElements(@NotNull GlobalSearchScope globalSearchScope)
	{
		List<PsiElement> elements = new ArrayList<PsiElement>();
		Collection<String> keys = UnityScriptFileByNameIndex.getInstance().getAllKeys(myProject);
		for(String key : keys)
		{
			Collection<JSFile> jsFiles = UnityScriptFileByNameIndex.getInstance().get(key, myProject, globalSearchScope);
			for(JSFile jsFile : jsFiles)
			{
				elements.add(new UnityScriptDotNetTypeDeclaration(key, jsFile));
			}
		}
		return elements;
	}
}
