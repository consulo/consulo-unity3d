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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.shaderlab.ide.refactoring.ShaderRefactorUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderPropertyElement extends ShaderLabElement implements ShaderProperty
{
	public ShaderPropertyElement(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	@Nullable
	public ShaderPropertyTypeElement getType()
	{
		return findChildByClass(ShaderPropertyTypeElement.class);
	}

	@Nullable
	@Override
	public ShaderPropertyValue getValue()
	{
		return findChildByClass(ShaderPropertyValue.class);
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitProperty(this);
	}

	@Override
	public String getName()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : nameIdentifier.getText();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(ShaderLabTokens.IDENTIFIER);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier == null)
		{
			throw new IncorrectOperationException();
		}
		ShaderRefactorUtil.replaceIdentifier(nameIdentifier, name);
		return this;
	}
}
