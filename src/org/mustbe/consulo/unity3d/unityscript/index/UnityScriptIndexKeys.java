package org.mustbe.consulo.unity3d.unityscript.index;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public interface UnityScriptIndexKeys
{
	StubIndexKey<String, JSFile> FILE_BY_NAME_INDEX = StubIndexKey.createIndexKey("unity.script.file.index");
}
