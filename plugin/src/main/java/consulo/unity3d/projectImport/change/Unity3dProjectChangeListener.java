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

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.disposer.Disposable;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class Unity3dProjectChangeListener implements Disposable
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
	public Unity3dProjectChangeListener(@Nonnull Project project, @Nonnull StartupManager startupManager, @Nonnull VirtualFileManager virtualFileManager)
	{
		myProject = project;

		if(project.isDefault())
		{
			return;
		}

		for(Unity3dProjectSourceFileTypeFactory factory : Unity3dProjectSourceFileTypeFactory.EP_NAME.getExtensionList())
		{
			factory.registerFileTypes(mySourceFileTypes::add);
		}

		myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener()
		{
			@Override
			@RequiredReadAction
			public void rootsChanged(ModuleRootEvent event)
			{
				checkAndRunIfNeed(Application.get().getLastUIAccess());
			}
		});

		myAssetsDirPointer = VirtualFilePointerManager.getInstance().create(StandardFileSystems.FILE_PROTOCOL_PREFIX + myProject.getPresentableUrl() + "/" + Unity3dProjectImporter
				.ASSETS_DIRECTORY, this, null);

		virtualFileManager.addAsyncFileListener(new AsyncFileListener()
		{
			@Nullable
			@Override
			public ChangeApplier prepareChange(@Nonnull List<? extends VFileEvent> list)
			{
				List<VFileEvent> addOrMove = new ArrayList<>();
				for(VFileEvent vFileEvent : list)
				{
					if(vFileEvent instanceof VFileCreateEvent || vFileEvent instanceof VFileMoveEvent)
					{
						addOrMove.add(vFileEvent);
					}
				}
				if(addOrMove.isEmpty())
				{
					return null;
				}
				return new ChangeApplier()
				{
					@Override
					public void afterVfsChange()
					{
						for(VFileEvent vFileEvent : addOrMove)
						{
							VirtualFile file = vFileEvent.getFile();

							if(file != null)
							{
								handleChange(file);
							}
						}
					}
				};
			}
		}, this);
		startupManager.registerPostStartupActivity(this::checkAndRunIfNeed);
	}

	@RequiredReadAction
	private void checkAndRunIfNeed(UIAccess uiAccess)
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
				Notification notification = new Notification("unity", ApplicationInfo.getInstance().getName(), "Unity project structure changed", NotificationType.INFORMATION);
				notification.addAction(new NotificationAction("Rebuild project")
				{
					@RequiredUIAccess
					@Override
					public void actionPerformed(@Nonnull AnActionEvent anActionEvent, @Nonnull Notification notification)
					{
						final Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
						if(rootModuleExtension == null)
						{
							return;
						}

						Unity3dProjectImporter.syncProjectStep(myProject, rootModuleExtension.getSdk(), null, true);
					}
				});
				notification.notify(myProject);
			}
		});
	}

	private void handleChange(@Nonnull VirtualFile virtualFile)
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
