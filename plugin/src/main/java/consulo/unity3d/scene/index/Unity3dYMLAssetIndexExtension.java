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

import consulo.annotation.DeprecationInfo;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.index.io.DataIndexer;
import consulo.index.io.ExternalIntegerKeyDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.index.io.data.DataExternalizer;
import consulo.index.io.data.DataInputOutputUtil;
import consulo.language.ast.LighterAST;
import consulo.language.ast.LighterASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.*;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.asset.Unity3dYMLAssetFileType;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLParserDefinition;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

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
@ExtensionImpl
public class Unity3dYMLAssetIndexExtension extends FileBasedIndexExtension<Integer, List<Unity3dYMLAsset>>
{
	public static final ID<Integer, List<Unity3dYMLAsset>> KEY = ID.create("unity3d.yml.asset.new.index");
	public static final String ourCustomGUIDPrefix = "__guid:";

	private static final int ourVersion = 13;
	private static final String ourGameObject = "GameObject";
	private static final Set<String> ourAcceptKeys = Set.of("MonoBehaviour", "Prefab", "Transform", ourGameObject, "TrailRenderer");
	private static final Set<String> ourGuidKeys = Set.of("m_PrefabParentObject", "m_Script", "m_ParentPrefab");

	private static final String ourGameObjectNameField = "m_Name";

	private final DefaultFileTypeSpecificInputFilter myInputFilter = new DefaultFileTypeSpecificInputFilter(Unity3dYMLAssetFileType.INSTANCE);
	private final ExternalIntegerKeyDescriptor myKeyDescriptor = new ExternalIntegerKeyDescriptor();

	private final DataExternalizer<List<Unity3dYMLAsset>> myExternalizer = new DataExternalizer<>()
	{
		@Override
		public void save(DataOutput dataOutput, List<Unity3dYMLAsset> list) throws IOException
		{
			DataInputOutputUtil.writeSeq(dataOutput, list, asset ->
			{
				dataOutput.writeUTF(asset.guid());
				dataOutput.writeInt(asset.startOffset());
				String gameObjectName = asset.gameObjectName();
				dataOutput.writeBoolean(gameObjectName != null);
				if(gameObjectName != null)
				{
					dataOutput.writeUTF(gameObjectName);
				}

				DataInputOutputUtil.writeSeq(dataOutput, asset.values(), it ->
				{
					dataOutput.writeUTF(it.name());
					dataOutput.writeUTF(it.value());
					dataOutput.writeInt(it.offset());
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

	private static final TokenSet ourYamlValuesSet = TokenSet.create(YAMLElementTypes.MAPPING,
			YAMLElementTypes.SCALAR_PLAIN_VALUE,
			YAMLElementTypes.SCALAR_TEXT_VALUE,
			YAMLElementTypes.SCALAR_LIST_VALUE,
			YAMLElementTypes.HASH,
			YAMLElementTypes.ARRAY,
			YAMLElementTypes.SEQUENCE,
			YAMLElementTypes.SEQUENCE_ITEM,
			YAMLElementTypes.COMPOUND_VALUE,
			YAMLElementTypes.SCALAR_QUOTED_STRING);

	public static final TokenSet YAML_MAPPING_SET = TokenSet.create(YAMLElementTypes.MAPPING, YAMLElementTypes.HASH);

	private final DataIndexer<Integer, List<Unity3dYMLAsset>, FileContent> myIndexer = Unity3dYMLAssetIndexExtension::indexViaLightAst;

	private static Map<Integer, List<Unity3dYMLAsset>> indexViaLightAst(FileContent fileContent)
	{
		PsiDependentFileContent dependentFileContent = (PsiDependentFileContent) fileContent;

		LighterAST ast = dependentFileContent.getLighterAST();

		LighterASTNode fileAst = ast.getRoot();

		if(fileAst.getTokenType() != YAMLParserDefinition.FILE)
		{
			return Map.of();
		}

		CharSequence fileText = fileContent.getContentAsText();

		Map<Integer, List<Unity3dYMLAsset>> map = new HashMap<>();

		String currentGameObjectName = null;

		for(LighterASTNode documentAst : ast.getChildren(fileAst))
		{
			if(documentAst.getTokenType() != YAMLElementTypes.DOCUMENT)
			{
				continue;
			}

			LighterASTNode mapping = findNode(documentAst, ast, YAML_MAPPING_SET);
			if(mapping == null)
			{
				continue;
			}

			List<LighterASTNode> keyValues = ast.getChildren(mapping);

			for(LighterASTNode keyValue : keyValues)
			{
				if(keyValue.getTokenType() != YAMLElementTypes.KEY_VALUE_PAIR)
				{
					continue;
				}

				List<LighterASTNode> keyValueChildren = ast.getChildren(keyValue);

				if(keyValueChildren.size() < 2)
				{
					continue;
				}

				LighterASTNode nameNode = ContainerUtil.find(keyValueChildren, it -> it.getTokenType() == YAMLTokenTypes.SCALAR_KEY);
				LighterASTNode valueNode = ContainerUtil.find(keyValueChildren, it -> YAML_MAPPING_SET.contains(it.getTokenType()));

				if(nameNode == null || valueNode == null)
				{
					continue;
				}

				// key, must be ends with ':' cut semicolon
				String keyText = fileText.subSequence(nameNode.getStartOffset(), nameNode.getEndOffset() - 1).toString();
				if(!ourAcceptKeys.contains(keyText))
				{
					continue;
				}

				String scriptGuid = null;
				int startOffset = 0;
				List<Unity3dYMLField> values = null;

				List<LighterASTNode> fieldsNodes = ast.getChildren(valueNode);

				for(LighterASTNode fieldNode : fieldsNodes)
				{
					if(fieldNode.getTokenType() != YAMLElementTypes.KEY_VALUE_PAIR)
					{
						continue;
					}

					List<LighterASTNode> fieldNodes = ast.getChildren(fieldNode);

					LighterASTNode fieldNameNode = ContainerUtil.find(fieldNodes, it -> it.getTokenType() == YAMLTokenTypes.SCALAR_KEY);
					LighterASTNode fieldValueNode = ContainerUtil.find(fieldNodes, it -> ourYamlValuesSet.contains(it.getTokenType()));

					if(fieldNameNode == null || fieldValueNode == null)
					{
						continue;
					}

					String fieldName = fileText.subSequence(fieldNameNode.getStartOffset(), fieldNameNode.getEndOffset() - 1).toString();
					if(fieldName.length() == 0)
					{
						continue;
					}

					if(ourGameObject.equals(keyText) && ourGameObjectNameField.equals(fieldName))
					{
						if(fieldValueNode.getTokenType() == YAMLElementTypes.SCALAR_PLAIN_VALUE)
						{
							currentGameObjectName = fileText.subSequence(fieldValueNode.getStartOffset(), fieldValueNode.getEndOffset()).toString();
						}
						else if(fieldValueNode.getTokenType() == YAMLElementTypes.SCALAR_QUOTED_STRING)
						{
							CharSequence quotedString = fileText.subSequence(fieldValueNode.getStartOffset(), fieldValueNode.getEndOffset());
							currentGameObjectName = StringUtil.unquoteString(quotedString.toString());
						}
					}

					if(ourGuidKeys.contains(fieldName) && YAML_MAPPING_SET.contains(fieldValueNode.getTokenType()))
					{
						Pair<String, LighterASTNode> guidFieldValue = findGUIDFieldValue(fieldValueNode, ast, fileText);
						if(guidFieldValue != null)
						{
							scriptGuid = guidFieldValue.getFirst();
							startOffset = guidFieldValue.getSecond().getStartOffset();
							values = new ArrayList<>();
						}
					}

					if(values != null)
					{
						String fieldValueText = null;
						if(YAML_MAPPING_SET.contains(fieldValueNode.getTokenType()))
						{
							Pair<String, LighterASTNode> guidFieldValue = findGUIDFieldValue(fieldValueNode, ast, fileText);
							if(guidFieldValue != null)
							{
								fieldValueText = ourCustomGUIDPrefix + guidFieldValue.getFirst();
							}
						}
						else if(fieldValueNode.getTokenType() == YAMLElementTypes.SCALAR_QUOTED_STRING)
						{
							String quoted = fileText.subSequence(fieldValueNode.getStartOffset(), fieldValueNode.getEndOffset()).toString();

							fieldValueText = StringUtil.unquoteString(quoted);
						}

						if(fieldValueText == null)
						{
							fieldValueText = fileText.subSequence(fieldValueNode.getStartOffset(), fieldValueNode.getEndOffset()).toString();
						}
						values.add(new Unity3dYMLField(fieldName, StringUtil.first(fieldValueText, 50, true), fieldValueNode.getStartOffset()));
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
		return map;
	}

	@Nullable
	public static Pair<String, LighterASTNode> findGUIDFieldValue(LighterASTNode fieldValueNode, LighterAST ast, CharSequence fileText)
	{
		List<LighterASTNode> possibleGuidsNodes = ast.getChildren(fieldValueNode);
		for(LighterASTNode possibleGuidNode : possibleGuidsNodes)
		{
			List<LighterASTNode> possibleGuidNodes = ast.getChildren(possibleGuidNode);

			LighterASTNode possibleGuidKey = ContainerUtil.find(possibleGuidNodes, it -> it.getTokenType() == YAMLTokenTypes.SCALAR_KEY);
			LighterASTNode possibleGuidValue = ContainerUtil.find(possibleGuidNodes, it -> ourYamlValuesSet.contains(it.getTokenType()));

			if(possibleGuidKey == null || possibleGuidValue == null)
			{
				continue;
			}

			CharSequence possibleGuidText = fileText.subSequence(possibleGuidKey.getStartOffset(), possibleGuidKey.getEndOffset() - 1);

			if(StringUtil.equals(possibleGuidText, Unity3dMetaManager.GUID_KEY))
			{
				CharSequence guidValue = fileText.subSequence(possibleGuidValue.getStartOffset(), possibleGuidValue.getEndOffset());
				return Pair.create(guidValue.toString(), possibleGuidValue);
			}
		}

		return null;
	}

	@Nullable
	public static LighterASTNode findNode(LighterASTNode node, LighterAST ast, TokenSet tokenSet)
	{
		List<LighterASTNode> children = ast.getChildren(node);
		return ContainerUtil.find(children, it -> tokenSet.contains(it.getTokenType()));
	}

	@RequiredReadAction
	@Deprecated
	@DeprecationInfo("Slow version of indexing")
	private static Map<Integer, List<Unity3dYMLAsset>> indexViaPsi(FileContent fileContent)
	{
		PsiFile psiFile = fileContent.getPsiFile();
		if(!(psiFile instanceof YAMLFile))
		{
			return Map.of();
		}

		Map<Integer, List<Unity3dYMLAsset>> map = new HashMap<>();

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
								String fieldText = fieldValue.getText();
								if(fieldValue instanceof YAMLMapping)
								{
									YAMLKeyValue keyValueByKey = ((YAMLMapping) fieldValue).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
									if(keyValueByKey != null)
									{
										fieldText = ourCustomGUIDPrefix + keyValueByKey.getValueText();
									}
								}
								values.add(new Unity3dYMLField(fieldName, StringUtil.first(fieldText, 50, true), fieldValue.getTextOffset()));
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
	}

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
