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

import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.stub.StubElement;
import consulo.unity3d.shaderlab.lang.ShaderLabFileType;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.virtualFileSystem.fileType.FileType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabFile extends PsiFileBase
{
	public ShaderLabFile(@Nonnull FileViewProvider viewProvider)
	{
		super(viewProvider, ShaderLabLanguage.INSTANCE);
	}

	@Nullable
	public ShaderDef getShaderDef()
	{
		StubElement<?> stub = getStub();
		if(stub != null)
		{
			StubElement<ShaderDef> childStubByType = stub.findChildStubByType(ShaderLabStubElements.SHADER_DEF);
			if(childStubByType != null)
			{
				return childStubByType.getPsi();
			}
		}
		return findChildByClass(ShaderDef.class);
	}

	@Nonnull
	public List<ShaderProperty> getProperties()
	{
		ShaderDef shaderDef = getShaderDef();
		return shaderDef == null ? Collections.<ShaderProperty>emptyList() : shaderDef.getProperties();
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return ShaderLabFileType.INSTANCE;
	}
}
