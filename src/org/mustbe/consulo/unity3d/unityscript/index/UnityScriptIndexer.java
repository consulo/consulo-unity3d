package org.mustbe.consulo.unity3d.unityscript.index;

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
