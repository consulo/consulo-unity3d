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

import com.intellij.ProjectTopics;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnSeparator;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.HyperlinkLabel;
import consulo.annotation.access.RequiredReadAction;
import consulo.bundle.SdkUtil;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtension;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.ui.Unity3dWizardStep;
import consulo.util.lang.ref.SimpleReference;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author VISTALL
 * @since 29.07.2015
 */
public class SetupUnitySDKProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	private final Project myProject;
	private final EditorNotifications myNotifications;
	private final SdkTable mySdkTable;

	@Inject
	public SetupUnitySDKProvider(Project project, EditorNotifications notifications, SdkTable sdkTable)
	{
		myProject = project;
		myNotifications = notifications;
		mySdkTable = sdkTable;
		myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener()
		{
			@Override
			public void rootsChanged(ModuleRootEvent event)
			{
				notifications.updateAllNotifications();
			}
		});
		myProject.getMessageBus().connect().subscribe(ModuleExtension.CHANGE_TOPIC, (oldExtension, newExtension) -> notifications.updateAllNotifications());
	}

	@Override
	@RequiredReadAction
	public EditorNotificationPanel createNotificationPanel(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor)
	{
		if(myProject.getUserData(Unity3dProjectImporter.ourInProgressFlag) == Boolean.TRUE)
		{
			return null;
		}

		final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
		if(psiFile == null)
		{
			return null;
		}

		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
		if(rootModuleExtension == null)
		{
			return null;
		}

		Sdk sdk = rootModuleExtension.getSdk();
		if(!isValidSdk(sdk))
		{
			if(rootModuleExtension.getInheritableSdk().isNull())
			{
				return createPanel(null, null);
			}
			return createPanel(rootModuleExtension.getInheritableSdk().getName(), rootModuleExtension.getInheritableSdk().get());
		}
		return null;
	}

	private boolean isValidSdk(@Nullable Sdk sdk)
	{
		if(sdk == null)
		{
			return false;
		}

		return sdk.getHomeDirectory() != null;
	}

	@Nonnull
	private EditorNotificationPanel createPanel(@Nullable String name, @Nullable Sdk targetSdk)
	{
		EditorNotificationPanel panel = new EditorNotificationPanel();
		String requiredVersion = Unity3dProjectImporter.loadVersionFromProject(myProject.getBasePath());

		if(requiredVersion != null)
		{
			if(targetSdk != null)
			{
				panel.setText(Unity3dLocalize.unity0SdkIsBrokenRequiredVersion(targetSdk.getName(), requiredVersion).getValue());
			}
			else if(name == null)
			{
				panel.setText(Unity3dLocalize.unitySdkIsNotDefinedRequiredVersion(requiredVersion).getValue());
			}
			else
			{
				panel.setText(Unity3dLocalize.unity0SdkIsNotDefinedRequiredVersion(name, requiredVersion).getValue());
			}
		}
		else
		{
			if(targetSdk != null)
			{
				panel.setText(Unity3dLocalize.unity0SdkIsBroken(targetSdk.getName()).getValue());
			}
			else if(name == null)
			{
				panel.setText(Unity3dLocalize.unitySdkIsNotDefined().getValue());
			}
			else
			{
				panel.setText(Unity3dLocalize.unity0SdkIsNotDefined(name).getValue());
			}
		}

		boolean[] requiredVersionFound = new boolean[1];
		if(requiredVersion != null)
		{
			Optional<Sdk> requiredSdk = mySdkTable.getSdksOfType(Unity3dBundleType.getInstance()).stream().filter(sdk -> Objects.equals(sdk.getVersionString(), requiredVersion)).findAny();

			requiredSdk.ifPresent(sdk ->
			{
				requiredVersionFound[0] = true;

				panel.createActionLabel("Select '" + sdk.getName() + "'", () ->
				{
					Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

					myNotifications.updateAllNotifications();
				});
			});
		}

		SimpleReference<HyperlinkLabel> ref = SimpleReference.create();
		Runnable action = () -> {
			final DataContext dataContext = DataManager.getInstance().getDataContext();
			ActionGroup.Builder builder = ActionGroup.newImmutableBuilder();
			List<Sdk> sdksOfType = mySdkTable.getSdksOfType(Unity3dBundleType.getInstance());
			for(Sdk sdk : sdksOfType)
			{
				builder.add(new DumbAwareAction(LocalizeValue.of(sdk.getName()), LocalizeValue.empty(), SdkUtil.getIcon(sdk))
				{
					@RequiredUIAccess
					@Override
					public void actionPerformed(@Nonnull AnActionEvent anActionEvent)
					{
						Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

						myNotifications.updateAllNotifications();
					}
				});
			}

			builder.add(AnSeparator.create());
			builder.add(new DumbAwareAction("From File System...", null, PlatformIconGroup.nodesFolderOpened())
			{
				@RequiredUIAccess
				@Override
				public void actionPerformed(@Nonnull AnActionEvent e)
				{
					Unity3dWizardStep.showAddSdk(ref.get(), sdk -> {
						WriteAction.run(() -> mySdkTable.addSdk(sdk));

						Unity3dProjectImporter.syncProjectStep(myProject, sdk, null, true);

						myNotifications.updateAllNotifications();
					});
				}
			});

			JBPopupFactory.getInstance().createActionGroupPopup(requiredVersionFound[0] ? "Select Another Unity" : "Select Unity", builder.build(), dataContext,
					JBPopupFactory.ActionSelectionAid.MNEMONICS, false)
					.showUnderneathOf(ref.get());
		};
		HyperlinkLabel actionLabel = panel.createActionLabel(requiredVersionFound[0] ? "Select Another Unity..." : "Select Unity...", action);
		ref.set(actionLabel);
		return panel;
	}
}
