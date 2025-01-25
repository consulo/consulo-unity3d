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

package consulo.unity3d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.progress.Task;
import consulo.application.util.function.ThrowableComputable;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.library.Library;
import consulo.content.library.LibraryTable;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.language.editor.WriteCommandAction;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableModuleRootLayer;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.startup.BackgroundStartupActivity;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationAction;
import consulo.project.ui.notification.NotificationType;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dManifest;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.SmartList;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.ThrowableConsumer;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 26-Jul-16
 */
@ExtensionImpl
public class UnityPluginValidator implements BackgroundStartupActivity {
    private static final Logger LOG = Logger.getInstance(UnityPluginValidator.class);

    public static final String PLUGIN_ID = "com.consulo.ide";
    public static final String PLUGIN_LINK = "https://github.com/consulo/UnityEditorConsuloPlugin.git#2.6.0";

    private static final String ourPath = "Assets/Editor/Plugins";

    @Override
    public void runActivity(@Nonnull Project project, @Nonnull UIAccess uiAccess) {
        uiAccess.give(() -> notifyAboutPluginFile(project));
    }

    public static void runValidation(@Nonnull final Project project) {
        DumbService.getInstance(project).runWhenSmart(() -> notifyAboutPluginFile(project));
    }

    @RequiredReadAction
    private static void notifyAboutPluginFile(@Nonnull final Project project) {
        Unity3dRootModuleExtension moduleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
        if (moduleExtension == null) {
            return;
        }

        Unity3dManifest manifest = Unity3dManifest.parse(project);
        // no files - not supported
        if (manifest == Unity3dManifest.EMPTY) {
            return;
        }

        String ver = manifest.dependencies.get(PLUGIN_ID);

        // same version
        if (PLUGIN_LINK.equals(ver)) {
            return;
        }

        if (ver == null) {
            showNotify(project, "Consulo plugin for UnityEditor is missing", "Install via manifest", false);
        }
        else {
            showNotify(project, "Outdated Consulo plugin for UnityEditor", "Update via manifest", true);
        }
    }

    private static void showNotify(final Project project, @Nonnull String text, @Nonnull String actionName, boolean update) {
        Notification notification = new Notification(UnityNotificationGroup.INSTANCE, "Unity3D Plugin", text, update ? NotificationType.WARNING : NotificationType.INFORMATION);
        notification.addAction(new NotificationAction(actionName) {
            @RequiredUIAccess
            @Override
            public void actionPerformed(@Nonnull AnActionEvent anActionEvent, @Nonnull Notification notification) {
                notification.expire();
                updatePlugin(project);

            }
        });
        notification.notify(project);
    }

    private static void updatePlugin(@Nonnull final Project project) {
        Task.Backgroundable.queue(project, "Changing manifest.json", (progressIndicator) ->
        {
            // drop old libraries
            modifyModules(project, modifiableModel ->
            {
                for (ModuleRootLayer layer : modifiableModel.getLayers().values()) {
                    LibraryTable moduleLibraryTable = ((ModifiableModuleRootLayer) layer).getModuleLibraryTable();
                    for (Library library : moduleLibraryTable.getLibraries()) {
                        String[] files = library.getUrls(BinariesOrderRootType.getInstance());
                        for (String url : files) {
                            String localPath = PathUtil.getFileName(url);
                            if (StringUtil.startsWith(localPath, "UnityEditorConsuloPlugin")) {
                                moduleLibraryTable.removeLibrary(library);
                                break;
                            }
                        }
                    }
                }
            });

            List<VirtualFile> oldPluginFiles = new SmartList<>();

            VirtualFile fileByRelativePath = project.getBaseDir().findFileByRelativePath(ourPath);
            if (fileByRelativePath != null) {
                VirtualFile[] children = fileByRelativePath.getChildren();
                for (VirtualFile child : children) {
                    CharSequence nameSequence = child.getNameSequence();
                    if (StringUtil.startsWith(nameSequence, "UnityEditorConsuloPlugin") && child.getFileType() == DotNetModuleFileType.INSTANCE) {
                        oldPluginFiles.add(child);
                    }
                }
            }

            // drop old plugins
            for (VirtualFile oldPluginFile : oldPluginFiles) {
                try {
                    WriteCommandAction.runWriteCommandAction(project, (ThrowableComputable<Object, Throwable>) () ->
                    {
                        oldPluginFile.delete(null);

                        doActionOnSuffixFile(oldPluginFile, virtualFile -> virtualFile.delete(null), ".mdb");
                        return null;
                    });
                }
                catch (Throwable e) {
                    LOG.error(e);
                    return;
                }
            }

            Unity3dManifest manifest = Unity3dManifest.parse(project);

            Unity3dManifest newManifest = manifest.clone();

            Unity3dManifest.ScopeRegistry[] scopedRegistries = newManifest.scopedRegistries;
            if (scopedRegistries != null) {
                for (Unity3dManifest.ScopeRegistry registry : List.of(scopedRegistries)) {
                    if ("https://upm.consulo.io/".equals(registry.url)) {
                        scopedRegistries = ArrayUtil.remove(scopedRegistries, registry);
                    }
                }

                if (scopedRegistries.length == 0) {
                    newManifest.scopedRegistries = null;
                }
                else {
                    newManifest.scopedRegistries = scopedRegistries;
                }
            }

            newManifest.dependencies.put(PLUGIN_ID, PLUGIN_LINK);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            WriteCommandAction.runWriteCommandAction(project, () -> Unity3dManifest.write(project, gson.toJson(newManifest)));
        });
    }

    public static boolean doActionOnSuffixFile(VirtualFile parentFile, ThrowableConsumer<VirtualFile, IOException> consumer, String suffix) {
        VirtualFile parent = parentFile.getParent();
        if (parent == null) {
            return false;
        }

        VirtualFile metaFile = parent.findChild(parentFile.getName() + suffix);
        if (metaFile != null) {
            try {
                consumer.consume(metaFile);
            }
            catch (IOException e) {
                LOG.error(e);
            }
        }
        return false;
    }

    private static void modifyModules(Project project, Consumer<ModifiableRootModel> action) {
        List<ModifiableRootModel> list = new ArrayList<>();
        ReadAction.run(() -> {
            Unity3dRootModuleExtension unity3dRootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);

            if (unity3dRootModuleExtension == null) {
                return;
            }

            ModuleManager moduleManager = ModuleManager.getInstance(project);

            Module[] modules = moduleManager.getModules();
            for (Module module : modules) {
                String name = module.getName();
                if (name.startsWith("Assembly") && name.endsWith("Editor")) {
                    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                    ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();

                    action.accept(modifiableModel);

                    list.add(modifiableModel);
                }
            }
        });

        WriteCommandAction.runWriteCommandAction(project, () ->
        {
            for (ModifiableRootModel modifiableRootModel : list) {
                modifiableRootModel.commit();
            }
        });
    }
}
