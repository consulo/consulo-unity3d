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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.index.Unity3dYMLAsset;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dSceneReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters searchParameters, @NotNull Processor<PsiReference> processor)
	{
		SearchScope scope = ReadAction.compute(searchParameters::getEffectiveSearchScope);
		if(!(scope instanceof GlobalSearchScope))
		{
			return;
		}

		PsiElement element = searchParameters.getElementToSearch();
		if(element instanceof CSharpFieldDeclaration && ReadAction.compute(() -> Unity3dModuleExtensionUtil.getRootModule(searchParameters.getProject()) != null))
		{
			String name = ReadAction.compute(((CSharpFieldDeclaration) element)::getName);
			Project project = element.getProject();
			MultiMap<VirtualFile, Unity3dYMLAsset> map = ReadAction.compute(() -> Unity3dYMLAsset.findAssetAsAttach(project, PsiUtilCore.getVirtualFile(element), false));

			for(VirtualFile virtualFile : map.keySet())
			{
				ProgressManager.checkCanceled();

				searchParameters.getOptimizer().searchWord(name + ":", GlobalSearchScope.fileScope(project, virtualFile), true, element);
			}
		}
	}
}
