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

package org.mustbe.consulo.unity3d.shaderlab.lang.psi.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

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

	@NotNull
	@Override
	public String getTargetText()
	{
		return myText;
	}
}
