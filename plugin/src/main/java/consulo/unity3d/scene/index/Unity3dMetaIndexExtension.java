/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.scene.index;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorIntegerDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.scene.Unity3dMetaManager;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dMetaIndexExtension extends FileBasedIndexExtension<String, Integer>
{
	public static final ID<String, Integer> KEY = ID.create("unity3d.meta.index");

	private static final int ourVersion = 4;

	private DataIndexer<String, Integer, FileContent> myIndexer = fileContent ->
	{
		PsiFile psiFile = fileContent.getPsiFile();
		if(!(psiFile instanceof YAMLFile))
		{
			return Collections.emptyMap();
		}

		String guid = findGUIDFromFile((YAMLFile) psiFile);

		if(guid == null)
		{
			return Collections.emptyMap();
		}

		VirtualFile file = fileContent.getFile();

		VirtualFile parent = file.getParent();

		String ownerFileName = StringUtil.trimEnd(file.getName(), "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
		VirtualFile owner = parent.findChild(ownerFileName);
		if(owner == null)
		{
			return Collections.emptyMap();
		}

		int fileId = FileBasedIndex.getFileId(owner);
		return Collections.singletonMap(guid, fileId);
	};

	public static String findGUIDFromFile(@NotNull YAMLFile psiFile)
	{
		String guid = null;
		List<YAMLDocument> documents = psiFile.getDocuments();
		for(YAMLDocument document : documents)
		{
			YAMLValue topLevelValue = document.getTopLevelValue();
			if(topLevelValue instanceof YAMLMapping)
			{
				YAMLKeyValue guidValue = ((YAMLMapping) topLevelValue).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
				guid = guidValue == null ? null : guidValue.getValueText();
			}
		}
		return guid;
	}

	private EnumeratorIntegerDescriptor myDescriptor = new EnumeratorIntegerDescriptor();
	private EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

	@NotNull
	@Override
	public FileBasedIndex.InputFilter getInputFilter()
	{
		return new DefaultFileTypeSpecificInputFilter(Unity3dMetaFileType.INSTANCE);
	}

	@Override
	public boolean dependsOnFileContent()
	{
		return true;
	}

	@NotNull
	@Override
	public ID<String, Integer> getName()
	{
		return KEY;
	}

	@NotNull
	@Override
	public DataIndexer<String, Integer, FileContent> getIndexer()
	{
		return myIndexer;
	}

	@NotNull
	@Override
	public KeyDescriptor<String> getKeyDescriptor()
	{
		return myKeyDescriptor;
	}

	@NotNull
	@Override
	public DataExternalizer<Integer> getValueExternalizer()
	{
		return myDescriptor;
	}

	@Override
	public int getVersion()
	{
		return ourVersion;
	}
}
