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

package consulo.unity3d.ide.ui;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.WriteAction;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkUtil;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.EditorNotifications;
import consulo.fileEditor.FileEditor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnSeparator;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.ui.Unity3dWizardStep;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 29.07.2015
 */
@ExtensionImpl
public class SetupUnitySDKProvider implements EditorNotificationProvider {
    private final Project myProject;
    private final EditorNotifications myNotifications;
    private final SdkTable mySdkTable;

    @Inject
    public SetupUnitySDKProvider(Project project, EditorNotifications notifications, SdkTable sdkTable) {
        myProject = project;
        myNotifications = notifications;
        mySdkTable = sdkTable;
    }

    @Nonnull
    @Override
    public String getId() {
        return "unity-setup-sdk";
    }

    @RequiredReadAction
    @Nullable
    @Override
    public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> supplier) {
        if (myProject.getUserData(Unity3dProjectImporter.ourInProgressFlag) == Boolean.TRUE) {
            return null;
        }

        final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
        if (psiFile == null) {
            return null;
        }

        Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
        if (rootModuleExtension == null) {
            return null;
        }

        Sdk sdk = rootModuleExtension.getSdk();
        if (!isValidSdk(sdk)) {
            if (rootModuleExtension.getInheritableSdk().isNull()) {
                return createPanel(null, null, supplier);
            }
            return createPanel(rootModuleExtension.getInheritableSdk().getName(), rootModuleExtension.getInheritableSdk().get(), supplier);
        }
        return null;
    }

    private boolean isValidSdk(@Nullable Sdk sdk) {
        if (sdk == null) {
            return false;
        }

        return sdk.getHomeDirectory() != null;
    }

    @Nonnull
    private EditorNotificationBuilder createPanel(@Nullable String name, @Nullable Sdk targetSdk, Supplier<EditorNotificationBuilder> supplier) {
        EditorNotificationBuilder panel = supplier.get();
        String requiredVersion = Unity3dProjectImporter.loadVersionFromProject(myProject.getBasePath());

        if (requiredVersion != null) {
            if (targetSdk != null) {
                panel.withText(Unity3dLocalize.unity0SdkIsBrokenRequiredVersion(targetSdk.getName(), requiredVersion));
            }
            else if (name == null) {
                panel.withText(Unity3dLocalize.unitySdkIsNotDefinedRequiredVersion(requiredVersion));
            }
            else {
                panel.withText(Unity3dLocalize.unity0SdkIsNotDefinedRequiredVersion(name, requiredVersion));
            }
        }
        else {
            if (targetSdk != null) {
                panel.withText(Unity3dLocalize.unity0SdkIsBroken(targetSdk.getName()));
            }
            else if (name == null) {
                panel.withText(Unity3dLocalize.unitySdkIsNotDefined());
            }
            else {
                panel.withText(Unity3dLocalize.unity0SdkIsNotDefined(name));
            }
        }

        boolean[] requiredVersionFound = new boolean[1];
        if (requiredVersion != null) {
            Optional<Sdk> requiredSdk = mySdkTable.getSdksOfType(Unity3dBundleType.getInstance()).stream().filter(sdk -> Objects.equals(sdk.getVersionString(), requiredVersion)).findAny();

            requiredSdk.ifPresent(sdk ->
            {
                requiredVersionFound[0] = true;

                panel.withAction(LocalizeValue.localizeTODO("Select '" + sdk.getName() + "'"), (e) ->
                {
                    Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

                    myNotifications.updateAllNotifications();
                });
            });
        }

        panel.withAction(LocalizeValue.localizeTODO(requiredVersionFound[0] ? "Select Another Unity..." : "Select Unity..."), (e) ->
        {
            final DataContext dataContext = DataManager.getInstance().getDataContext();
            ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
            List<Sdk> sdksOfType = mySdkTable.getSdksOfType(Unity3dBundleType.getInstance());
            for (Sdk sdk : sdksOfType) {
                builder.add(new DumbAwareAction(LocalizeValue.of(sdk.getName()), LocalizeValue.empty(), SdkUtil.getIcon(sdk)) {
                    @RequiredUIAccess
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
                        Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

                        myNotifications.updateAllNotifications();
                    }
                });
            }

            builder.add(AnSeparator.create());
            builder.add(new DumbAwareAction("From File System...", null, PlatformIconGroup.nodesFolderopened()) {
                @RequiredUIAccess
                @Override
                public void actionPerformed(@Nonnull AnActionEvent e) {
                    Unity3dWizardStep.showAddSdk(sdk ->
                    {
                        WriteAction.run(() -> mySdkTable.addSdk(sdk));

                        Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

                        myNotifications.updateAllNotifications();
                    });
                }
            });

            JBPopupFactory.getInstance().createActionGroupPopup(requiredVersionFound[0] ? "Select Another Unity" : "Select Unity", builder.build(), dataContext,
                    JBPopupFactory.ActionSelectionAid.MNEMONICS, false)
                .showBy(e);
        });
        return panel;
    }
}
