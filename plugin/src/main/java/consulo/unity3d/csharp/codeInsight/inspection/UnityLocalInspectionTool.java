/*
 * Copyright 2013-2022 consulo.io
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

package consulo.unity3d.csharp.codeInsight.inspection;

import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.unity3d.localize.Unity3dLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
public abstract class UnityLocalInspectionTool extends LocalInspectionTool {
    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return Unity3dLocalize.inspectionUnityGroupName();
    }
}
