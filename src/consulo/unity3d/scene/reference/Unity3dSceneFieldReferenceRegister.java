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
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dSceneFieldReferenceRegister extends PsiReferenceContributor
{
	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar)
	{
		PsiElementPattern.Capture<YAMLKeyValue> pattern = StandardPatterns.psiElement(YAMLKeyValue.class);
		pattern = pattern.withParent(StandardPatterns.psiElement(YAMLMapping.class));
		pattern = pattern.withSuperParent(2, StandardPatterns.psiElement(YAMLKeyValue.class));
		pattern = pattern.withSuperParent(3, StandardPatterns.psiElement(YAMLMapping.class));
		pattern = pattern.withSuperParent(4, StandardPatterns.psiElement(YAMLDocument.class));
		pattern = pattern.withSuperParent(5, StandardPatterns.psiElement(YAMLFile.class));

		psiReferenceRegistrar.registerReferenceProvider(pattern, new PsiReferenceProvider()
		{
			@NotNull
			@Override
			@RequiredReadAction
			public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext)
			{
				PsiFile containingFile = psiElement.getContainingFile();
				if(containingFile == null || containingFile.getFileType() != Unity3dYMLAssetFileType.INSTANCE)
				{
					return PsiReference.EMPTY_ARRAY;
				}

				Project project = psiElement.getProject();
				if(Unity3dModuleExtensionUtil.getRootModule(project) == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				YAMLMapping parent = (YAMLMapping) psiElement.getParent();
				YAMLKeyValue scriptKey = parent.getKeyValueByKey("m_Script");
				if(scriptKey == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				YAMLValue value = scriptKey.getValue();
				if(!(value instanceof YAMLMapping))
				{
					return PsiReference.EMPTY_ARRAY;
				}
				YAMLKeyValue guidValue = ((YAMLMapping) value).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
				if(guidValue == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				String valueText = guidValue.getValueText();
				VirtualFile fileByGUID = Unity3dMetaManager.getInstance(project).findFileByGUID(valueText);
				if(fileByGUID == null || fileByGUID.getFileType() != CSharpFileType.INSTANCE)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				return new PsiReference[]{new Unity3dSceneCSharpFieldReference((YAMLKeyValue) psiElement, fileByGUID)};
			}
		});
	}
}
