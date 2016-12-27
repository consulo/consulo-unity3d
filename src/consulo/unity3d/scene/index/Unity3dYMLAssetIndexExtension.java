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

package consulo.unity3d.scene.index;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;

/**
 * @author VISTALL
 * @since 22.12.2015
 */
public class Unity3dYMLAssetIndexExtension extends ScalarIndexExtension<String>
{
	public static final ID<String, Void> KEY = ID.create("unity3d.yml.asset.index");

	private DataIndexer<String, Void, FileContent> myIndexer = new DataIndexer<String, Void, FileContent>()
	{
		@NotNull
		@Override
		public Map<String, Void> map(FileContent inputData)
		{
			Map<String, Void> map = new HashMap<String, Void>();
			CharSequence contentAsText = inputData.getContentAsText();
			for(int i = 0; i < contentAsText.length(); i++)
			{
				if(isGuid(i, contentAsText))
				{
					i += 6;

					String cut = cut(i, i + 32, contentAsText);
					if(cut != null)
					{
						i += 32;

						map.put(cut, null);
					}
				}
			}
			return map;
		}

		@Nullable
		private String cut(int i, int max, CharSequence charSequence)
		{
			if(max >= charSequence.length())
			{
				return null;
			}
			return charSequence.subSequence(i, max).toString();
		}

		private boolean isGuid(int i, CharSequence contentAsText)
		{
			return contains(i, contentAsText, 'g') &&
					contains(++i, contentAsText, 'u') &&
					contains(++i, contentAsText, 'i') &&
					contains(++i, contentAsText, 'd') &&
					contains(++i, contentAsText, ':') &&
					contains(++i, contentAsText, ' ');
		}

		private boolean contains(int i, CharSequence charSequence, char c)
		{
			return i < charSequence.length() && charSequence.charAt(i) == c;
		}
	};

	private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();
	private final DefaultFileTypeSpecificInputFilter myInputFilter = new DefaultFileTypeSpecificInputFilter(Unity3dYMLAssetFileType.INSTANCE);

	@NotNull
	@Override
	public ID<String, Void> getName()
	{
		return KEY;
	}

	@NotNull
	@Override
	public DataIndexer<String, Void, FileContent> getIndexer()
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
	public FileBasedIndex.InputFilter getInputFilter()
	{
		return myInputFilter;
	}

	@Override
	public boolean dependsOnFileContent()
	{
		return true;
	}

	@Override
	public int getVersion()
	{
		return 1;
	}
}
