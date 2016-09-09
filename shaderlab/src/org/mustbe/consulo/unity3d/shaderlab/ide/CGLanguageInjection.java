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

package org.mustbe.consulo.unity3d.shaderlab.ide;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.cgshader.CGLanguage;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderCGScript;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGLanguageInjection implements MultiHostInjector
{
	@Override
	@RequiredReadAction
	public void injectLanguages(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context)
	{
		ShaderCGScript cgScript = (ShaderCGScript) context;

		PsiElement scriptBlock = cgScript.getScriptBlock();
		if(scriptBlock == null)
		{
			return;
		}

		int startElement = cgScript.getTextOffset();
		int startBlock = scriptBlock.getTextOffset();

		registrar.startInjecting(CGLanguage.INSTANCE).addPlace(null, null, cgScript, new TextRange(startBlock - startElement, scriptBlock.getTextLength())).doneInjecting();
	}
}
