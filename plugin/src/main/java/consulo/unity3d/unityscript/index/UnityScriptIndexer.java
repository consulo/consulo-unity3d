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

package consulo.unity3d.unityscript.index;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.stubs.IndexSink;
import consulo.javascript.index.JavaScriptIndexer;
import consulo.javascript.psi.stubs.JSFileStub;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptIndexer extends JavaScriptIndexer
{
	@Override
	public void indexFile(@NotNull JSFileStub fileStub, @NotNull IndexSink sink)
	{
		String nameWithoutExtension = FileUtilRt.getNameWithoutExtension(fileStub.getName());
		sink.occurrence(UnityScriptIndexKeys.FILE_BY_NAME_INDEX, nameWithoutExtension);
	}

	@Override
	public int getVersion()
	{
		return 1;
	}
}
