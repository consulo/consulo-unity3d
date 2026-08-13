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
import consulo.application.concurrent.coroutine.WriteLock;
import consulo.application.ui.wm.IdeFocusManager;
import consulo.builtinWebServer.json.JsonPostRequestHandler;
import consulo.codeEditor.Editor;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkUtil;
import consulo.disposer.Disposer;
import consulo.fileEditor.FileEditorManager;
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
import consulo.ui.UIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dModuleImportProvider;
import consulo.util.concurrent.AsyncResult;
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

    @Inject
    public UnityOpenFilePostHandler(Application application) {
        super("unityOpenFile", UnityOpenFilePostHandlerRequest.class);
        myApplication = application;
    }

    @Nonnull
    @Override
    public JsonResponse handle(@Nonnull final UnityOpenFilePostHandlerRequest body) {
        String contentType = body.contentType;

        if (!ourSupportedContentTypes.contains(contentType)) {
            return JsonResponse.asError("unsupported-content-type");
        }

        UIUtil.invokeLaterIfNeeded(() ->
        {
            UIAccess uiAccess = UIAccess.current();

            VirtualFile projectVirtualFile = LocalFileSystem.getInstance().findFileByPath(body.projectPath);
            if (projectVirtualFile != null) {
                Project targetProject = null;
                Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                for (Project openProject : openProjects) {
                    if (ProjectUtil.isSameProject(body.projectPath, openProject)) {
                        targetProject = openProject;
                        break;
                    }
                }

                if (targetProject == null) {
                    if (!new File(projectVirtualFile.getPath(), Project.DIRECTORY_STORE_FOLDER).exists()) {
                        String sdkPath = Platform.current().os().isMac() ? body.editorPath : new File(body.editorPath).getParentFile().getParentFile().getPath();

                        VirtualFile sdkFileHome = LocalFileSystem.getInstance().findFileByPath(sdkPath);
                        if (sdkFileHome == null) {
                            IdeFrame frame = WindowManager.getInstance().findVisibleIdeFrame();
                            if (frame != null) {
                                frame.activate();
                            }
                            Messages.showErrorDialog("Unity path is not resolved: " + sdkPath, "Consulo");
                            return;
                        }

                        Sdk targetSdk = null;
                        List<Sdk> sdksOfType = SdkTable.getInstance().getSdksOfType(Unity3dBundleType.getInstance());
                        for (Sdk sdk : sdksOfType) {
                            VirtualFile homeDirectory = sdk.getHomeDirectory();
                            if (sdkFileHome.equals(homeDirectory)) {
                                targetSdk = sdk;
                                break;
                            }
                        }

                        if (targetSdk == null) {
                            targetSdk = SdkUtil.createAndAddSDK(sdkPath, Unity3dBundleType.getInstance(), uiAccess);
                        }

                        if (targetSdk == null) {
                            IdeFrame frame = WindowManager.getInstance().findVisibleIdeFrame();
                            if (frame != null) {
                                frame.activate();
                            }
                            Messages.showErrorDialog("Unity SDK cant add by path: " + sdkPath, "Consulo");
                            return;
                        }


                        Unity3dModuleImportProvider importProvider = new Unity3dModuleImportProvider(targetSdk, body);

                        CompletableFuture<Pair<ModuleImportContext, ModuleImportProvider<ModuleImportContext>>> result = new CompletableFuture<>();

                        myApplication.getInstance(ModuleCreationHelper.class)
                            .showImportChooser(null, projectVirtualFile, Collections.singletonList(importProvider), result);

                        result.whenComplete((pair, e) -> {
                            if (pair == null) {
                                return;
                            }

                            ModuleImportContext context = pair.getFirst();

                            ModuleImportProvider<ModuleImportContext> provider = pair.getSecond();

                            Coroutine<Void, Project> importProjectAsync = NewOrImportModuleUtil.importProject(context, provider, uiAccess);

                            CoroutineScope scope = CoroutineScope.of(myApplication.coroutineContext());
                            scope.putCopyableUserData(UIAccess.KEY, uiAccess);

                            importProjectAsync
                                .then(WriteLock.apply((project, continuation) -> {
                                    String basePath = project.getBasePath();
                                    Disposer.dispose(project);
                                    return basePath;
                                }))
                                .then(CompletableFutureStep.await(path -> {
                                    return ProjectManager.getInstance().openProjectAsync(Path.of(path), uiAccess, new ProjectOpenContext());
                                }))
                                .then(CodeExecution.consume((project, continuation) -> {
                                    if (project != null) {
                                        postOpenFileRequest(project, uiAccess, body);
                                    }
                                }));

                            importProjectAsync.runAsync(scope, null);
                        });
                    }
                    else {
                        CompletableFuture<Project> result = ProjectManager.getInstance()
                            .openProjectAsync(projectVirtualFile.toNioPath(), uiAccess, new ProjectOpenContext());

                        result.whenComplete((project, t) -> {
                            if (project != null) {
                                postOpenFileRequest(project, uiAccess, body);
                            }
                        });
                    }
                }
                else {
                    final Project project = targetProject;
                    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> postOpenFileRequest(project, uiAccess, body));
                }
            }
        });
        return JsonResponse.asSuccess(null);
    }

    private void postOpenFileRequest(@Nullable Project project, @Nonnull UIAccess uiAccess, @Nonnull UnityOpenFilePostHandlerRequest body) {
        uiAccess.give(() ->
        {
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

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(body.filePath);
        if (fileByPath != null) {
            OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(openedProject).builder(fileByPath).line(body.line - 1).build();
            Editor editor = FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);

            if (editor != null) {
                IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> editor.getComponent().grabFocus());
            }
        }
    }
}
