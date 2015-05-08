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

package org.mustbe.consulo.unity3d.shaderlab.lang.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.stub.ShaderDefStub;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderDef extends StubBasedPsiElementBase<ShaderDefStub> implements PsiNameIdentifierOwner, StubBasedPsiElement<ShaderDefStub>, ShaderBraceOwner
{
	public ShaderDef(@NotNull ASTNode node)
	{
		super(node);
	}

	public ShaderDef(@NotNull ShaderDefStub stub, @NotNull IStubElementType nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@Override
	public String getName()
	{
		ShaderDefStub stub = getStub();
		if(stub != null)
		{
			return stub.getName();
		}
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : nameIdentifier.getText();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof SharpLabElementVisitor)
		{
			((SharpLabElementVisitor) visitor).visitShaderDef(this);
		}
		else
		{
			super.accept(visitor);
		}
	}

	@Override
	@Nullable
	public PsiElement getLeftBrace()
	{
		return findChildByType(ShaderLabTokens.LBRACE);
	}

	@Override
	@Nullable
	public PsiElement getRightBrace()
	{
		return findChildByType(ShaderLabTokens.RBRACE);
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(ShaderLabTokens.STRING_LITERAL);
	}
}
