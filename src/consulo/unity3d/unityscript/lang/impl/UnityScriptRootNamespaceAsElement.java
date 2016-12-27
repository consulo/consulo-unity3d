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

package consulo.unity3d.unityscript.lang.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.lang.psi.impl.DotNetNamespaceCacheManager;
import consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;
import consulo.unity3d.unityscript.index.UnityScriptFileByNameIndex;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class UnityScriptRootNamespaceAsElement extends BaseDotNetNamespaceAsElement
{
	private static final DotNetNamespaceCacheManager.ItemCalculator ourElementsCalculator = new DotNetNamespaceCacheManager.ItemCalculator()
	{
		@NotNull
		@Override
		@RequiredReadAction
		public Set<PsiElement> compute(@NotNull final Project project,
				@Nullable final IndexBasedDotNetPsiSearcher searcher,
				@NotNull final String indexKey,
				@NotNull final String thisQName,
				@NotNull final GlobalSearchScope scope)
		{
			Set<PsiElement> elements = new LinkedHashSet<PsiElement>();
			Collection<String> keys = UnityScriptFileByNameIndex.getInstance().getAllKeys(project);
			for(String key : keys)
			{
				Collection<JSFile> jsFiles = UnityScriptFileByNameIndex.getInstance().get(key, project, scope);
				for(JSFile jsFile : jsFiles)
				{
					elements.add(new UnityScriptDotNetTypeDeclaration(key, jsFile));
				}
			}
			return elements;
		}

		@NotNull
		@Override
		public ChildrenFilter getFilter()
		{
			return ChildrenFilter.ONLY_ELEMENTS;
		}
	};

	public UnityScriptRootNamespaceAsElement(@NotNull Project project, @NotNull Language language, @NotNull String qName)
	{
		super(project, language, qName);
	}

	@NotNull
	@Override
	@RequiredReadAction
	protected Collection<? extends PsiElement> getOnlyElements(@NotNull GlobalSearchScope globalSearchScope)
	{
		return DotNetNamespaceCacheManager.getInstance(myProject).computeElements(null, this, myQName, myQName, globalSearchScope, ourElementsCalculator);
	}
}
