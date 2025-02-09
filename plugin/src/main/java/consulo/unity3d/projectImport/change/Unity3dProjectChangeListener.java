/*
 * Copyright 2013-2018 consulo.io
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

package consulo.unity3d.projectImport.change;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.disposer.Disposable;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationAction;
import consulo.project.ui.notification.NotificationType;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.UIUtil;
import consulo.unity3d.UnityNotificationGroup;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.virtualFileSystem.StandardFileSystems;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.AsyncFileListener;
import consulo.virtualFileSystem.event.VFileCreateEvent;
import consulo.virtualFileSystem.event.VFileEvent;
import consulo.virtualFileSystem.event.VFileMoveEvent;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.virtualFileSystem.pointer.VirtualFilePointerManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author VISTALL
 * @since 1/11/18
 */
@Singleton
@ServiceAPI(value = ComponentScope.PROJECT)
@ServiceImpl
public class Unity3dProjectChangeListener implements Disposable {
    public static class DataBlock {
        List<VirtualFile> myFiles = new ArrayList<>();
    }

    private final Project myProject;
    private VirtualFilePointer myAssetsDirPointer;

    @Nonnull
    private Future<?> myUpdateCheckTask = CompletableFuture.completedFuture(null);

    private volatile DataBlock myDataBlock = new DataBlock();

    private final AtomicBoolean myActive = new AtomicBoolean();

    private final Set<FileType> mySourceFileTypes = new HashSet<>();

    @Inject
    public Unity3dProjectChangeListener(@Nonnull Project project, @Nonnull VirtualFileManager virtualFileManager) {
        myProject = project;

        if (project.isDefault()) {
            return;
        }

        for (Unity3dProjectSourceFileTypeFactory factory : Unity3dProjectSourceFileTypeFactory.EP_NAME.getExtensionList()) {
            factory.registerFileTypes(mySourceFileTypes::add);
        }

        myProject.getMessageBus().connect().subscribe(ModuleRootListener.class, new ModuleRootListener() {
            @Override
            @RequiredReadAction
            public void rootsChanged(ModuleRootEvent event) {
                checkAndRunIfNeed();
            }
        });

        myAssetsDirPointer = VirtualFilePointerManager.getInstance().create(StandardFileSystems.FILE_PROTOCOL_PREFIX + myProject.getPresentableUrl() + "/" + Unity3dProjectImporter
            .ASSETS_DIRECTORY, this, null);

        virtualFileManager.addAsyncFileListener(new AsyncFileListener() {
            @Nullable
            @Override
            public ChangeApplier prepareChange(@Nonnull List<? extends VFileEvent> list) {
                List<VFileEvent> addOrMove = new ArrayList<>();
                for (VFileEvent vFileEvent : list) {
                    if (vFileEvent instanceof VFileCreateEvent || vFileEvent instanceof VFileMoveEvent) {
                        addOrMove.add(vFileEvent);
                    }
                }
                if (addOrMove.isEmpty()) {
                    return null;
                }
                return new ChangeApplier() {
                    @Override
                    public void afterVfsChange() {
                        for (VFileEvent vFileEvent : addOrMove) {
                            VirtualFile file = vFileEvent.getFile();

                            if (file != null) {
                                handleChange(file);
                            }
                        }
                    }
                };
            }
        }, this);
    }

    public void checkAndRunIfNeed() {
        myUpdateCheckTask.cancel(false);
        myUpdateCheckTask = CompletableFuture.completedFuture(null);

        myActive.set(ReadAction.compute(() -> Unity3dModuleExtensionUtil.getRootModuleExtension(myProject)) != null);

        if (!myActive.get()) {
            return;
        }

        myUpdateCheckTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) this::checkNotification, 10, 10, TimeUnit.SECONDS);
    }

    private void checkNotification() {
        if (DumbService.isDumb(myProject)) {
            return;
        }

        DataBlock dataBlock = myDataBlock;

        myDataBlock = new DataBlock();

        if (dataBlock.myFiles.isEmpty()) {
            return;
        }

        UIUtil.invokeLaterIfNeeded(() ->
        {
            boolean needNotification = false;

            for (VirtualFile file : dataBlock.myFiles) {
                Module module = ModuleUtilCore.findModuleForFile(file, myProject);
                if (module == null) {
                    continue;
                }

                if (module == Unity3dModuleExtensionUtil.getRootModule(myProject)) {
                    needNotification = true;
                    break;
                }
            }

            if (needNotification) {
                Notification notification = new Notification(UnityNotificationGroup.INSTANCE, Application.get().getName().get(), "Unity project structure changed", NotificationType.INFORMATION);
                notification.addAction(new NotificationAction(LocalizeValue.localizeTODO("Rebuild Project")) {
                    @RequiredUIAccess
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent, @Nonnull Notification notification) {
                        final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
                        if (rootModuleExtension == null) {
                            return;
                        }

                        Unity3dProjectImporter.syncProjectStep(myProject, rootModuleExtension.getSdk(), null, true);
                    }
                });
                notification.notify(myProject);
            }
        });
    }

    private void handleChange(@Nonnull VirtualFile virtualFile) {
        if (!myActive.get()) {
            return;
        }

        if (!mySourceFileTypes.contains(virtualFile.getFileType())) {
            return;
        }

        String url = virtualFile.getUrl();
        if (url.startsWith(myAssetsDirPointer.getUrl())) {
            myDataBlock.myFiles.add(virtualFile);
        }
    }

    @Override
    public void dispose() {
        myUpdateCheckTask.cancel(false);
    }
}
