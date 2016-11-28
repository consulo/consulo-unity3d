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

package consulo.unity3d.shaderlab.lang.psi.stub.index;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import consulo.lombok.annotations.Lazy;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderDefIndex extends StringStubIndexExtension<ShaderDef>
{
	@NotNull
	@Lazy
	public static ShaderDefIndex getInstance()
	{
		return EP_NAME.findExtension(ShaderDefIndex.class);
	}

	public static final StubIndexKey<String, ShaderDef> KEY = StubIndexKey.createIndexKey("shader.def.key");

	@NotNull
	@Override
	public StubIndexKey<String, ShaderDef> getKey()
	{
		return KEY;
	}
}
