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

package consulo.unity3d.projectImport;

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author VISTALL
 * @since 1/11/18
 */
public class Unity3dProjectChangeListener implements ProjectComponent, VirtualFileListener, Disposable
{
	private final Project myProject;
	private VirtualFilePointer myAssetsDirPointer;

	@NotNull
	private Future<?> myUpdateCheckaTask = CompletableFuture.completedFuture(null);

	private final AtomicBoolean myNeedShowNotification = new AtomicBoolean();
	private final AtomicBoolean myActive = new AtomicBoolean();

	public Unity3dProjectChangeListener(@NotNull Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredReadAction
	public void initComponent()
	{
		myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener()
		{
			@Override
			@RequiredReadAction
			public void rootsChanged(ModuleRootEvent event)
			{
				checkAndRunIfNeed();
			}
		});

		VirtualFileManager.getInstance().addVirtualFileListener(this, this);

		myAssetsDirPointer = VirtualFilePointerManager.getInstance().create(StandardFileSystems.FILE_PROTOCOL_PREFIX + myProject.getPresentableUrl() + "/" + Unity3dProjectImportUtil.ASSETS_DIRECTORY, this, null);

		checkAndRunIfNeed();
	}

	@RequiredReadAction
	private void checkAndRunIfNeed()
	{
		myUpdateCheckaTask.cancel(false);
		myUpdateCheckaTask = CompletableFuture.completedFuture(null);

		myActive.set(Unity3dModuleExtensionUtil.getRootModuleExtension(myProject) != null);

		if(!myActive.get())
		{
			return;
		}

		myUpdateCheckaTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) this::checkNotification, 10, 10, TimeUnit.SECONDS);
	}

	private void checkNotification()
	{
		if(!myNeedShowNotification.get() || DumbService.isDumb(myProject))
		{
			return;
		}

		myNeedShowNotification.set(false);

		UIUtil.invokeLaterIfNeeded(() -> new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "Project structure changed<br><a href=\"#\">Rebuild project</a>", NotificationType
				.WARNING, (notification, hyperlinkEvent) ->
		{
			final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
			if(rootModuleExtension == null)
			{
				return;
			}

			Unity3dProjectImportUtil.syncProjectStep1(myProject, rootModuleExtension.getSdk(), null, true);
		}).notify(myProject));
	}

	@Override
	public void fileCreated(@NotNull VirtualFileEvent event)
	{
		handleChange(event.getFile());
	}

	@Override
	public void fileMoved(@NotNull VirtualFileMoveEvent event)
	{
		handleChange(event.getFile());
	}

	private void handleChange(VirtualFile virtualFile)
	{
		if(myNeedShowNotification.get() || !myActive.get())
		{
			return;
		}

		String url = virtualFile.getUrl();
		if(url.startsWith(myAssetsDirPointer.getUrl()))
		{
			myNeedShowNotification.set(true);
		}
	}

	@Override
	public void dispose()
	{
		myUpdateCheckaTask.cancel(false);
	}
}
