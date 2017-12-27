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

package consulo.unity3d.shaderlab.lang.psi.stub;

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderDefStub extends NamedStubBase<ShaderDef>
{
	public ShaderDefStub(StubElement parent, IStubElementType elementType, @Nullable StringRef name)
	{
		super(parent, elementType, name);
	}

	public ShaderDefStub(StubElement parent, IStubElementType elementType, @Nullable String name)
	{
		super(parent, elementType, name);
	}
}
