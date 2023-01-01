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

package consulo.unity3d.shaderlab.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.cgshader.CGLanguage;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.util.ProcessingContext;
import consulo.unity3d.shaderlab.lang.psi.ShaderCGScript;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabFile;
import consulo.unity3d.shaderlab.lang.psi.ShaderReference;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
@ExtensionImpl
public class ShaderLabCGCompletionContributor extends CompletionContributor
{
	public ShaderLabCGCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(CGLanguage.INSTANCE), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
			{
				PsiLanguageInjectionHost.Place shreds = InjectedLanguageManager.getInstance(parameters.getPosition().getProject()).getShreds(parameters.getOriginalFile());

				for(PsiLanguageInjectionHost.Shred shred : shreds)
				{
					PsiLanguageInjectionHost host = shred.getHost();
					if(host instanceof ShaderCGScript)
					{
						ShaderLabFile containingFile = (ShaderLabFile) host.getContainingFile();
						ShaderReference.consumeProperties(containingFile, result::addElement);
					}
				}
			}
		});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CGLanguage.INSTANCE;
	}
}
