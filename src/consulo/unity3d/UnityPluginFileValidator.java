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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.ModuleRootLayer;
import consulo.roots.types.BinariesOrderRootType;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectUtil;
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 26-Jul-16
 */
public class UnityPluginFileValidator extends AbstractProjectComponent
{
	private static final Logger LOGGER = Logger.getInstance(UnityPluginFileValidator.class);

	private static final String ourPath = "Assets/Editor/Plugins";
	private static final NotificationGroup ourGroup = new NotificationGroup("consulo.unity", NotificationDisplayType.STICKY_BALLOON, true);

	public UnityPluginFileValidator(Project project)
	{
		super(project);
	}

	@Override
	public void projectOpened()
	{
		runValidation(myProject);
	}

	public static void runValidation(@NotNull final Project project)
	{
		DumbService.getInstance(project).runWhenSmart(new Runnable()
		{
			@Override
			@RequiredReadAction
			public void run()
			{
				notifyAboutPluginFile(project);
			}
		});
	}

	@RequiredReadAction
	private static void notifyAboutPluginFile(@NotNull final Project project)
	{
		Unity3dRootModuleExtension moduleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(moduleExtension == null)
		{
			return;
		}

		Unity3dDefineByVersion unity3dDefineByVersion = Unity3dProjectUtil.getUnity3dDefineByVersion(moduleExtension.getSdk());
		final String pluginFileName = unity3dDefineByVersion.getPluginFileName();
		if(pluginFileName == null)
		{
			return;
		}

		File pluginPath = PluginManager.getPluginPath(UnityPluginFileValidator.class);

		final File unityPluginFile = new File(pluginPath, "UnityEditorConsuloPlugin/" + pluginFileName);
		if(!unityPluginFile.exists())
		{
			return;
		}

		VirtualFile baseDir = project.getBaseDir();
		if(baseDir == null)
		{
			return;
		}

		VirtualFile targetFile = null;

		VirtualFile fileByRelativePath = baseDir.findFileByRelativePath(ourPath);
		if(fileByRelativePath != null)
		{
			VirtualFile[] children = fileByRelativePath.getChildren();
			for(VirtualFile child : children)
			{
				CharSequence nameSequence = child.getNameSequence();
				if(StringUtil.startsWith(nameSequence, "UnityEditorConsuloPlugin") && child.getFileType() == DotNetModuleFileType.INSTANCE)
				{
					targetFile = child;
					break;
				}
			}
		}

		if(targetFile == null)
		{
			showNotify(project, pluginFileName, unityPluginFile, "Consulo plugin for UnityEditor is missing<br><a href=\"#\">Install</a>", null);
		}
		else
		{
			final File file = VfsUtil.virtualToIoFile(targetFile);
			if(!file.exists())
			{
				return;
			}

			String title = null;
			if(!pluginFileName.equals(targetFile.getName()))
			{
				title = "Outdated Consulo plugin for UnityEditor<br><a href=\"#\">Update</a>";
			}
			else if(file.lastModified() < unityPluginFile.lastModified())
			{
				title = "New version of Consulo plugin for UnityEditor is available<br><a href=\"#\">Update</a>";
			}

			if(title != null)
			{
				final VirtualFile finalTargetFile = targetFile;
				showNotify(project, pluginFileName, unityPluginFile, title, new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							finalTargetFile.delete(null);
						}
						catch(IOException e)
						{
							//
						}
					}
				});
			}
		}
	}

	private static void showNotify(final Project project, final String pluginFileName, final File unityPluginFile, @NotNull String title, @Nullable final Runnable beforeTask)
	{
		Notification notification = new Notification(ourGroup.getDisplayId(), "Unity3D Plugin", title, NotificationType.INFORMATION);
		notification.setListener(new NotificationListener()
		{
			@Override
			public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent)
			{
				notification.hideBalloon();

				runTask(project, pluginFileName, unityPluginFile, beforeTask);
			}
		});
		notification.notify(project);
	}

	private static void runTask(@NotNull final Project project, @NotNull final String pluginFileName, @NotNull final File unityPluginFile, @Nullable final Runnable otherTask)
	{
		new Task.Backgroundable(project, "Installing plugin", false)
		{
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator)
			{
				if(otherTask != null)
				{
					WriteCommandAction.runWriteCommandAction(project, otherTask);
				}

				final Ref<VirtualFile> fileRef = Ref.create();
				File targetFile = new File(project.getBasePath(), ourPath + "/" + pluginFileName);
				try
				{
					FileUtil.copy(unityPluginFile, targetFile);

					VirtualFile value = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile);
					VirtualFile archiveRootForLocalFile = value == null ? null : ArchiveVfsUtil.getArchiveRootForLocalFile(value);
					fileRef.set(archiveRootForLocalFile);
				}
				catch(IOException e)
				{
					LOGGER.error(e);
				}

				// if target file is not found, no need change module roots
				final VirtualFile targetArchiveFile = fileRef.get();
				if(targetArchiveFile == null)
				{
					return;
				}

				final List<ModifiableRootModel> list = new ArrayList<ModifiableRootModel>(2);
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					@Override
					public void run()
					{
						Unity3dRootModuleExtension unity3dRootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);

						if(unity3dRootModuleExtension == null)
						{
							return;
						}

						ModuleManager moduleManager = ModuleManager.getInstance(project);

						Module[] modules = moduleManager.getModules();
						for(Module module : modules)
						{
							String name = module.getName();
							if(name.startsWith("Assembly") && name.endsWith("Editor"))
							{
								ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
								ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();
								for(ModuleRootLayer layer : modifiableModel.getLayers().values())
								{
									LibraryTable moduleLibraryTable = ((ModifiableModuleRootLayer) layer).getModuleLibraryTable();
									for(Library library : moduleLibraryTable.getLibraries())
									{
										String[] files = library.getUrls(BinariesOrderRootType.getInstance());
										for(String url : files)
										{
											String localPath = PathUtil.getFileName(url);
											if(StringUtil.startsWith(localPath, "UnityEditorConsuloPlugin"))
											{
												moduleLibraryTable.removeLibrary(library);
												break;
											}
										}
									}

									Library library = moduleLibraryTable.createLibrary();
									Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
									libraryModifiableModel.addRoot(targetArchiveFile, BinariesOrderRootType.getInstance());
									libraryModifiableModel.commit();
								}

								list.add(modifiableModel);
							}
						}
					}
				});

				WriteCommandAction.runWriteCommandAction(project, new Runnable()
				{
					@Override
					@RequiredWriteAction
					public void run()
					{
						for(ModifiableRootModel modifiableRootModel : list)
						{
							modifiableRootModel.commit();
						}
					}
				});
			}
		}.queue();
	}
}
