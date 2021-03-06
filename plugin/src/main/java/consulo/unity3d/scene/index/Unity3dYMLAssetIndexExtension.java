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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.openapi.util.text.StringUtil;
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
import consulo.unity3d.scene.Unity3dMetaManager;
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
public class Unity3dYMLAssetIndexExtension extends FileBasedIndexExtension<Integer, List<Unity3dYMLAsset>>
{
	public static final ID<Integer, List<Unity3dYMLAsset>> KEY = ID.create("unity3d.yml.asset.new.index");
	public static final String ourCustomGUIDPrefix = "__guid:";

	private static final int ourVersion = 12;
	private static final String ourGameObject = "GameObject";
	private static final Set<String> ourAcceptKeys = ContainerUtil.newTroveSet("MonoBehaviour", "Prefab", "Transform", ourGameObject, "TrailRenderer");
	private static final Set<String> ourGuidKeys = ContainerUtil.newTroveSet("m_PrefabParentObject", "m_Script", "m_ParentPrefab");

	private static final String ourGameObjectNameField = "m_Name";

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
				dataOutput.writeInt(asset.getStartOffset());
				String gameObjectName = asset.getGameObjectName();
				dataOutput.writeBoolean(gameObjectName != null);
				if(gameObjectName != null)
				{
					dataOutput.writeUTF(gameObjectName);
				}

				DataInputOutputUtil.writeSeq(dataOutput, asset.getValues(), it ->
				{
					dataOutput.writeUTF(it.getName());
					dataOutput.writeUTF(it.getValue());
					dataOutput.writeInt(it.getOffset());
				});
			});
		}

		@Override
		public List<Unity3dYMLAsset> read(DataInput dataInput) throws IOException
		{
			return DataInputOutputUtil.readSeq(dataInput, () ->
			{
				String guid = dataInput.readUTF();
				int startOffset = dataInput.readInt();
				boolean gameObjectCheck = dataInput.readBoolean();
				String gameObjectName = null;
				if(gameObjectCheck)
				{
					gameObjectName = dataInput.readUTF();
				}
				List<Unity3dYMLField> values = DataInputOutputUtil.readSeq(dataInput, () ->
				{
					String name = dataInput.readUTF();
					String value = dataInput.readUTF();
					int offset = dataInput.readInt();
					return new Unity3dYMLField(name, value, offset);
				});
				return new Unity3dYMLAsset(guid, gameObjectName, startOffset, values);
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

		String currentGameObjectName = null;

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
				// optimization
				for(PsiElement keyValue = topLevelValue.getFirstChild(); keyValue != null; keyValue = keyValue.getNextSibling())
				{
					if(!(keyValue instanceof YAMLKeyValue))
					{
						continue;
					}

					String keyText = ((YAMLKeyValue) keyValue).getKeyText();
					YAMLValue value = ((YAMLKeyValue) keyValue).getValue();
					if(ourAcceptKeys.contains(keyText) && value instanceof YAMLMapping)
					{
						YAMLMapping mapping = (YAMLMapping) value;

						String scriptGuid = null;
						int startOffset = 0;
						List<Unity3dYMLField> values = null;

						// optimization
						for(PsiElement keyValuePair = mapping.getFirstChild(); keyValuePair != null; keyValuePair = keyValuePair.getNextSibling())
						{
							if(!(keyValuePair instanceof YAMLKeyValue))
							{
								continue;
							}

							YAMLKeyValue temp = (YAMLKeyValue) keyValuePair;
							String fieldName = temp.getKeyText();
							YAMLValue fieldValue = temp.getValue();
							if(fieldValue == null)
							{
								continue;
							}

							if(ourGameObject.equals(keyText) && ourGameObjectNameField.equals(fieldName))
							{
								currentGameObjectName = fieldValue instanceof YAMLScalar ? ((YAMLScalar) fieldValue).getTextValue() : null;
							}

							if(ourGuidKeys.contains(fieldName))
							{
								if(fieldValue instanceof YAMLMapping)
								{
									YAMLKeyValue guidKeyValue = ((YAMLMapping) fieldValue).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
									if(guidKeyValue != null)
									{
										YAMLValue guidValue = guidKeyValue.getValue();
										scriptGuid = guidValue instanceof YAMLScalar ? ((YAMLScalar) guidValue).getTextValue() : null;
										startOffset = guidValue == null ? 0 : guidValue.getTextOffset();
										if(scriptGuid != null)
										{
											values = new ArrayList<>();
										}
									}
								}
							}

							if(values != null)
							{
								String text = fieldValue.getText();
								if(fieldValue instanceof YAMLMapping)
								{
									YAMLKeyValue keyValueByKey = ((YAMLMapping) fieldValue).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
									if(keyValueByKey != null)
									{
										text = ourCustomGUIDPrefix + keyValueByKey.getValueText();
									}
								}
								values.add(new Unity3dYMLField(fieldName, StringUtil.first(text, 50, true), fieldValue.getTextOffset()));
							}
						}

						if(scriptGuid != null)
						{
							final int key = Math.abs(FileBasedIndex.getFileId(fileContent.getFile()));
							Unity3dYMLAsset unity3DYMLAsset = new Unity3dYMLAsset(scriptGuid, currentGameObjectName, startOffset, values);

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

	@Nonnull
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

	@Nonnull
	@Override
	public ID<Integer, List<Unity3dYMLAsset>> getName()
	{
		return KEY;
	}

	@Nonnull
	@Override
	public DataIndexer<Integer, List<Unity3dYMLAsset>, FileContent> getIndexer()
	{
		return myIndexer;
	}

	@Nonnull
	@Override
	public KeyDescriptor<Integer> getKeyDescriptor()
	{
		return myKeyDescriptor;
	}

	@Nonnull
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
