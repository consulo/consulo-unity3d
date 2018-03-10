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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.ModuleRootLayer;
import consulo.roots.types.BinariesOrderRootType;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.projectImport.Unity3dProjectImportUtil;
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

	public static void runValidation(@Nonnull final Project project)
	{
		DumbService.getInstance(project).runWhenSmart(() -> notifyAboutPluginFile(project));
	}

	@RequiredReadAction
	private static void notifyAboutPluginFile(@Nonnull final Project project)
	{
		Unity3dRootModuleExtension moduleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(moduleExtension == null)
		{
			return;
		}

		Unity3dDefineByVersion unity3dDefineByVersion = Unity3dProjectImportUtil.getUnity3dDefineByVersion(moduleExtension.getSdk());
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

		List<VirtualFile> targetFiles = new SmartList<>();

		VirtualFile fileByRelativePath = baseDir.findFileByRelativePath(ourPath);
		if(fileByRelativePath != null)
		{
			VirtualFile[] children = fileByRelativePath.getChildren();
			for(VirtualFile child : children)
			{
				CharSequence nameSequence = child.getNameSequence();
				if(StringUtil.startsWith(nameSequence, "UnityEditorConsuloPlugin") && child.getFileType() == DotNetModuleFileType.INSTANCE)
				{
					targetFiles.add(child);
				}
			}
		}

		if(targetFiles.isEmpty())
		{
			showNotify(project, pluginFileName, unityPluginFile, "Consulo plugin for UnityEditor is missing<br><a href=\"update\">Install</a>", Collections.emptyList());
		}
		else
		{
			VirtualFile firstItem = targetFiles.size() == 1 ? targetFiles.get(0) : null;
			if(firstItem != null && VfsUtilCore.virtualToIoFile(firstItem).lastModified() == unityPluginFile.lastModified())
			{
				return;
			}

			String title = "Outdated Consulo plugin(s) for UnityEditor can create <a href=\"info\">issues</a>. <a href=\"update\">Update</a> are recommended";

			showNotify(project, pluginFileName, unityPluginFile, title, targetFiles);
		}
	}

	private static void showNotify(final Project project, final String pluginFileName, final File unityPluginFile, @Nonnull String title, @Nonnull List<VirtualFile> oldPluginFiles)
	{
		Notification notification = new Notification(ourGroup.getDisplayId(), "Unity3D Plugin", title, !oldPluginFiles.isEmpty() ? NotificationType.ERROR : NotificationType.INFORMATION);
		notification.setListener((thisNotification, hyperlinkEvent) ->
		{
			thisNotification.hideBalloon();

			switch(hyperlinkEvent.getDescription())
			{
				case "info":
					BrowserUtil.browse("https://github.com/consulo/consulo/issues/250");
					break;
				case "update":
					updatePlugin(project, pluginFileName, unityPluginFile, oldPluginFiles);
					break;
			}
		});
		notification.notify(project);
	}

	private static void updatePlugin(@Nonnull final Project project, @Nonnull final String pluginFileName, @Nonnull final File unityPluginFile, @Nonnull List<VirtualFile> oldPluginFiles)
	{
		Task.Backgroundable.queue(project, "Installing plugin", (progressIndicator) ->
		{
			// drop old libraries
			modifyModules(project, modifiableModel ->
			{
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
				}
			});

			// drop old plugins
			for(VirtualFile oldPluginFile : oldPluginFiles)
			{
				try
				{
					WriteCommandAction.runWriteCommandAction(project, (ThrowableComputable<Object, Throwable>) () ->
					{
						oldPluginFile.delete(null);

						Unity3dLocalFileSystemComponent.doActionOnSuffixFile(oldPluginFile, virtualFile -> virtualFile.delete(null), ".mdb");
						return null;
					});
				}
				catch(Throwable e)
				{
					LOGGER.error(e);
					return;
				}
			}

			VirtualFile targetArchiveFile;
			File targetFile = new File(project.getBasePath(), ourPath + "/" + pluginFileName);
			try
			{
				targetFile.setLastModified(unityPluginFile.lastModified());
				FileUtil.copy(unityPluginFile, targetFile);

				VirtualFile value = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile);
				targetArchiveFile = value == null ? null : ArchiveVfsUtil.getArchiveRootForLocalFile(value);
			}
			catch(Throwable e)
			{
				LOGGER.error(e);
				return;
			}

			// if target file is not found, no need change module roots
			if(targetArchiveFile == null)
			{
				return;
			}

			modifyModules(project, modifiableRootModel ->
			{
				for(ModuleRootLayer layer : modifiableRootModel.getLayers().values())
				{
					LibraryTable moduleLibraryTable = ((ModifiableModuleRootLayer) layer).getModuleLibraryTable();

					Library library = moduleLibraryTable.createLibrary();
					Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
					libraryModifiableModel.addRoot(targetArchiveFile, BinariesOrderRootType.getInstance());
					libraryModifiableModel.commit();
				}
			});
		});
	}

	private static void modifyModules(Project project, Consumer<ModifiableRootModel> action)
	{
		List<ModifiableRootModel> list = new ArrayList<>();
		ReadAction.run(() ->
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

					action.consume(modifiableModel);

					list.add(modifiableModel);
				}
			}
		});

		WriteCommandAction.runWriteCommandAction(project, () ->
		{
			for(ModifiableRootModel modifiableRootModel : list)
			{
				modifiableRootModel.commit();
			}
		});
	}
}
