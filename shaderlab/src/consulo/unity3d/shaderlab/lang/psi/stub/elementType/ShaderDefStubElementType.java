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

package consulo.unity3d.shaderlab.lang.psi.stub.elementType;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;
import consulo.unity3d.shaderlab.lang.psi.stub.ShaderDefStub;
import consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import consulo.psi.tree.IElementTypeAsPsiFactory;

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

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new ShaderDef(astNode);
	}

	@Override
	public ShaderDef createPsi(@NotNull ShaderDefStub stub)
	{
		return new ShaderDef(stub, this);
	}

	@Override
	public ShaderDefStub createStub(@NotNull ShaderDef psi, StubElement parentStub)
	{
		return new ShaderDefStub(parentStub, this, psi.getName());
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return getLanguage() + "." + toString();
	}

	@Override
	public void serialize(@NotNull ShaderDefStub stub, @NotNull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getName());
	}

	@NotNull
	@Override
	public ShaderDefStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef name = dataStream.readName();
		return new ShaderDefStub(parentStub, this, name);
	}

	@Override
	public void indexStub(@NotNull ShaderDefStub stub, @NotNull IndexSink sink)
	{
		String name = stub.getName();
		if(name != null)
		{
			sink.occurrence(ShaderDefIndex.KEY, name);
		}
	}
}
