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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImportUtil;

/**
 * @author VISTALL
 * @since 1/11/18
 */
@Singleton
public class Unity3dProjectChangeListener implements VirtualFileListener, Disposable
{
	public static class DataBlock
	{
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
	public Unity3dProjectChangeListener(@Nonnull Project project, @Nonnull StartupManager startupManager)
	{
		myProject = project;

		if(project.isDefault())
		{
			return;
		}

		for(Unity3dProjectSourceFileTypeFactory factory : Unity3dProjectSourceFileTypeFactory.EP_NAME.getExtensions())
		{
			factory.registerFileTypes(mySourceFileTypes::add);
		}

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

		myAssetsDirPointer = VirtualFilePointerManager.getInstance().create(StandardFileSystems.FILE_PROTOCOL_PREFIX + myProject.getPresentableUrl() + "/" + Unity3dProjectImportUtil
				.ASSETS_DIRECTORY, this, null);

		startupManager.registerPostStartupActivity(this::checkAndRunIfNeed);
	}

	@RequiredReadAction
	private void checkAndRunIfNeed()
	{
		myUpdateCheckTask.cancel(false);
		myUpdateCheckTask = CompletableFuture.completedFuture(null);

		myActive.set(Unity3dModuleExtensionUtil.getRootModuleExtension(myProject) != null);

		if(!myActive.get())
		{
			return;
		}

		myUpdateCheckTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) this::checkNotification, 10, 10, TimeUnit.SECONDS);
	}

	private void checkNotification()
	{
		if(DumbService.isDumb(myProject))
		{
			return;
		}

		DataBlock dataBlock = myDataBlock;

		myDataBlock = new DataBlock();

		if(dataBlock.myFiles.isEmpty())
		{
			return;
		}

		UIUtil.invokeLaterIfNeeded(() ->
		{
			boolean needNotification = false;

			for(VirtualFile file : dataBlock.myFiles)
			{
				Module module = ModuleUtilCore.findModuleForFile(file, myProject);
				if(module == null)
				{
					continue;
				}

				if(module == Unity3dModuleExtensionUtil.getRootModule(myProject))
				{
					needNotification = true;
					break;
				}
			}

			if(needNotification)
			{
				new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "Project structure changed<br><a href=\"#\">Rebuild project</a>", NotificationType.INFORMATION,
						(notification, hyperlinkEvent) ->
				{
					notification.hideBalloon();

					final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
					if(rootModuleExtension == null)
					{
						return;
					}

					Unity3dProjectImportUtil.syncProjectStep1(myProject, rootModuleExtension.getSdk(), null, true);
				}).notify(myProject);
			}
		});
	}

	@Override
	public void fileCreated(@Nonnull VirtualFileEvent event)
	{
		handleChange(event.getFile());
	}

	@Override
	public void fileMoved(@Nonnull VirtualFileMoveEvent event)
	{
		handleChange(event.getFile());
	}

	@RequiredReadAction
	private void handleChange(VirtualFile virtualFile)
	{
		if(!myActive.get())
		{
			return;
		}

		if(!mySourceFileTypes.contains(virtualFile.getFileType()))
		{
			return;
		}

		String url = virtualFile.getUrl();
		if(url.startsWith(myAssetsDirPointer.getUrl()))
		{
			myDataBlock.myFiles.add(virtualFile);
		}
	}

	@Override
	public void dispose()
	{
		myUpdateCheckTask.cancel(false);
	}
}
