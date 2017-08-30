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

import gnu.trove.THashMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.ExternalIntegerKeyDescriptor;
import com.intellij.util.io.KeyDescriptor;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;

/**
 * @author VISTALL
 * @since 30-Aug-17
 * assert guid -> this class
 * YML example
 * <p>
 * MonoBehaviour:
 * m_ObjectHideFlags: 0
 * m_PrefabParentObject: {fileID: 0}
 * m_PrefabInternal: {fileID: 0}
 * m_GameObject: {fileID: 1085037891}
 * m_Enabled: 1
 * m_EditorHideFlags: 0
 * m_Script: {fileID: 11500000, guid: 38b5c2f743cd8034a8beeebf277c92c1, type: 3}
 * m_Name:
 * m_EditorClassIdentifier:
 * speed: 30
 */
public class Unity3dYMLAssetNewIndexExtension extends FileBasedIndexExtension<Integer, List<Unity3dYMLAsset>>
{
	public static final ID<Integer, List<Unity3dYMLAsset>> KEY = ID.create("unity3d.yml.asset.new.index");

	private static final int ourVersion = 6;
	private static final Set<String> ourAcceptKeys = ContainerUtil.newTroveSet("MonoBehaviour", "Prefab", "Transform", "GameObject", "TrailRenderer");
	private static final Set<String> ourGuidKeys = ContainerUtil.newTroveSet("m_PrefabParentObject", "m_Script", "m_ParentPrefab");

	private final DefaultFileTypeSpecificInputFilter myInputFilter = new DefaultFileTypeSpecificInputFilter(Unity3dYMLAssetFileType.INSTANCE);
	private final ExternalIntegerKeyDescriptor myKeyDescriptor = new ExternalIntegerKeyDescriptor();

	private final DataExternalizer<List<Unity3dYMLAsset>> myExternalizer = new DataExternalizer<List<Unity3dYMLAsset>>()
	{
		@Override
		public void save(DataOutput dataOutput, List<Unity3dYMLAsset> list) throws IOException
		{
			DataInputOutputUtil.writeSeq(dataOutput, list, asset ->
			{
				dataOutput.writeUTF(asset.getGuild());

				DataInputOutputUtil.writeSeq(dataOutput, asset.getValues(), it ->
				{
					dataOutput.writeUTF(it.getFirst());
					dataOutput.writeUTF(it.getSecond());
				});
			});
		}

		@Override
		public List<Unity3dYMLAsset> read(DataInput dataInput) throws IOException
		{
			return DataInputOutputUtil.readSeq(dataInput, () ->
			{
				String guid = dataInput.readUTF();
				List<Couple<String>> values = DataInputOutputUtil.readSeq(dataInput, () -> Couple.of(dataInput.readUTF(), dataInput.readUTF()));
				return new Unity3dYMLAsset(guid, values);
			});
		}
	};

	public final DataIndexer<Integer, List<Unity3dYMLAsset>, FileContent> myIndexer = fileContent ->
	{
		PsiFile psiFile = fileContent.getPsiFile();
		if(!(psiFile instanceof YAMLFile))
		{
			return Collections.emptyMap();
		}

		Map<Integer, List<Unity3dYMLAsset>> map = new THashMap<>();

		// optimization - do not call psiFile.getDocuments()
		for(PsiElement element = psiFile.getFirstChild(); element != null; element = element.getNextSibling())
		{
			if(!(element instanceof YAMLDocument))
			{
				continue;
			}

			YAMLDocument document = (YAMLDocument) element;

			YAMLValue topLevelValue = document.getTopLevelValue();
			if(topLevelValue instanceof YAMLMapping)
			{
				Collection<YAMLKeyValue> keyValues = ((YAMLMapping) topLevelValue).getKeyValues();
				for(YAMLKeyValue keyValue : keyValues)
				{
					String keyText = keyValue.getKeyText();
					YAMLValue value = keyValue.getValue();
					if(ourAcceptKeys.contains(keyText) && value instanceof YAMLMapping)
					{
						YAMLMapping mapping = (YAMLMapping) value;

						String scriptGuid = null;
						List<Couple<String>> values = null;

						// optimization
						for(PsiElement keyValuePair = mapping.getFirstChild(); keyValuePair != null; keyValuePair = keyValuePair.getNextSibling())
						{
							if(!(keyValuePair instanceof YAMLKeyValue))
							{
								continue;
							}

							YAMLKeyValue temp = (YAMLKeyValue) keyValuePair;
							String fieldNameText = temp.getKeyText();
							YAMLValue fieldValue = temp.getValue();

							if(ourGuidKeys.contains(fieldNameText))
							{
								if(fieldValue instanceof YAMLMapping)
								{
									YAMLKeyValue guidKeyValue = ((YAMLMapping) fieldValue).getKeyValueByKey("guid");
									if(guidKeyValue != null)
									{
										YAMLValue guidValue = guidKeyValue.getValue();
										scriptGuid = guidValue instanceof YAMLScalar ? ((YAMLScalar) guidValue).getTextValue() : null;
										if(scriptGuid != null)
										{
											values = new ArrayList<>();
										}
									}
								}
							}

							if(values != null)
							{
								if(fieldValue instanceof YAMLScalar)
								{
									values.add(Couple.of(fieldNameText, ((YAMLScalar) fieldValue).getTextValue()));
								}
							}
						}

						if(scriptGuid != null)
						{
							final int key = Math.abs(FileBasedIndex.getFileId(fileContent.getFile()));
							Unity3dYMLAsset unity3DYMLAsset = new Unity3dYMLAsset(scriptGuid, values);

							List<Unity3dYMLAsset> list = map.get(key);
							if(list != null)
							{
								list.add(unity3DYMLAsset);
							}
							else
							{
								list = new ArrayList<>();
								list.add(unity3DYMLAsset);
								map.put(key, list);
							}
						}
					}
				}
			}
		}

		return map;
	};

	@Override
	public boolean keyIsUniqueForIndexedFile()
	{
		return true;
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

	@NotNull
	@Override
	public ID<Integer, List<Unity3dYMLAsset>> getName()
	{
		return KEY;
	}

	@NotNull
	@Override
	public DataIndexer<Integer, List<Unity3dYMLAsset>, FileContent> getIndexer()
	{
		return myIndexer;
	}

	@NotNull
	@Override
	public KeyDescriptor<Integer> getKeyDescriptor()
	{
		return myKeyDescriptor;
	}

	@NotNull
	@Override
	public DataExternalizer<List<Unity3dYMLAsset>> getValueExternalizer()
	{
		return myExternalizer;
	}

	@Override
	public int getVersion()
	{
		return ourVersion;
	}
}
