/*
 * Copyright 2013-2018 consulo.io
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

package consulo.unity3d.projectImport.change;

import java.util.function.Consumer;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.dll.DotNetModuleFileType;

/**
 * @author VISTALL
 * @since 2018-01-12
 */
public class DefaultUnity3dProjectSourceFileTypeFactory implements Unity3dProjectSourceFileTypeFactory
{
	@Override
	public void registerFileTypes(Consumer<FileType> consumer)
	{
		consumer.accept(DotNetModuleFileType.INSTANCE);
		consumer.accept(CSharpFileType.INSTANCE);
		consumer.accept(JavaScriptFileType.INSTANCE);
	}
}
