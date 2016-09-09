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

package consulo.unity3d.shaderlab.lang.psi.light;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.psi.ShaderProperty;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyValue;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class LightShaderProperty extends LightElement implements ShaderProperty
{
	private String myName;
	private LightShaderPropertyType myType;

	public LightShaderProperty(@NotNull Project project, @NotNull String name, @NotNull String type)
	{
		super(PsiManager.getInstance(project), ShaderLabLanguage.INSTANCE);
		myName = name;
		myType = new LightShaderPropertyType(project, type);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + myName;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public ShaderPropertyType getType()
	{
		return myType;
	}

	@Nullable
	@Override
	public ShaderPropertyValue getValue()
	{
		return null;
	}
}
