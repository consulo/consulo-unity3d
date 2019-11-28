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

package consulo.unity3d.shaderlab.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.unity3d.shaderlab.lang.psi.*;
import consulo.unity3d.shaderlab.lang.psi.light.LightShaderProperty;
import consulo.unity3d.shaderlab.lang.psi.stub.ShaderDefStub;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderDefImpl extends StubBasedPsiElementBase<ShaderDefStub> implements StubBasedPsiElement<ShaderDefStub>, ShaderDef
{
	public ShaderDefImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public ShaderDefImpl(@Nonnull ShaderDefStub stub, @Nonnull IStubElementType nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@RequiredReadAction
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

	@Nonnull
	@Override
	public List<ShaderProperty> getProperties()
	{
		ShaderPropertyList[] childrenByClass = findChildrenByClass(ShaderPropertyList.class);
		List<ShaderProperty> list = new ArrayList<>();
		for(ShaderPropertyList propertyList : childrenByClass)
		{
			Collections.addAll(list, propertyList.getProperties());
		}
		list.add(new LightShaderProperty(getProject(), "_Projector", "Vector"));
		list.add(new LightShaderProperty(getProject(), "_ProjectorClip", "Vector"));
		return list;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
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

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(ShaderLabTokens.STRING_LITERAL);
	}

	@Nullable
	@Override
	public ShaderLabRole getRole()
	{
		return ShaderLabRoles.Shader;
	}
}
