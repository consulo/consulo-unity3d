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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.logging.Logger;
import consulo.project.startup.StartupActivity;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.ModuleRootLayer;
import consulo.roots.types.BinariesOrderRootType;
import consulo.ui.UIAccess;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dManifest;
import consulo.util.collection.ArrayUtil;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 26-Jul-16
 */
@Singleton
public class UnityPluginValidator implements StartupActivity.Background
{
	private static final Logger LOG = Logger.getInstance(UnityPluginValidator.class);

	public static final String PLUGIN_ID = "com.consulo.ide";
	public static final String PLUGIN_LINK = "https://github.com/consulo/UnityEditorConsuloPlugin.git#2.6.0";

	private static final String ourPath = "Assets/Editor/Plugins";
	private static final NotificationGroup ourGroup = new NotificationGroup("consulo.unity", NotificationDisplayType.STICKY_BALLOON, true);

	@Override
	public void runActivity(@Nonnull Project project, @Nonnull UIAccess uiAccess)
	{
		uiAccess.give(() -> notifyAboutPluginFile(project));
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

		Unity3dManifest manifest = Unity3dManifest.parse(project);
		// no files - not supported
		if(manifest == Unity3dManifest.EMPTY)
		{
			return;
		}

		String ver = manifest.dependencies.get(PLUGIN_ID);

		// same version
		if(PLUGIN_LINK.equals(ver))
		{
			return;
		}

		if(ver == null)
		{
			showNotify(project, "Consulo plugin for UnityEditor is missing.<br><a href=\"update\">Install via manifest</a>", false);
		}
		else
		{
			showNotify(project, "Outdated Consulo plugin for UnityEditor.<br><a href=\"update\">Update via manifest</a>", true);
		}
	}

	private static void showNotify(final Project project, @Nonnull String text, boolean update)
	{
		Notification notification = new Notification(ourGroup.getDisplayId(), "Unity3D Plugin", text, update ? NotificationType.WARNING : NotificationType.INFORMATION);
		notification.setListener((thisNotification, hyperlinkEvent) ->
		{
			thisNotification.hideBalloon();

			switch(hyperlinkEvent.getDescription())
			{
				case "update":
					updatePlugin(project);
					break;
			}
		});
		notification.notify(project);
	}

	private static void updatePlugin(@Nonnull final Project project)
	{
		Task.Backgroundable.queue(project, "Changing manifest.json", (progressIndicator) ->
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

			List<VirtualFile> oldPluginFiles = new SmartList<>();

			VirtualFile fileByRelativePath = project.getBaseDir().findFileByRelativePath(ourPath);
			if(fileByRelativePath != null)
			{
				VirtualFile[] children = fileByRelativePath.getChildren();
				for(VirtualFile child : children)
				{
					CharSequence nameSequence = child.getNameSequence();
					if(StringUtil.startsWith(nameSequence, "UnityEditorConsuloPlugin") && child.getFileType() == DotNetModuleFileType.INSTANCE)
					{
						oldPluginFiles.add(child);
					}
				}
			}

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
					LOG.error(e);
					return;
				}
			}

			Unity3dManifest manifest = Unity3dManifest.parse(project);

			Unity3dManifest newManifest = manifest.clone();

			Unity3dManifest.ScopeRegistry[] scopedRegistries = newManifest.scopedRegistries;
			if(scopedRegistries != null)
			{
				for(Unity3dManifest.ScopeRegistry registry : List.of(scopedRegistries))
				{
					if("https://upm.consulo.io/".equals(registry.url))
					{
						scopedRegistries = ArrayUtil.remove(scopedRegistries, registry);
					}
				}

				if(scopedRegistries.length == 0)
				{
					newManifest.scopedRegistries = null;
				}
				else
				{
					newManifest.scopedRegistries = scopedRegistries;
				}
			}

			newManifest.dependencies.put(PLUGIN_ID, PLUGIN_LINK);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			WriteCommandAction.runWriteCommandAction(project, () -> Unity3dManifest.write(project, gson.toJson(newManifest)));
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
