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

package consulo.unity3d.shaderlab.ide;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.cgshader.CGLanguage;
import consulo.document.util.TextRange;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderCGScript;

import jakarta.annotation.Nonnull;
/**
 * @author VISTALL
 * @since 11.10.2015
 */
@ExtensionImpl
public class CGLanguageInjection implements MultiHostInjector
{
	@Nonnull
	@Override
	public Class<? extends PsiElement> getElementClass()
	{
		return ShaderCGScript.class;
	}

	@Override
	@RequiredReadAction
	public void injectLanguages(@Nonnull MultiHostRegistrar registrar, @Nonnull PsiElement context)
	{
		ShaderCGScript cgScript = (ShaderCGScript) context;

		PsiElement scriptBlock = cgScript.getScriptBlock();
		if(scriptBlock == null)
		{
			return;
		}

		int startOffsetInParent = scriptBlock.getStartOffsetInParent();
		registrar.startInjecting(CGLanguage.INSTANCE).addPlace(null, null, cgScript, new TextRange(startOffsetInParent, scriptBlock.getTextLength() + startOffsetInParent)).doneInjecting();
	}
}
