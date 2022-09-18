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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.*;
import consulo.language.util.ProcessingContext;
import consulo.project.Project;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
@ExtensionImpl
public class Unity3dSceneFieldReferenceRegister extends PsiReferenceContributor
{
	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar)
	{
		PsiElementPattern.Capture<YAMLKeyValue> fieldNamePattern = StandardPatterns.psiElement(YAMLKeyValue.class);
		fieldNamePattern = fieldNamePattern.withParent(StandardPatterns.psiElement(YAMLMapping.class));
		fieldNamePattern = fieldNamePattern.withSuperParent(2, StandardPatterns.psiElement(YAMLKeyValue.class));
		fieldNamePattern = fieldNamePattern.withSuperParent(3, StandardPatterns.psiElement(YAMLMapping.class));
		fieldNamePattern = fieldNamePattern.withSuperParent(4, StandardPatterns.psiElement(YAMLDocument.class));
		fieldNamePattern = fieldNamePattern.withSuperParent(5, StandardPatterns.psiElement(YAMLFile.class));

		psiReferenceRegistrar.registerReferenceProvider(fieldNamePattern, new PsiReferenceProvider()
		{
			@Nonnull
			@Override
			@RequiredReadAction
			public PsiReference[] getReferencesByElement(@Nonnull PsiElement psiElement, @Nonnull ProcessingContext processingContext)
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

		psiReferenceRegistrar.registerReferenceProvider(StandardPatterns.psiElement(YAMLKeyValue.class).withParent(YAMLMapping.class), new PsiReferenceProvider()
		{
			@Nonnull
			@Override
			public PsiReference[] getReferencesByElement(@Nonnull PsiElement psiElement, @Nonnull ProcessingContext processingContext)
			{
				YAMLKeyValue keyValue = (YAMLKeyValue) psiElement;

				YAMLMapping parentMapping = keyValue.getParentMapping();

				YAMLKeyValue fileID = parentMapping.getKeyValueByKey("fileID");
				if(fileID == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				return new PsiReference[]{new Unity3dAssetGUIDReference(keyValue)};
			}
		});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
