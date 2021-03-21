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

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorIntegerDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import consulo.annotation.access.RequiredReadAction;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.util.lang.Pair;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dMetaIndexExtension extends FileBasedIndexExtension<String, Integer>
{
	public static final ID<String, Integer> KEY = ID.create("unity3d.meta.index");

	private static final int ourVersion = 5;

	private DataIndexer<String, Integer, FileContent> myIndexer = fileContent ->
	{
		PsiDependentFileContent dependentFileContent = (PsiDependentFileContent) fileContent;

		LighterAST ast = dependentFileContent.getLighterAST();

		LighterASTNode fileAst = ast.getRoot();

		if(fileAst.getTokenType() != YAMLElementTypes.FILE)
		{
			return Map.of();
		}

		CharSequence fileText = fileContent.getContentAsText();

		String guid = null;
		for(LighterASTNode documentAst : ast.getChildren(fileAst))
		{
			if(documentAst.getTokenType() != YAMLElementTypes.DOCUMENT)
			{
				continue;
			}

			LighterASTNode mapping = Unity3dYMLAssetIndexExtension.findNode(documentAst, ast, Unity3dYMLAssetIndexExtension.YAML_MAPPING_SET);
			if(mapping == null)
			{
				continue;
			}

			Pair<String, LighterASTNode> guidFieldValue = Unity3dYMLAssetIndexExtension.findGUIDFieldValue(mapping, ast, fileText);
			if(guidFieldValue != null)
			{
				guid = guidFieldValue.getFirst();
				break;
			}
		}

		if(guid == null)
		{
			return Map.of();
		}

		VirtualFile file = fileContent.getFile();

		VirtualFile parent = file.getParent();

		String ownerFileName = StringUtil.trimEnd(file.getName(), "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
		VirtualFile owner = parent.findChild(ownerFileName);
		if(owner == null)
		{
			return Map.of();
		}

		int fileId = FileBasedIndex.getFileId(owner);
		return Map.of(guid, fileId);
	};

	@RequiredReadAction
	public static String findGUIDFromFile(@Nonnull YAMLFile psiFile)
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

	@Nonnull
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

	@Nonnull
	@Override
	public ID<String, Integer> getName()
	{
		return KEY;
	}

	@Nonnull
	@Override
	public DataIndexer<String, Integer, FileContent> getIndexer()
	{
		return myIndexer;
	}

	@Nonnull
	@Override
	public KeyDescriptor<String> getKeyDescriptor()
	{
		return myKeyDescriptor;
	}

	@Nonnull
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
