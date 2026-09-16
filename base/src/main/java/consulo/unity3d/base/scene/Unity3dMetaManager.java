/*
 * Copyright 2013-2026 consulo.io
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

package consulo.unity3d.base.scene;

import consulo.unity3d.base.scene.Unity3dYMLAsset;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.project.Project;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Resolves Unity asset GUIDs, which live in the {@code .meta} file beside every asset.
 * <p>
 * The implementation is index backed and lives in the main plugin module; callers that only need GUID lookup -
 * the project importer, for one - depend on this interface instead.
 *
 * @author VISTALL
 */
@ServiceAPI(ComponentScope.PROJECT)
public interface Unity3dMetaManager {
    String GUID_KEY = "guid";

    @Nonnull
    static Unity3dMetaManager getInstance(@Nonnull Project project) {
        return project.getInstance(Unity3dMetaManager.class);
    }

    @Nullable
    @RequiredReadAction
    String getGUID(@Nonnull VirtualFile virtualFile);

    @Nullable
    @RequiredReadAction
    VirtualFile findFileByGUID(@Nonnull String guid);

    @Nonnull
    MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@Nonnull VirtualFile file);
}
