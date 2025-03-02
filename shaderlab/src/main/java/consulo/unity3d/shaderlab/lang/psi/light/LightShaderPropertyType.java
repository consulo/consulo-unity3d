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

package consulo.unity3d.shaderlab.lang.psi.light;

import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class LightShaderPropertyType extends LightElement implements ShaderPropertyType
{
	private String myText;

	public LightShaderPropertyType(Project project, String text)
	{
		super(PsiManager.getInstance(project), ShaderLabLanguage.INSTANCE);
		myText = text;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + myText;
	}

	@Nullable
	@Override
	public PsiElement getTargetElement()
	{
		return null;
	}

	@Nonnull
	@Override
	public String getTargetText()
	{
		return myText;
	}
}
