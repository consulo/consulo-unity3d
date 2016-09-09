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

package consulo.unity3d.shaderlab.lang.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderPropertyTypeElement extends ShaderLabElement implements ShaderPropertyType
{
	public ShaderPropertyTypeElement(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	@NotNull
	public PsiElement getTargetElement()
	{
		return findNotNullChildByType(ShaderLabTokens.IDENTIFIER);
	}

	@NotNull
	@Override
	public String getTargetText()
	{
		return getTargetElement().getText();
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitPropertyType(this);
	}
}
