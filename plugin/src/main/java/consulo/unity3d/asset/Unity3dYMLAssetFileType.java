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

package consulo.unity3d.asset;

import consulo.gameFramework.icon.GameFrameworkIconGroup;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dYMLAssetFileType extends LanguageFileType {
    public static final Unity3dYMLAssetFileType INSTANCE = new Unity3dYMLAssetFileType();

    private Unity3dYMLAssetFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @Nonnull
    @Override
    public String getId() {
        return "UNITY_YML_ASSET";
    }

    @Nonnull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("Unity YAML Asset File");
    }

    @Nullable
    @Override
    public Image getIcon() {
        return GameFrameworkIconGroup.prefabfile();
    }
}
