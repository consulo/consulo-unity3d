package org.mustbe.consulo.unity3d.unityscript.index;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptFileByNameIndex extends StringStubIndexExtension<JSFile>
{
	@NotNull
	@LazyInstance
	public static UnityScriptFileByNameIndex getInstance()
	{
		return EP_NAME.findExtension(UnityScriptFileByNameIndex.class);
	}

	@NotNull
	@Override
	public StubIndexKey<String, JSFile> getKey()
	{
		return UnityScriptIndexKeys.FILE_BY_NAME_INDEX;
	}
}
