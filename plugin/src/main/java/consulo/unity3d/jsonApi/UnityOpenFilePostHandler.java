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

package consulo.unity3d.jsonApi;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.ui.wm.IdeFocusManager;
import consulo.builtinWebServer.json.JsonPostRequestHandler;
import consulo.codeEditor.Editor;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkUtil;
import consulo.fileEditor.FileEditorManager;
import consulo.localize.LocalizeValue;
import consulo.module.creation.ModuleCreationHelper;
import consulo.module.creation.NewOrImportModuleUtil;
import consulo.module.creation.importing.ModuleImportContext;
import consulo.module.creation.importing.ModuleImportProvider;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.ProjectOpenContext;
import consulo.project.startup.StartupManager;
import consulo.project.ui.wm.IdeFrame;
import consulo.project.ui.wm.WindowManager;
import consulo.project.util.ProjectUtil;
import consulo.ui.Alert;
import consulo.ui.Alerts;
import consulo.ui.UIAccess;
import consulo.ui.UIAction;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dModuleImportProvider;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.CoroutineScope;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import consulo.util.concurrent.coroutine.step.CompletableFutureStep;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 14.11.2015
 */
@ExtensionImpl
public class UnityOpenFilePostHandler extends JsonPostRequestHandler<UnityOpenFilePostHandlerRequest> {
    private static final Set<String> ourSupportedContentTypes = Set.of("UnityEditor.MonoScript", "UnityEngine.Shader");
    private final Application myApplication;
    private final SdkTable mySdkTable;

    @Inject
    public UnityOpenFilePostHandler(Application application, SdkTable sdkTable) {
        super("unityOpenFile", UnityOpenFilePostHandlerRequest.class);
        myApplication = application;
        mySdkTable = sdkTable;
    }

    @Nonnull
    @Override
    public JsonResponse handle(@Nonnull final UnityOpenFilePostHandlerRequest body) {
        String contentType = body.contentType;

        if (!ourSupportedContentTypes.contains(contentType)) {
            return JsonResponse.asError("unsupported-content-type");
        }

        VirtualFile projectVirtualFile = LocalFileSystem.getInstance().findFileByPath(body.projectPath);
        if (projectVirtualFile == null) {
            return JsonResponse.asError("project-dir-not-exists");
        }

        Project targetProject = null;
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project openProject : openProjects) {
            if (ProjectUtil.isSameProject(body.projectPath, openProject)) {
                targetProject = openProject;
                break;
            }
        }

        if (targetProject != null) {
            final Project finalTargetProject = targetProject;
            StartupManager.getInstance(targetProject).runWhenProjectIsInitialized(() -> {
                postOpenFileRequest(finalTargetProject, finalTargetProject.getUIAccess(), body);
            });
            return JsonResponse.asSuccess(null);
        }

        if (!new File(projectVirtualFile.getPath(), Project.DIRECTORY_STORE_FOLDER).exists()) {
            String sdkPath = Platform.current().os().isMac() ? body.editorPath : new File(body.editorPath).getParentFile().getParentFile().getPath();

            VirtualFile sdkFileHome = LocalFileSystem.getInstance().findFileByPath(sdkPath);
            if (sdkFileHome == null) {
                IdeFrame frame = WindowManager.getInstance().findVisibleIdeFrame();
                if (frame != null) {
                    frame.activate();
                }
                myApplication.invokeLater(() -> Alerts.okError(LocalizeValue.localizeTODO("Unity path is not resolved: " + sdkPath)).showAsync());
                return JsonResponse.asError("unity-sdk-not-found");
            }

            Sdk targetSdk = null;
            List<Sdk> sdksOfType = mySdkTable.getSdksOfType(Unity3dBundleType.getInstance());
            for (Sdk sdk : sdksOfType) {
                VirtualFile homeDirectory = sdk.getHomeDirectory();
                if (sdkFileHome.equals(homeDirectory)) {
                    targetSdk = sdk;
                    break;
                }
            }

            Coroutine<?, Sdk> coroutineStep;
            if (targetSdk == null) {
                coroutineStep = UIAction.apply((i, continuation) -> {
                    UIAccess uiAccess = Objects.requireNonNull(continuation.getConfiguration(UIAccess.KEY));
                    return SdkUtil.createAndAddSDK(sdkPath, Unity3dBundleType.getInstance(), uiAccess);
                }).toCoroutine();
            }
            else {
                final Sdk finalTargetSdk = targetSdk;
                coroutineStep = CodeExecution.apply(i -> finalTargetSdk).toCoroutine();
            }

            coroutineStep = coroutineStep.then(UIAction.apply((sdk, continuation) -> {
                if (sdk == null) {
                    IdeFrame frame = WindowManager.getInstance().findVisibleIdeFrame();
                    if (frame != null) {
                        frame.activate();
                    }
                    Alerts.okError(LocalizeValue.localizeTODO("Unity SDK cant add by path: " + sdkPath)).showAsync();
                    continuation.cancel();
                    return null;
                }

                return sdk;
            }));

            Coroutine<?, Pair<ModuleImportContext, ModuleImportProvider<ModuleImportContext>>> next = coroutineStep.then(CompletableFutureStep.await((sdk, continuation) -> {
                Unity3dModuleImportProvider importProvider = new Unity3dModuleImportProvider(sdk, body);

                CompletableFuture<Pair<ModuleImportContext, ModuleImportProvider<ModuleImportContext>>> result = new CompletableFuture<>();

                UIAccess uiAccess = Objects.requireNonNull(continuation.getConfiguration(UIAccess.KEY));

                uiAccess.give(() -> {
                    myApplication.getInstance(ModuleCreationHelper.class)
                        .showImportChooser(null, projectVirtualFile, Collections.singletonList(importProvider), result);
                });

                return result;
            }));

            next = next.then(CodeExecution.apply((pair, continuation) -> {
                UIAccess uiAccess = Objects.requireNonNull(continuation.getConfiguration(UIAccess.KEY));

                ModuleImportContext context = pair.getFirst();

                ModuleImportProvider<ModuleImportContext> provider = pair.getSecond();

                Coroutine<Void, Project> importProjectAsync = NewOrImportModuleUtil.importProject(context, provider, uiAccess);

                CoroutineScope scope = CoroutineScope.of(myApplication.coroutineContext());
                scope.putCopyableUserData(UIAccess.KEY, uiAccess);

                importProjectAsync = importProjectAsync
                    .then(CompletableFutureStep.await(project -> {
                        ProjectOpenContext openContext = new ProjectOpenContext();
                        openContext.putUserData(ProjectOpenContext.ACTIVE_PROJECT, project);

                        return ProjectManager.getInstance().openProjectAsync(Path.of(project.getBasePath()), uiAccess, openContext);
                    }))
                    .then(CodeExecution.consume((project, c) -> {
                        if (project != null) {
                            postOpenFileRequest(project, uiAccess, body);
                        }
                    }));

                importProjectAsync.runAsync(scope, null);

                return null;
            }));

            CoroutineScope scope = CoroutineScope.of(myApplication.coroutineContext());
            scope.putCopyableUserData(UIAccess.KEY, myApplication.getLastUIAccess());

            next.runAsync(scope, null);
        }
        else {
            UIAccess uiAccess = myApplication.getLastUIAccess();

            CompletableFuture<Project> result = ProjectManager.getInstance()
                .openProjectAsync(projectVirtualFile.toNioPath(), uiAccess, new ProjectOpenContext());

            result.whenComplete((project, t) -> {
                if (project != null) {
                    postOpenFileRequest(project, uiAccess, body);
                }
            });
        }

        return JsonResponse.asSuccess(null);
    }

    private void postOpenFileRequest(@Nullable Project project, @Nonnull UIAccess uiAccess, @Nonnull UnityOpenFilePostHandlerRequest body) {
        uiAccess.give(() -> {
            activateFrame(project, body);

            openFile(project, body);
        });
    }

    private static void activateFrame(@Nullable Project openedProject, @Nonnull UnityOpenFilePostHandlerRequest body) {
        if (openedProject == null) {
            return;
        }

        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(openedProject);
        if (ideFrame == null || !ideFrame.getWindow().isVisible()) {
            return;
        }

        ideFrame.activate();

        PlatformOperatingSystem os = Platform.current().os();
        if (os.isMac()) {
            // something?
        }
        else if (os.isWindows()) {
            Pointer windowPointer = Native.getWindowPointer(TargetAWT.to(ideFrame.getWindow()));
            User32.INSTANCE.SetForegroundWindow(new WinDef.HWND(windowPointer));
        }
    }

    public static void openFile(@Nullable Project openedProject, @Nonnull UnityOpenFilePostHandlerRequest body) {
        if (openedProject == null) {
            return;
        }

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPathIfCached(body.filePath);
        if (fileByPath != null) {
            OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(openedProject)
                .newBuilder(fileByPath)
                .line(body.line - 1)
                .build();

            Editor editor = FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);

            if (editor != null) {
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> editor.getComponent().grabFocus());
            }
        }
    }
}
