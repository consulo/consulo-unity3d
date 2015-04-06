package org.mustbe.consulo.unity3d.projectImport;

import java.util.ArrayList;
import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.consulo.module.extension.MutableModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.dll.DotNetModuleFileType;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.roots.impl.ExcludedContentFolderTypeProvider;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import org.mustbe.consulo.unity3d.bundle.Unity3dDefineByVersion;
import org.mustbe.consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dTarget;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.types.BinariesOrderRootType;
import com.intellij.openapi.roots.types.DocumentationOrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
@Logger
public class Unity3dProjectUtil
{
	@NotNull
	@RequiredReadAction
	public static List<Module> importOrUpdate(@NotNull Project project, @Nullable Sdk unitySdk, @Nullable ModifiableModuleModel originalModel)
	{
		val fromProjectStructure = originalModel != null;

		val newModel = fromProjectStructure ? originalModel : ModuleManager.getInstance(project).getModifiableModel();

		List<Module> modules = new ArrayList<Module>(5);

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel, unitySdk));

		MultiMap<Module, VirtualFile> virtualFilesByModule = MultiMap.create();

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleFirstPass(project, newModel, unitySdk, virtualFilesByModule));

		ContainerUtil.addIfNotNull(modules, createAssemblyUnityScriptModuleFirstPass(project, newModel, unitySdk, virtualFilesByModule));

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(project, newModel, unitySdk, virtualFilesByModule));

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModule(project, newModel, unitySdk, virtualFilesByModule));

		if(!fromProjectStructure)
		{
			ApplicationManager.getApplication().runWriteAction(new Runnable()
			{
				@Override
				public void run()
				{
					newModel.commit();
				}
			});
		}
		return modules;
	}

	private static Module createAssemblyCSharpModule(Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule)
	{
		return createAndSetupModule("Assembly-CSharp", project, newModel, new String[]{"Assets"}, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				layer.addInvalidModuleEntry("Assembly-UnityScript-firstpass");
				layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule);
	}

	private static Module createAssemblyUnityScriptModuleFirstPass(final Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule)
	{
		val paths = new String[]{
				"Assets/Standard Assets",
				"Assets/Pro Standard Assets",
				"Assets/Plugins"
		};

		return createAndSetupModule("Assembly-UnityScript-firstpass", project, newModel, paths, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				for(String path : paths)
				{
					VirtualFile dirFile = LocalFileSystem.getInstance().findFileByPath(path);
					if(dirFile != null)
					{
						for(VirtualFile virtualFile : dirFile.getChildren())
						{
							addAsLibrary(virtualFile, layer);
						}
					}
				}
			}
		}, "unity3d-unityscript-child", JavaScriptFileType.INSTANCE, virtualFilesByModule);
	}

	private static Module createAssemblyCSharpModuleFirstPass(final Project project,
			ModifiableModuleModel newModel,
			Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule)
	{
		val paths = new String[]{
				"Assets/Standard Assets",
				"Assets/Pro Standard Assets",
				"Assets/Plugins"
		};

		return createAndSetupModule("Assembly-CSharp-firstpass", project, newModel, paths, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				for(String path : paths)
				{
					VirtualFile dirFile = LocalFileSystem.getInstance().findFileByPath(path);
					if(dirFile != null)
					{
						for(VirtualFile virtualFile : dirFile.getChildren())
						{
							addAsLibrary(virtualFile, layer);
						}
					}
				}
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule);
	}

	private static Module createAssemblyCSharpModuleEditor(final Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule)
	{
		val paths = new ArrayList<String>();
		paths.add("Assets/Standard Assets/Editor");
		paths.add("Assets/Pro Standard Assets/Editor");
		paths.add("Assets/Plugins/Editor");

		val baseDir = project.getBaseDir();

		val assetsDir = baseDir.findFileByRelativePath("Assets");
		if(assetsDir != null)
		{
			VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@NotNull VirtualFile file)
				{
					if(file.isDirectory() && "Editor".equals(file.getName()))
					{
						paths.add(VfsUtil.getRelativePath(file, baseDir, '/'));
					}
					return true;
				}
			});
		}

		val pathsAsArray = ArrayUtil.toStringArray(paths);
		return createAndSetupModule("Assembly-CSharp-Editor", project, newModel, pathsAsArray, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(final ModuleRootLayerImpl layer)
			{
				layer.addInvalidModuleEntry("Assembly-UnityScript-firstpass");
				layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
				layer.addInvalidModuleEntry("Assembly-CSharp");

				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.Graphs"));

				if(isVersionHigherOrEqual(unityBundle, "4.6.0"))
				{
					layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.UI"));
				}
				for(String path : pathsAsArray)
				{
					VirtualFile dirFile = LocalFileSystem.getInstance().findFileByPath(path);
					if(dirFile != null)
					{
						VfsUtil.visitChildrenRecursively(dirFile, new VirtualFileVisitor()
						{
							@Override
							public boolean visitFile(@NotNull VirtualFile file)
							{
								addAsLibrary(file, layer);
								return true;
							}
						});
					}
				}
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule);
	}

	@NotNull
	private static Module createAndSetupModule(String moduleName,
			@NotNull Project project,
			@NotNull ModifiableModuleModel modifiableModuleModels,
			@NotNull String[] paths,
			@Nullable Sdk unitySdk,
			@NotNull Consumer<ModuleRootLayerImpl> setupConsumer,
			@NotNull String moduleExtensionId,
			@NotNull final FileType fileType,
			@NotNull final MultiMap<Module, VirtualFile> virtualFilesByModule)
	{
		for(int i = 0; i < paths.length; i++)
		{
			paths[i] = project.getBasePath() + "/" + paths[i];
		}

		Module temp = modifiableModuleModels.findModuleByName(moduleName);
		final Module module;
		if(temp == null)
		{
			module = modifiableModuleModels.newModule(moduleName, null);
		}
		else
		{
			module = temp;
		}

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		val modifiableModel = moduleRootManager.getModifiableModel();

		modifiableModel.removeAllLayers(false);

		final List<VirtualFile> toAdd = new ArrayList<VirtualFile>();

		for(String path : paths)
		{
			VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
			if(fileByPath != null)
			{
				VfsUtil.visitChildrenRecursively(fileByPath, new VirtualFileVisitor()
				{
					@Override
					public boolean visitFile(@NotNull VirtualFile file)
					{
						if(file.getFileType() == fileType)
						{
							if(virtualFilesByModule.containsScalarValue(file))
							{
								return true;
							}

							virtualFilesByModule.putValue(module, file);
							toAdd.add(file);
						}
						return true;
					}
				});
			}
		}

		for(final Unity3dTarget unity3dTarget : Unity3dTarget.values())
		{
			val layer = (ModuleRootLayerImpl) modifiableModel.addLayer(unity3dTarget.getPresentation(), null, getDefaultTarget() == unity3dTarget);

			for(VirtualFile virtualFile : toAdd)
			{
				layer.addContentEntry(virtualFile);
			}

			setupConsumer.consume(layer);

			layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);
			// enable correct unity C# extension
			layer.<MutableModuleExtension>getExtensionWithoutCheck(moduleExtensionId).setEnabled(true);

			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "mscorlib"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine"));
			if(isVersionHigherOrEqual(unitySdk, "4.6.0"))
			{
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.UI"));
			}
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml.Linq"));
		}

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return module;
	}

	@NotNull
	private static Version parseBundleVersion(@Nullable Sdk unityBundle)
	{
		String currentVersionString = unityBundle == null ? Unity3dBundleType.UNKNOWN_VERSION : unityBundle.getVersionString();
		return parseVersion(currentVersionString);
	}

	private static boolean isVersionHigherOrEqual(@Nullable Sdk unityBundle, @NotNull String requiredVersionString)
	{
		Version currentVersion = parseBundleVersion(unityBundle);
		Version requiredVersion = parseVersion(requiredVersionString);
		return currentVersion.isOrGreaterThan(requiredVersion.major, requiredVersion.minor, requiredVersion.bugfix);
	}

	@NotNull
	private static Version parseVersion(@Nullable String versionString)
	{
		if(versionString == null)
		{
			return new Version(0, 0, 0);
		}
		List<String> list = StringUtil.split(versionString, ".");
		if(list.size() >= 3)
		{
			try
			{
				return new Version(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)), Integer.parseInt(list.get(2)));
			}
			catch(NumberFormatException ignored)
			{
			}
		}
		return new Version(0, 0, 0);
	}

	private static void addAsLibrary(VirtualFile virtualFile, ModuleRootLayerImpl layer)
	{
		if(virtualFile.getFileType() == DotNetModuleFileType.INSTANCE)
		{
			VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(virtualFile);
			if(archiveRootForLocalFile != null)
			{
				Library library = layer.getModuleLibraryTable().createLibrary();
				Library.ModifiableModel modifiableModel = library.getModifiableModel();
				modifiableModel.addRoot(archiveRootForLocalFile, BinariesOrderRootType.getInstance());
				VirtualFile docFile = virtualFile.getParent().findChild(virtualFile.getNameWithoutExtension() + ".xml");
				if(docFile != null)
				{
					modifiableModel.addRoot(docFile, DocumentationOrderRootType.getInstance());
				}
				modifiableModel.commit();

				LibraryOrderEntry libraryOrderEntry = layer.findLibraryOrderEntry(library);
				assert libraryOrderEntry != null;
				libraryOrderEntry.setExported(true);
			}
		}
	}

	private static Unity3dTarget getDefaultTarget()
	{
		if(SystemInfo.isWindows)
		{
			return Unity3dTarget.Windows;
		}
		else if(SystemInfo.isLinux)
		{
			return Unity3dTarget.LinuxUniversal;
		}
		else if(SystemInfo.isMac)
		{
			return Unity3dTarget.OSXUniversal;
		}
		throw new IllegalArgumentException(SystemInfo.OS_NAME);
	}

	@NotNull
	@RequiredReadAction
	private static Module createRootModule(Project project, ModifiableModuleModel newModel, Sdk unityBundle)
	{
		Module rootModule;
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);
		if(rootModuleExtension != null)
		{
			rootModule = rootModuleExtension.getModule();
		}
		else
		{
			rootModule = newModel.newModule(project.getName(), project.getBasePath());
		}

		String projectUrl = project.getBaseDir().getUrl();

		val modifiableModel = ModuleRootManager.getInstance(rootModule).getModifiableModel();
		modifiableModel.removeAllLayers(false);

		for(Unity3dTarget unity3dTarget : Unity3dTarget.values())
		{
			ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.addLayer(unity3dTarget.getPresentation(), null,
					getDefaultTarget() == unity3dTarget);

			ContentEntry contentEntry = layer.addContentEntry(projectUrl);

			Unity3dRootMutableModuleExtension extension = layer.getExtensionWithoutCheck(Unity3dRootMutableModuleExtension.class);
			assert extension != null;
			extension.setEnabled(true);
			extension.getInheritableSdk().set(null, unityBundle);

			extension.getVariables().add(unity3dTarget.getDefineName());

			Version currentBundleVersion = parseBundleVersion(unityBundle);
			Unity3dDefineByVersion unity3dDefineByVersion = Unity3dDefineByVersion.find(currentBundleVersion.toString());
			if(unity3dDefineByVersion != Unity3dDefineByVersion.UNKNOWN)
			{
				extension.getVariables().add(unity3dDefineByVersion.name());
			}

			// exclude temp dirs
			contentEntry.addFolder(projectUrl + "/" + Project.DIRECTORY_STORE_FOLDER, ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/Library", ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/Temp", ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/test_Data", ExcludedContentFolderTypeProvider.getInstance());
		}

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return rootModule;
	}
}
