/*
 * Copyright 2013-2025 consulo.io
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

package consulo.unity3d.meta;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.gameFramework.meta.MetadataFileType;
import consulo.gameFramework.meta.MetadataProvider;
import consulo.language.ast.LighterAST;
import consulo.language.ast.LighterASTNode;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.PsiDependentFileContent;
import consulo.project.Project;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.psi.*;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-01-23
 */
@ExtensionImpl
public class UnityMetadataProvider implements MetadataProvider {
    @Nonnull
    @Override
    public String getExtension() {
        return "meta";
    }

    @RequiredReadAction
    @Override
    public boolean isAvailable(@Nonnull Project project) {
        return Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String extractIdForIndex(@Nonnull PsiDependentFileContent fileContent) {
        LighterAST ast = fileContent.getLighterAST();

        LighterASTNode fileAst = ast.getRoot();

        if (fileAst.getTokenType() != YAMLElementTypes.FILE) {
            return null;
        }

        CharSequence fileText = fileContent.getContentAsText();

        String guid = null;
        for (LighterASTNode documentAst : ast.getChildren(fileAst)) {
            if (documentAst.getTokenType() != YAMLElementTypes.DOCUMENT) {
                continue;
            }

            LighterASTNode mapping = Unity3dYMLAssetIndexExtension.findNode(documentAst, ast, Unity3dYMLAssetIndexExtension.YAML_MAPPING_SET);
            if (mapping == null) {
                continue;
            }

            Pair<String, LighterASTNode> guidFieldValue = Unity3dYMLAssetIndexExtension.findGUIDFieldValue(mapping, ast, fileText);
            if (guidFieldValue != null) {
                guid = guidFieldValue.getFirst();
                break;
            }
        }

        return guid;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String extractId(@Nonnull PsiFile file) {
        if (file instanceof YAMLFile yamlFile) {
            return UnityMetadataProvider.findGUIDFromFile(yamlFile);
        }
        return null;
    }

    @Nonnull
    @Override
    public MetadataFileType getFileType() {
        return Unity3dMetaFileType.INSTANCE;
    }

    @RequiredReadAction
    public static String findGUIDFromFile(@Nonnull YAMLFile psiFile) {
        String guid = null;
        List<YAMLDocument> documents = psiFile.getDocuments();
        for (YAMLDocument document : documents) {
            YAMLValue topLevelValue = document.getTopLevelValue();
            if (topLevelValue instanceof YAMLMapping) {
                YAMLKeyValue guidValue = ((YAMLMapping) topLevelValue).getKeyValueByKey(Unity3dMetaManager.GUID_KEY);
                guid = guidValue == null ? null : guidValue.getValueText();
            }
        }
        return guid;
    }
}
