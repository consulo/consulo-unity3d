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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.SingleEntryFileBasedIndexExtension;
import com.intellij.util.indexing.SingleEntryIndexer;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import consulo.unity3d.Unity3dMetaFileType;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dMetaIndexExtension extends SingleEntryFileBasedIndexExtension<String>
{
	public static final ID<Integer, String> KEY = ID.create("unity3d.meta.index");

	private static final int ourVersion = 1;

	private SingleEntryIndexer<String> myIndexer = new SingleEntryIndexer<String>(false)
	{
		@Nullable
		@Override
		protected String computeValue(@NotNull FileContent fileContent)
		{
			PsiFile psiFile = fileContent.getPsiFile();
			if(psiFile instanceof YAMLFile)
			{
				List<YAMLDocument> documents = ((YAMLFile) psiFile).getDocuments();
				for(YAMLDocument document : documents)
				{
					YAMLValue topLevelValue = document.getTopLevelValue();
					if(topLevelValue instanceof YAMLMapping)
					{
						YAMLKeyValue guidValue = ((YAMLMapping) topLevelValue).getKeyValueByKey("guid");
						return guidValue == null ? null : guidValue.getValueText();
					}
				}
			}
			return null;
		}
	};

	private EnumeratorStringDescriptor myDescriptor = new EnumeratorStringDescriptor();

	@NotNull
	@Override
	public FileBasedIndex.InputFilter getInputFilter()
	{
		return new DefaultFileTypeSpecificInputFilter(Unity3dMetaFileType.INSTANCE);
	}

	@NotNull
	@Override
	public ID<Integer, String> getName()
	{
		return KEY;
	}

	@NotNull
	@Override
	public SingleEntryIndexer<String> getIndexer()
	{
		return myIndexer;
	}

	@NotNull
	@Override
	public DataExternalizer<String> getValueExternalizer()
	{
		return myDescriptor;
	}

	@Override
	public int getVersion()
	{
		return ourVersion;
	}
}
