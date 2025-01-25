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

package consulo.unity3d.scene;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.ProgressManager;
import consulo.application.util.LowMemoryWatcher;
import consulo.application.util.function.CommonProcessors;
import consulo.disposer.Disposable;
import consulo.gameFramework.meta.MetadataService;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.project.Project;
import consulo.unity3d.asset.Unity3dAssetUtil;
import consulo.unity3d.meta.Unity3dMetaFileType;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;
import consulo.util.collection.MultiMap;
import consulo.util.lang.Comparing;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.AsyncFileListener;
import consulo.virtualFileSystem.event.VFileEvent;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class Unity3dMetaManager implements Disposable {
    public static final String GUID_KEY = "guid";

    @Nonnull
    public static Unity3dMetaManager getInstance(@Nonnull Project project) {
        return project.getInstance(Unity3dMetaManager.class);
    }

    private Project myProject;
    private final MetadataService myMetadataService;
    private Map<String, MultiMap<VirtualFile, Unity3dYMLAsset>> myAttaches = new ConcurrentHashMap<>();

    @Inject
    public Unity3dMetaManager(Project project, VirtualFileManager virtualFileManager, MetadataService metadataService) {
        myProject = project;
        myMetadataService = metadataService;
        virtualFileManager.addAsyncFileListener(new AsyncFileListener() {
            @Nonnull
            @Override
            public ChangeApplier prepareChange(@Nonnull List<? extends VFileEvent> list) {
                return new ChangeApplier() {
                    @Override
                    public void afterVfsChange() {
                        for (VFileEvent vFileEvent : list) {
                            VirtualFile file = vFileEvent.getFile();
                            if (file == null) {
                                continue;
                            }

                            if (clearIfNeed(file)) {
                                break;
                            }
                        }
                    }

                    private boolean clearIfNeed(@Nonnull VirtualFile virtualFile) {
                        if (virtualFile.getFileType() == Unity3dMetaFileType.INSTANCE) {
                            myAttaches.clear();
                            return true;
                        }
                        return false;
                    }
                };
            }
        }, this);

        LowMemoryWatcher.register(this::clear, this);
    }

    private void clear() {
        myAttaches.clear();
    }

    @Nullable
    @RequiredReadAction
    public VirtualFile findFileByGUID(@Nonnull String guid) {
        return myMetadataService.findFileByMetadataId(guid);
    }

    @Nonnull
    public MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@Nonnull VirtualFile file) {
        String uuid = Unity3dAssetUtil.getGUID(myProject, file);
        if (uuid == null) {
            return MultiMap.empty();
        }

        VirtualFile baseDir = myProject.getBaseDir();
        if (baseDir == null) {
            return MultiMap.empty();
        }
        return myAttaches.computeIfAbsent(uuid, it ->
        {
            CommonProcessors.CollectProcessor<Integer> fileIds = new CommonProcessors.CollectProcessor<>();

            FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
            fileBasedIndex.processAllKeys(Unity3dYMLAssetIndexExtension.KEY, fileIds, myProject);

            GlobalSearchScope scope = GlobalSearchScope.projectScope(myProject);
            MultiMap<VirtualFile, Unity3dYMLAsset> map = MultiMap.create();
            for (int fileId : fileIds.getResults()) {
                ProgressManager.checkCanceled();

                VirtualFile assertFile = fileBasedIndex.findFileById(myProject, fileId);
                if (assertFile == null) {
                    continue;
                }

                if (!VirtualFileUtil.isAncestor(baseDir, assertFile, false)) {
                    continue;
                }

                fileBasedIndex.processValues(Unity3dYMLAssetIndexExtension.KEY, fileId, assertFile, (virtualFile, list) ->
                {
                    for (Unity3dYMLAsset asset : list) {
                        if (Comparing.equal(uuid, asset.getGuid())) {
                            map.putValue(assertFile, asset);
                        }
                    }

                    return true;
                }, scope);
            }
            return map;
        });
    }

    @Nullable
    @RequiredReadAction
    public String getGUID(@Nonnull VirtualFile virtualFile) {
        return myMetadataService.getFileIdFromMetadata(virtualFile);
    }

    @Override
    public void dispose() {
        clear();
    }
}
