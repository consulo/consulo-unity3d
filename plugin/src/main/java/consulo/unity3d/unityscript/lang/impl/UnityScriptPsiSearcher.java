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

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.unity3d.unityscript.index.UnityScriptFileByNameIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptPsiSearcher extends DotNetPsiSearcher
{
	private Project myProject;

	@Inject
	public UnityScriptPsiSearcher(Project project)
	{
		myProject = project;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetNamespaceAsElement findNamespace(@Nonnull String qName, @Nonnull GlobalSearchScope scope)
	{
		if(qName.isEmpty())
		{
			return new UnityScriptRootNamespaceAsElement(myProject, JavaScriptLanguage.INSTANCE, qName);
		}
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends DotNetTypeDeclaration> findTypesImpl(@Nonnull String key, @Nonnull GlobalSearchScope searchScope)
	{
		if(DumbService.isDumb(myProject))
		{
			return Collections.emptyList();
		}

		Collection<JSFile> jsFiles = UnityScriptFileByNameIndex.getInstance().get(key, myProject, searchScope);
		JSFile jsFile = ContainerUtil.getFirstItem(jsFiles);
		if(jsFile == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(new UnityScriptDotNetTypeDeclaration(key, jsFile));
	}
}
