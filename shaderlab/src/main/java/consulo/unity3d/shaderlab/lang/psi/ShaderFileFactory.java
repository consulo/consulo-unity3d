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

package consulo.unity3d.shaderlab.lang.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import consulo.unity3d.shaderlab.lang.ShaderLabFileType;

/**
 * @author VISTALL
 * @since 02.06.2015
 */
public class ShaderFileFactory
{
	@NotNull
	public static PsiElement createSimpleIdentifier(@NotNull Project project, @NotNull String name)
	{
		ShaderLabFile fileFromText = (ShaderLabFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.shader", ShaderLabFileType.INSTANCE,
				"Shader \"dummy\" { Properties { " + name + "(\"dummy\", Int) = 1 }}");
		return fileFromText.getShaderDef().getProperties().get(0).getNameIdentifier();
	}
}
