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

package consulo.unity3d.shaderlab.ide.refactoring;

import consulo.language.psi.PsiElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderFileFactory;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 02.06.2015
 */
public class ShaderRefactorUtil
{
	public static void replaceIdentifier(@Nonnull PsiElement identifier, @Nonnull String newName)
	{
		PsiElement newIdentifier = ShaderFileFactory.createSimpleIdentifier(identifier.getProject(), newName);
		identifier.replace(newIdentifier);
	}
}
