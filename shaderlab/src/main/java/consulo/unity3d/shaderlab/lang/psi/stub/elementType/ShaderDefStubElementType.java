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

package consulo.unity3d.shaderlab.lang.psi.stub.elementType;

import consulo.annotation.access.RequiredReadAction;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementTypeAsPsiFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.*;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;
import consulo.unity3d.shaderlab.lang.psi.impl.ShaderDefImpl;
import consulo.unity3d.shaderlab.lang.psi.stub.ShaderDefStub;
import consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;

import jakarta.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderDefStubElementType extends IStubElementType<ShaderDefStub, ShaderDef> implements IElementTypeAsPsiFactory
{
	public ShaderDefStubElementType()
	{
		super("SHADER_DEF", ShaderLabLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new ShaderDefImpl(astNode);
	}

	@Override
	public ShaderDef createPsi(@Nonnull ShaderDefStub stub)
	{
		return new ShaderDefImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public ShaderDefStub createStub(@Nonnull ShaderDef psi, StubElement parentStub)
	{
		return new ShaderDefStub(parentStub, this, psi.getName());
	}

	@Nonnull
	@Override
	public String getExternalId()
	{
		return getLanguage() + "." + toString();
	}

	@Override
	public void serialize(@Nonnull ShaderDefStub stub, @Nonnull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getName());
	}

	@Nonnull
	@Override
	public ShaderDefStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef name = dataStream.readName();
		return new ShaderDefStub(parentStub, this, name);
	}

	@Override
	public void indexStub(@Nonnull ShaderDefStub stub, @Nonnull IndexSink sink)
	{
		String name = stub.getName();
		if(name != null)
		{
			sink.occurrence(ShaderDefIndex.KEY, name);
		}
	}
}
