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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.project.Project;
import consulo.project.util.query.QueryExecutorBase;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.yaml.psi.YAMLFile;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
@ExtensionImpl
public class Unity3dSceneReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor
{
	@Override
	public void processQuery(@Nonnull ReferencesSearch.SearchParameters searchParameters, @Nonnull Processor<? super PsiReference> processor)
	{
		SearchScope scope = ReadAction.compute(searchParameters::getEffectiveSearchScope);
		if(!(scope instanceof GlobalSearchScope))
		{
			return;
		}

		Project project = searchParameters.getProject();

		PsiElement element = searchParameters.getElementToSearch();
		if(ReadAction.compute(() -> Unity3dModuleExtensionUtil.getRootModule(searchParameters.getProject()) != null))
		{
			if(element instanceof CSharpFieldDeclaration)
			{
				String name = ReadAction.compute(((CSharpFieldDeclaration) element)::getName);
				MultiMap<VirtualFile, Unity3dYMLAsset> map = ReadAction.compute(() -> Unity3dYMLAsset.findAssetAsAttach(project, PsiUtilCore.getVirtualFile(element)));

				for(VirtualFile virtualFile : map.keySet())
				{
					ProgressManager.checkCanceled();

					searchParameters.getOptimizer().searchWord(name + ":", GlobalSearchScope.fileScope(project, virtualFile), true, element);
				}
			}
			else if(element instanceof YAMLFile)
			{
				String guid = ReadAction.compute(() -> Unity3dMetaManager.getInstance(project).getGUID(PsiUtilCore.getVirtualFile(element)));
				if(guid != null)
				{
					searchParameters.getOptimizer().searchWord(guid, GlobalSearchScope.allScope(project), true, element);
				}
			}
		}
	}
}
